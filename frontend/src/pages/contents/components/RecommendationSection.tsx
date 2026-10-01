import {
  ChevronLeft,
  ChevronRight,
  Sparkles,
} from 'lucide-react';
import {
  useEffect,
  useRef,
} from 'react';

import useRecommendationStore from '@/lib/stores/useRecommendationStore';
import type { RecommendationItem } from '@/lib/types';
import useHorizontalScrollFade from '@/pages/contents/hooks/useHorizontalScrollFade';

import RecommendationCard from './RecommendationCard';

const USE_MOCK_DATA = true;

const MOCK_RECOMMENDATIONS: RecommendationItem[] = [
  {
    contentId: '11111111-1111-1111-1111-111111111111',
    title: 'Interstellar',
    type: 'MOVIE',
    thumbnailUrl: '/placeholder-movie.png',
    tags: ['SF', '우주', '드라마'],
    semanticScore: 0.93,
    externalRating: 8.7,
    externalPopularity: 294.4,
    externalVoteCount: 38124,
    reason:
      '높은 평점의 SF 드라마 장르를 바탕으로, 우주를 배경으로 한 깊이 있는 이야기를 좋아할 가능성이 높아 추천했어요.',
  },
  {
    contentId: '22222222-2222-2222-2222-222222222222',
    title: 'The Last of Us',
    type: 'TV_SERIES',
    thumbnailUrl: '/placeholder-movie.png',
    tags: ['드라마', '액션', '포스트 아포칼립스'],
    semanticScore: 0.91,
    externalRating: 8.6,
    externalPopularity: 410.2,
    externalVoteCount: 6721,
    reason:
      '몰입감 있는 서사와 인물 관계를 중심으로 한 콘텐츠를 선호하는 시청 취향과 잘 맞는 작품이에요.',
  },
  {
    contentId: '33333333-3333-3333-3333-333333333333',
    title: 'Formula 1',
    type: 'SPORT',
    thumbnailUrl: '/placeholder-movie.png',
    tags: ['F1', '모터스포츠', '레이싱'],
    semanticScore: 0.89,
    externalRating: 8.9,
    externalPopularity: 380.7,
    externalVoteCount: 9214,
    reason:
      '스포츠 콘텐츠 중에서도 빠른 전개와 경쟁 구도가 강한 콘텐츠에 관심을 보인 기록을 바탕으로 추천했어요.',
  },
  {
    contentId: '44444444-4444-4444-4444-444444444444',
    title: 'Dune: Part Two',
    type: 'MOVIE',
    thumbnailUrl: '/placeholder-movie.png',
    tags: ['SF', '모험', '액션'],
    semanticScore: 0.87,
    externalRating: 8.5,
    externalPopularity: 520.8,
    externalVoteCount: 15841,
    reason:
      'SF 세계관과 긴 호흡의 서사를 가진 영화를 선호하는 취향을 반영해 추천했어요.',
  },
  {
    contentId: '55555555-5555-5555-5555-555555555555',
    title: 'Severance',
    type: 'TV_SERIES',
    thumbnailUrl: '/placeholder-movie.png',
    tags: ['미스터리', 'SF', '스릴러'],
    semanticScore: 0.84,
    externalRating: 8.7,
    externalPopularity: 307.1,
    externalVoteCount: 4832,
    reason:
      '미스터리와 SF 요소가 섞인 콘텐츠에 대한 선호도가 높아 비슷한 분위기의 작품으로 골랐어요.',
  },
  {
    contentId: '66666666-6666-6666-6666-666666666666',
    title: 'Premier League',
    type: 'SPORT',
    thumbnailUrl: '/placeholder-movie.png',
    tags: ['축구', '프리미어리그', '스포츠'],
    semanticScore: 0.8,
    externalRating: 8.3,
    externalPopularity: 275.6,
    externalVoteCount: 12402,
    reason:
      '경기 중심의 스포츠 콘텐츠와 실시간으로 함께 즐길 수 있는 콘텐츠를 선호하는 패턴을 반영했어요.',
  },
];

export default function RecommendationSection() {
  const scrollContainerRef = useRef<HTMLDivElement>(null);

  const {
    data,
    loading,
    error,
    fetch,
  } = useRecommendationStore();

  useEffect(() => {
    if (!USE_MOCK_DATA) {
      void fetch();
    }
  }, [fetch]);

  const recommendations = USE_MOCK_DATA
    ? MOCK_RECOMMENDATIONS
    : data;

  const isLoading = !USE_MOCK_DATA && loading;
  const hasError = !USE_MOCK_DATA && Boolean(error);

  const {
    showLeftFade,
    showRightFade,
  } = useHorizontalScrollFade(
    scrollContainerRef,
    `${isLoading}-${recommendations.length}`,
  );

  const handleScrollLeft = () => {
    scrollContainerRef.current?.scrollBy({
      left: -552,
      behavior: 'smooth',
    });
  };

  const handleScrollRight = () => {
    scrollContainerRef.current?.scrollBy({
      left: 552,
      behavior: 'smooth',
    });
  };

  if (hasError) {
    return (
      <section
        className="
          w-full
          min-w-0
          max-w-full
          overflow-hidden
          rounded-3xl
          border
          border-gray-800
          bg-gray-900/50
          px-6
          py-8
        "
      >
        <div className="flex items-center gap-2">
          <Sparkles className="h-5 w-5 text-pink-400" />

          <h2 className="text-xl font-bold text-white">
            OO님을 위한 추천
          </h2>
        </div>

        <p className="mt-3 text-body2-m text-gray-500">
          추천 콘텐츠를 불러오지 못했어요.
        </p>
      </section>
    );
  }

  if (!isLoading && recommendations.length === 0) {
    return null;
  }

  return (
    <section
      className="
        relative
        w-full
        min-w-0
        max-w-full
        overflow-hidden
        rounded-3xl
        border
        border-gray-800
        bg-gray-900/50
        px-6
        py-7
      "
    >
      <div
        className="
          pointer-events-none
          absolute
          -left-20
          -top-28
          h-64
          w-64
          rounded-full
          bg-pink-500/5
          blur-3xl
        "
      />

      <div
        className="
          relative
          mb-6
          flex
          items-end
          justify-between
          gap-4
        "
      >
        <div className="flex items-center gap-3">
          <div
            className="
              flex
              h-9
              w-9
              items-center
              justify-center
              rounded-xl
              bg-pink-500/10
            "
          >
            <Sparkles className="h-5 w-5 text-pink-400" />
          </div>

          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-xl font-bold text-white">
                OO님을 위한 추천
              </h2>

              <span
                className="
                  rounded-full
                  bg-pink-500/10
                  px-2
                  py-1
                  text-[11px]
                  font-semibold
                  text-pink-300
                "
              >
                AI
              </span>
            </div>

            <p className="mt-1 text-body2-m text-gray-400">
              시청 기록과 리뷰 취향을 바탕으로 골라봤어요.
            </p>
          </div>
        </div>

        {!isLoading && recommendations.length > 0 && (
          <div className="hidden items-center gap-2 sm:flex">
            <button
              type="button"
              onClick={handleScrollLeft}
              aria-label="이전 추천 콘텐츠 보기"
              className="
                flex
                h-9
                w-9
                items-center
                justify-center
                rounded-full
                border
                border-gray-700
                bg-gray-800
                text-gray-300
                transition
                hover:border-gray-600
                hover:bg-gray-700
                hover:text-white
              "
            >
              <ChevronLeft className="h-5 w-5" />
            </button>

            <button
              type="button"
              onClick={handleScrollRight}
              aria-label="다음 추천 콘텐츠 보기"
              className="
                flex
                h-9
                w-9
                items-center
                justify-center
                rounded-full
                border
                border-gray-700
                bg-gray-800
                text-gray-300
                transition
                hover:border-gray-600
                hover:bg-gray-700
                hover:text-white
              "
            >
              <ChevronRight className="h-5 w-5" />
            </button>
          </div>
        )}
      </div>

      {isLoading ? (
        <div className="flex w-full min-w-0 max-w-full gap-4 overflow-hidden">
          {Array.from({ length: 4 }).map((_, index) => (
            <RecommendationCardSkeleton key={index} />
          ))}
        </div>
      ) : (
        <div className="relative min-w-0 overflow-hidden">
          <div
            ref={scrollContainerRef}
            className="
              scrollbar-hide
              flex
              w-full
              min-w-0
              max-w-full
              gap-4
              overflow-x-auto
              overflow-y-hidden
              scroll-smooth
              pb-1
            "
          >
            {recommendations.map((recommendation) => (
              <RecommendationCard
                key={recommendation.contentId}
                recommendation={recommendation}
              />
            ))}
          </div>

          {showLeftFade && (
            <div
              className="
                pointer-events-none
                absolute
                inset-y-0
                left-0
                z-10
                w-24
                bg-gradient-to-r
                from-gray-950/95
                via-gray-950/55
                to-transparent
              "
            />
          )}

          {showRightFade && (
            <div
              className="
                pointer-events-none
                absolute
                inset-y-0
                right-0
                z-10
                w-24
                bg-gradient-to-l
                from-gray-950/95
                via-gray-950/55
                to-transparent
              "
            />
          )}
        </div>
      )}
    </section>
  );
}

function RecommendationCardSkeleton() {
  return (
    <div
      className="
        w-[260px]
        flex-shrink-0
        animate-pulse
        overflow-hidden
        rounded-2xl
        border
        border-gray-800
        bg-gray-900
      "
    >
      <div className="aspect-[260/390] w-full bg-gray-800" />

      <div className="p-5">
        <div className="flex justify-between">
          <div className="h-7 w-20 rounded-full bg-gray-800" />
          <div className="h-5 w-16 rounded bg-gray-800" />
        </div>

        <div className="mt-5 h-6 w-4/5 rounded bg-gray-800" />
        <div className="mt-4 h-[125px] rounded-xl bg-gray-800" />

        <div className="mt-4 flex gap-2">
          <div className="h-6 w-14 rounded-full bg-gray-800" />
          <div className="h-6 w-16 rounded-full bg-gray-800" />
          <div className="h-6 w-12 rounded-full bg-gray-800" />
        </div>
      </div>
    </div>
  );
}