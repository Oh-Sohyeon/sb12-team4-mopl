import { useEffect, useState } from 'react';
import { MessageCircle } from 'lucide-react';

import {
    getAiPlaylistSessions,
    type AiPlaylistSession,
} from '@/lib/api/aiPlaylists';

interface AiPlaylistSessionListProps {
    onSelect: (sessionId: string) => void;
}

const formatUpdatedAt = (value: string) => {
    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return '';
    }

    const now = new Date();

    const isToday =
        date.getFullYear() === now.getFullYear() &&
        date.getMonth() === now.getMonth() &&
        date.getDate() === now.getDate();

    if (isToday) {
        return new Intl.DateTimeFormat('ko-KR', {
            hour: '2-digit',
            minute: '2-digit',
        }).format(date);
    }

    return new Intl.DateTimeFormat('ko-KR', {
        month: 'long',
        day: 'numeric',
    }).format(date);
};

export default function AiPlaylistSessionList({
                                                  onSelect,
                                              }: AiPlaylistSessionListProps) {
    const [sessions, setSessions] = useState<AiPlaylistSession[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let cancelled = false;

        const loadSessions = async () => {
            try {
                setIsLoading(true);
                setError(null);

                const response = await getAiPlaylistSessions({
                    limit: 20,
                    sortDirection: 'DESCENDING',
                });

                if (!cancelled) {
                    setSessions(response.data);
                }
            } catch {
                if (!cancelled) {
                    setError('대화 목록을 불러오지 못했어요.');
                }
            } finally {
                if (!cancelled) {
                    setIsLoading(false);
                }
            }
        };

        void loadSessions();

        return () => {
            cancelled = true;
        };
    }, []);

    if (isLoading) {
        return (
            <div className="flex flex-1 items-center justify-center text-sm text-gray-400">
                대화 목록을 불러오는 중...
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex flex-1 items-center justify-center px-6 text-center text-sm text-red-400">
                {error}
            </div>
        );
    }

    if (sessions.length === 0) {
        return (
            <div className="flex flex-1 items-center justify-center px-6 text-center">
                <div>
                    <MessageCircle className="mx-auto mb-3 h-8 w-8 text-gray-500" />

                    <p className="font-medium text-white">
                        아직 이전 대화가 없어요
                    </p>

                    <p className="mt-1 text-sm text-gray-400">
                        + 버튼을 눌러 새로운 대화를 시작해보세요.
                    </p>
                </div>
            </div>
        );
    }

    return (
        <div className="ai-chat-scroll min-h-0 flex-1 overflow-y-auto p-3">
            <div className="flex flex-col gap-1">
                {sessions.map((session) => (
                    <button
                        key={session.id}
                        type="button"
                        onClick={() => onSelect(session.id)}
                        className="
                            flex w-full items-center gap-3 rounded-xl
                            px-3 py-3 text-left
                            transition-colors hover:bg-gray-800
                        "
                    >
                        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-gray-800">
                            <MessageCircle className="h-5 w-5 text-pink-500" />
                        </div>

                        <div className="min-w-0 flex-1">
                            <p className="truncate text-sm font-medium text-white">
                                {session.title?.trim() || '새로운 대화'}
                            </p>

                            <p className="mt-1 text-xs text-gray-500">
                                {formatUpdatedAt(session.updatedAt)}
                            </p>
                        </div>
                    </button>
                ))}
            </div>
        </div>
    );
}