import {
  Clapperboard,
  Sparkles,
  Trophy,
  Tv,
} from 'lucide-react';
import {
  useRef,
  type ReactNode,
} from 'react';

import useHorizontalScrollFade from '@/pages/contents/hooks/useHorizontalScrollFade';

interface NewContentItem {
  id: string;
  title: string;
  type: '영화' | 'TV 시리즈' | '스포츠';
  thumbnailUrl: string;
  rating: number;
  tags: string[];
  addedLabel: string;
}

const PERSONALIZED_NEW_CONTENTS: NewContentItem[] = [
  {
    id: 'personal-new-1',
    title: 'Dune: Part Two',
    type: '영화',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.8,
    tags: ['SF', '모험'],
    addedLabel: '최근 추가',
  },
  {
    id: 'personal-new-2',
    title: 'Severance',
    type: 'TV 시리즈',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.7,
    tags: ['미스터리', 'SF'],
    addedLabel: '최근 추가',
  },
  {
    id: 'personal-new-3',
    title: 'Formula 1',
    type: '스포츠',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.6,
    tags: ['F1', '레이싱'],
    addedLabel: '최근 추가',
  },
  {
    id: 'personal-new-4',
    title: 'The Last of Us',
    type: 'TV 시리즈',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.7,
    tags: ['드라마', '액션'],
    addedLabel: '최근 추가',
  },
  {
    id: 'personal-new-5',
    title: 'Interstellar',
    type: '영화',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.9,
    tags: ['SF', '우주'],
    addedLabel: '최근 추가',
  },
];

const NEW_MOVIES: NewContentItem[] = [
  {
    id: 'new-movie-1',
    title: 'Dune: Part Two',
    type: '영화',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.8,
    tags: ['SF', '모험'],
    addedLabel: '오늘 추가',
  },
  {
    id: 'new-movie-2',
    title: 'Civil War',
    type: '영화',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.4,
    tags: ['액션', '드라마'],
    addedLabel: '1일 전',
  },
  {
    id: 'new-movie-3',
    title: 'Challengers',
    type: '영화',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.5,
    tags: ['드라마', '스포츠'],
    addedLabel: '2일 전',
  },
  {
    id: 'new-movie-4',
    title: 'Furiosa',
    type: '영화',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.6,
    tags: ['액션', '모험'],
    addedLabel: '3일 전',
  },
  {
    id: 'new-movie-5',
    title: 'Poor Things',
    type: '영화',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.5,
    tags: ['판타지', '드라마'],
    addedLabel: '4일 전',
  },
];

const NEW_TV_SERIES: NewContentItem[] = [
  {
    id: 'new-tv-1',
    title: 'Severance',
    type: 'TV 시리즈',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.8,
    tags: ['미스터리', 'SF'],
    addedLabel: '오늘 추가',
  },
  {
    id: 'new-tv-2',
    title: 'The Last of Us',
    type: 'TV 시리즈',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.7,
    tags: ['드라마', '액션'],
    addedLabel: '1일 전',
  },
  {
    id: 'new-tv-3',
    title: 'Shogun',
    type: 'TV 시리즈',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.8,
    tags: ['드라마', '역사'],
    addedLabel: '2일 전',
  },
  {
    id: 'new-tv-4',
    title: 'Fallout',
    type: 'TV 시리즈',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.6,
    tags: ['SF', '액션'],
    addedLabel: '3일 전',
  },
  {
    id: 'new-tv-5',
    title: 'The Bear',
    type: 'TV 시리즈',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.7,
    tags: ['드라마', '코미디'],
    addedLabel: '4일 전',
  },
];

const NEW_SPORTS: NewContentItem[] = [
  {
    id: 'new-sport-1',
    title: 'Formula 1',
    type: '스포츠',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.8,
    tags: ['F1', '레이싱'],
    addedLabel: '오늘 추가',
  },
  {
    id: 'new-sport-2',
    title: 'Premier League',
    type: '스포츠',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.8,
    tags: ['축구', '프리미어리그'],
    addedLabel: '1일 전',
  },
  {
    id: 'new-sport-3',
    title: 'Champions League',
    type: '스포츠',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.9,
    tags: ['축구', '챔피언스리그'],
    addedLabel: '2일 전',
  },
  {
    id: 'new-sport-4',
    title: 'NBA',
    type: '스포츠',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.7,
    tags: ['농구', 'NBA'],
    addedLabel: '3일 전',
  },
  {
    id: 'new-sport-5',
    title: 'MLB',
    type: '스포츠',
    thumbnailUrl: '/placeholder-movie.png',
    rating: 4.6,
    tags: ['야구', 'MLB'],
    addedLabel: '4일 전',
  },
];

export default function NewTab() {
  return (
    <div className="flex min-w-0 flex-col gap-12">
      <NewRailSection
        icon={<Sparkles className="h-5 w-5 text-pink-400" />}
        title="새 콘텐츠 중 OO님 취향"
        description="최근 MOPL에 추가된 콘텐츠 중 취향에 맞는 작품을 골라봤어요."
        contents={PERSONALIZED_NEW_CONTENTS}
      />

      <NewRailSection
        icon={<Clapperboard className="h-5 w-5 text-violet-400" />}
        title="새로 추가된 영화"
        description="최근 MOPL에 등록된 영화를 확인해보세요."
        contents={NEW_MOVIES}
      />

      <NewRailSection
        icon={<Tv className="h-5 w-5 text-emerald-400" />}
        title="새로 추가된 TV 시리즈"
        description="최근 MOPL에 등록된 TV 시리즈예요."
        contents={NEW_TV_SERIES}
      />

      <NewRailSection
        icon={<Trophy className="h-5 w-5 text-amber-400" />}
        title="새로 추가된 스포츠"
        description="최근 MOPL에 등록된 스포츠 콘텐츠예요."
        contents={NEW_SPORTS}
      />
    </div>
  );
}

function NewRailSection({
  icon,
  title,
  description,
  contents,
}: {
  icon: ReactNode;
  title: string;
  description: string;
  contents: NewContentItem[];
}) {
  const scrollContainerRef = useRef<HTMLDivElement>(null);

  const {
    showLeftFade,
    showRightFade,
  } = useHorizontalScrollFade(scrollContainerRef, contents.length);

  return (
    <section className="min-w-0">
      <div className="mb-5 flex items-start gap-3">
        <div
          className="
            flex
            h-9
            w-9
            flex-shrink-0
            items-center
            justify-center
            rounded-xl
            bg-gray-900
          "
        >
          {icon}
        </div>

        <div>
          <h2 className="text-title1-b-140 text-white">
            {title}
          </h2>

          <p className="mt-1 text-body3-m text-gray-500">
            {description}
          </p>
        </div>
      </div>

      <div className="relative min-w-0 overflow-hidden">
        <div
          ref={scrollContainerRef}
          className="
            scrollbar-hide
            flex
            min-w-0
            gap-4
            overflow-x-auto
            overflow-y-hidden
            pb-2
          "
        >
          {contents.map((content) => (
            <NewContentCard
              key={content.id}
              content={content}
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
    </section>
  );
}

function NewContentCard({
  content,
}: {
  content: NewContentItem;
}) {
  return (
    <div className="group w-[200px] flex-shrink-0">
      <div
        className="
          relative
          aspect-[260/390]
          w-full
          overflow-hidden
          rounded-2xl
          bg-gray-800
        "
      >
        <img
          src={content.thumbnailUrl}
          alt={content.title}
          className="
            h-full
            w-full
            object-cover
            transition-transform
            duration-300
            group-hover:scale-[1.03]
          "
          onError={(event) => {
            event.currentTarget.src = '/placeholder-movie.png';
          }}
        />

        <div
          className="
            pointer-events-none
            absolute
            inset-0
            bg-gradient-to-t
            from-black/60
            via-transparent
            to-transparent
          "
        />

        <span
          className="
            absolute
            left-3
            top-3
            rounded-full
            bg-pink-500
            px-2.5
            py-1
            text-caption1-sb
            text-white
          "
        >
          {content.addedLabel}
        </span>

        <span
          className="
            absolute
            bottom-3
            left-3
            rounded-full
            bg-black/70
            px-2.5
            py-1
            text-caption1-sb
            text-gray-100
          "
        >
          {content.type}
        </span>
      </div>

      <div className="mt-3">
        <h3 className="line-clamp-1 text-body1-b text-gray-50">
          {content.title}
        </h3>

        <div className="mt-1.5 flex items-center gap-1.5">
          <span className="text-sm text-yellow-400">
            ★
          </span>

          <span className="text-body3-m text-gray-400">
            {content.rating.toFixed(1)}
          </span>
        </div>

        <div className="mt-2 flex gap-1.5 overflow-hidden">
          {content.tags.slice(0, 2).map((tag) => (
            <span
              key={tag}
              className="
                flex-shrink-0
                rounded-full
                bg-gray-900
                px-2
                py-1
                text-caption1-m
                text-gray-400
              "
            >
              #{tag}
            </span>
          ))}
        </div>
      </div>
    </div>
  );
}