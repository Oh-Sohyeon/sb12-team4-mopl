import { useState } from 'react';
import { Bot, X } from 'lucide-react';

import { Button } from '@/components/ui/button';

import AiPlaylistChat from './AiPlaylistChat';

export default function AiPlaylist() {
    const [isOpen, setIsOpen] = useState(false);

    return (
        <>
            {isOpen && (
                <section
                    className="
                       fixed bottom-24 right-6 z-50
                       flex h-[600px] w-[380px] flex-col
                       overflow-hidden rounded-2xl border border-[#303038] bg-[#18181d] shadow-xl
                    "
                    aria-label="AI Playlist"
                >
                    <header className="flex items-center justify-between border-b border-gray-800 px-4 py-3">
                        <div className="flex items-center gap-2">
                            <Bot className="h-5 w-5 text-pink-500" />

                            <div>
                                <h2 className="font-semibold text-white">
                                    AI Playlist
                                </h2>

                                <p className="text-xs text-gray-400">
                                    원하는 콘텐츠를 말해보세요
                                </p>
                            </div>
                        </div>

                        <Button
                            type="button"
                            variant="ghost"
                            size="icon"
                            onClick={() => setIsOpen(false)}
                            aria-label="AI Playlist 닫기"
                            className="text-gray-400 hover:bg-gray-700 hover:text-white"
                        >
                            <X className="h-5 w-5" />
                        </Button>
                    </header>

                    <AiPlaylistChat />
                </section>
            )}

            <Button
                type="button"
                size="icon"
                className="
                    fixed bottom-6 right-6 z-50
                    h-14 w-14 rounded-full
                    bg-pink-500 text-white shadow-lg
                    hover:bg-pink-600 hover:text-white
                "
                onClick={() => setIsOpen((prev) => !prev)}
                aria-label={isOpen ? 'AI Playlist 닫기' : 'AI Playlist 열기'}
                aria-expanded={isOpen}
            >
                {isOpen ? (
                    <X className="h-6 w-6 text-white" />
                ) : (
                    <Bot className="h-11 w-11 text-white" />
                )}
            </Button>
        </>
    );
}