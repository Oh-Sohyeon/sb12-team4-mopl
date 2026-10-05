import { Client, type StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { create } from 'zustand';
import { toast } from 'sonner';

interface ChatRestriction {
  restrictionLevel: 'TEMPORARY_SHORT' | 'TEMPORARY_LONG';
  restrictedUntil: string;
}

let restrictionTimer: ReturnType<typeof setTimeout> | undefined;
export function formatRestrictionEnd(until: string) {
  const date = new Date(until);
  const today = new Date();
  return date.toLocaleString('ko-KR', {
    ...(date.toDateString() !== today.toDateString() ? { month: '2-digit', day: '2-digit' } : {}),
    hour: '2-digit', minute: '2-digit', hour12: false,
  });
}

interface WebSocketState {
  restriction: ChatRestriction | null;
  handleModerationError: (payload: unknown) => void;
  stompClient: Client | null;
  isConnected: boolean;
  isConnecting: boolean;
  subscriptions: Map<string, StompSubscription>;
  connect: (accessToken: string) => Promise<void>;
  disconnect: () => void;
  subscribe: (destination: string, callback: (message: any) => void) => void;
  unsubscribe: (destination: string) => void;
  send: (destination: string, body: any) => void;
}

export const useWebSocketStore = create<WebSocketState>((set, get) => ({
  restriction: null,
  handleModerationError: (payload: unknown) => {
    if (!payload || typeof payload !== 'object') return;
    const error = payload as Record<string, unknown>;
    if (error.code !== 'CHAT_RESTRICTED' && error.code !== 'MODERATION_002') {
      if (typeof error.message === 'string') toast.error(error.message);
      return;
    }
    const until = typeof error.restrictedUntil === 'string' ? Date.parse(error.restrictedUntil) : NaN;
    if (!Number.isFinite(until)) {
      toast.error('반복적인 메시지 이용 정책 위반으로 채팅 이용이 일시적으로 제한되었습니다.');
      return;
    }
    // 만료된 응답은 제한 UI를 다시 만들지 않는다.
    if (until <= Date.now()) return;
    if (error.restrictionLevel !== 'TEMPORARY_SHORT' && error.restrictionLevel !== 'TEMPORARY_LONG') return;
    const restriction: ChatRestriction = {
      restrictionLevel: error.restrictionLevel, restrictedUntil: error.restrictedUntil as string,
    };
    if (restrictionTimer !== undefined) clearTimeout(restrictionTimer);
    set({ restriction });
    const clearWhenExpired = () => {
      const remaining = until - Date.now();
      if (remaining > 0) restrictionTimer = setTimeout(clearWhenExpired, Math.min(remaining, 2147483647));
      else { restrictionTimer = undefined; set({ restriction: null }); }
    };
    clearWhenExpired();
    toast.error(`반복적인 메시지 이용 정책 위반으로 채팅 이용이 일시적으로 제한되었습니다. ${formatRestrictionEnd(restriction.restrictedUntil)}까지 메시지를 보낼 수 없습니다.`);
  },
  stompClient: null,
  isConnected: false,
  isConnecting: false,
  subscriptions: new Map(),

  connect: async (accessToken: string) => {
    const { isConnected, isConnecting } = get();
    if (isConnected || isConnecting) return;

    set({ isConnecting: true });

    const BASE_URL = import.meta.env.VITE_API_BASE_URL || '';
    const socket = new SockJS(`${BASE_URL}/ws`);
    const client = new Client({
      webSocketFactory: () => socket,
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      debug: (str) => {
        console.log(str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      // 재연결에서도 사용자 전용 오류를 방 구독보다 먼저 등록한다.
      const errors = client.subscribe('/user/sub/errors', (message) => {
        try { get().handleModerationError(JSON.parse(message.body)); }
        catch { toast.error('메시지 전송 오류를 확인할 수 없습니다.'); }
      });
      set({ stompClient: client, isConnected: true, isConnecting: false,
        subscriptions: new Map([['/user/sub/errors', errors]]) });
      console.log('WebSocket 연결 성공');
    };

    client.onStompError = (frame) => {
      console.error('STOMP 에러:', frame);
      set({ isConnected: false, stompClient: null, isConnecting: false, subscriptions: new Map() });
    };

    client.onWebSocketClose = () => {
      set({ isConnected: false, stompClient: null, isConnecting: false, subscriptions: new Map() });
    };

    client.activate();
  },

  disconnect: () => {
    const { stompClient, isConnected } = get();
    if (stompClient && isConnected) stompClient.deactivate();
    if (restrictionTimer !== undefined) clearTimeout(restrictionTimer);
    restrictionTimer = undefined;
    set({ stompClient: null, isConnected: false, isConnecting: false, subscriptions: new Map(), restriction: null });
  },

  subscribe: (destination: string, callback: <T>(message: T) => void) => {
    const { stompClient, isConnected, subscriptions } = get();

    if (subscriptions.has(destination) || !isConnected || stompClient == null) {
      return;
    }

    const subscription: StompSubscription = stompClient.subscribe(destination, (message) => {
      const payload = JSON.parse(message.body);
      callback(payload);
    });

    subscriptions.set(destination, subscription);
  },

  unsubscribe: (destination: string) => {
    const { stompClient, isConnected, subscriptions } = get();

    if (!subscriptions.has(destination) || !isConnected || stompClient == null) {
      return;
    }

    const subscription = subscriptions.get(destination);
    if (subscription) {
      subscription.unsubscribe();
      subscriptions.delete(destination);
      set({ subscriptions });
    }
  },

  send: (destination: string, body: any) => {
    const { stompClient, isConnected, restriction } = get();
    const isMessage = /^\/pub\/(contents\/[^/]+\/chat|conversations\/[^/]+\/direct-messages)$/.test(destination);
    if (isMessage && restriction) {
      if (Date.parse(restriction.restrictedUntil) > Date.now()) {
        toast.error(`반복적인 메시지 이용 정책 위반으로 채팅 이용이 일시적으로 제한되었습니다. ${formatRestrictionEnd(restriction.restrictedUntil)}까지 메시지를 보낼 수 없습니다.`);
        return;
      }
      set({ restriction: null });
    }

    if (stompClient && isConnected) {
      stompClient.publish({
        destination,
        body: JSON.stringify(body),
      });
    } else {
      console.error('WebSocket이 연결되어 있지 않습니다.');
    }
  },
}));
