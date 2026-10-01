import ContentRailSection, {
  type SectionContentItem,
  type SectionContentType,
} from './ContentRailSection';

interface MovieSection {
  key: string;
  title: string;
  description: string;
  contentType: SectionContentType;
  tag: string | null;
  items: SectionContentItem[];
}

const PERSONALIZED_MOVIES: SectionContentItem[] = [
  {
    contentId: 'personal-movie-1',
    title: 'Interstellar',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.9,
    tags: ['SF', '드라마'],
  },
  {
    contentId: 'personal-movie-2',
    title: 'Dune: Part Two',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.8,
    tags: ['SF', '모험'],
  },
  {
    contentId: 'personal-movie-3',
    title: 'Arrival',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.7,
    tags: ['SF', '드라마'],
  },
  {
    contentId: 'personal-movie-4',
    title: 'Blade Runner 2049',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.6,
    tags: ['SF', '미스터리'],
  },
  {
    contentId: 'personal-movie-5',
    title: 'The Martian',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.6,
    tags: ['SF', '우주'],
  },
];

const NEW_MOVIES: SectionContentItem[] = [
  {
    contentId: 'movie-new-1',
    title: 'Dune: Part Two',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.8,
    tags: ['SF', '모험'],
    badge: '최근 추가',
  },
  {
    contentId: 'movie-new-2',
    title: 'Civil War',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.4,
    tags: ['액션', '드라마'],
    badge: '최근 추가',
  },
  {
    contentId: 'movie-new-3',
    title: 'Challengers',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.5,
    tags: ['드라마', '스포츠'],
    badge: '최근 추가',
  },
  {
    contentId: 'movie-new-4',
    title: 'Furiosa',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.6,
    tags: ['액션', '모험'],
    badge: '최근 추가',
  },
  {
    contentId: 'movie-new-5',
    title: 'Poor Things',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.5,
    tags: ['판타지', '드라마'],
    badge: '최근 추가',
  },
];

const SF_MOVIES: SectionContentItem[] = [
  {
    contentId: 'movie-sf-1',
    title: 'Interstellar',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.9,
    tags: ['SF', '우주'],
  },
  {
    contentId: 'movie-sf-2',
    title: 'Dune: Part Two',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.8,
    tags: ['SF', '모험'],
  },
  {
    contentId: 'movie-sf-3',
    title: 'Arrival',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.7,
    tags: ['SF', '드라마'],
  },
  {
    contentId: 'movie-sf-4',
    title: 'Blade Runner 2049',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.6,
    tags: ['SF', '미스터리'],
  },
  {
    contentId: 'movie-sf-5',
    title: 'The Martian',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.6,
    tags: ['SF', '우주'],
  },
];

const ACTION_MOVIES: SectionContentItem[] = [
  {
    contentId: 'movie-action-1',
    title: 'Mad Max: Fury Road',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.8,
    tags: ['액션', '모험'],
  },
  {
    contentId: 'movie-action-2',
    title: 'John Wick',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.7,
    tags: ['액션', '범죄'],
  },
  {
    contentId: 'movie-action-3',
    title: 'Top Gun: Maverick',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.7,
    tags: ['액션', '드라마'],
  },
  {
    contentId: 'movie-action-4',
    title: 'Mission: Impossible',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.6,
    tags: ['액션', '스파이'],
  },
  {
    contentId: 'movie-action-5',
    title: 'Furiosa',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.6,
    tags: ['액션', '모험'],
  },
];

const THRILLER_MOVIES: SectionContentItem[] = [
  {
    contentId: 'movie-thriller-1',
    title: 'Gone Girl',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.8,
    tags: ['스릴러', '미스터리'],
  },
  {
    contentId: 'movie-thriller-2',
    title: 'Prisoners',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.7,
    tags: ['스릴러', '범죄'],
  },
  {
    contentId: 'movie-thriller-3',
    title: 'Shutter Island',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.7,
    tags: ['스릴러', '미스터리'],
  },
  {
    contentId: 'movie-thriller-4',
    title: 'Nightcrawler',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.6,
    tags: ['스릴러', '범죄'],
  },
  {
    contentId: 'movie-thriller-5',
    title: 'Zodiac',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.6,
    tags: ['스릴러', '범죄'],
  },
];

const MOCK_MOVIE_SECTIONS: MovieSection[] = [
  {
    key: 'PERSONALIZED_MOVIE',
    title: 'OO님을 위한 영화',
    description:
      '시청 기록과 리뷰 취향을 바탕으로 고른 영화예요.',
    contentType: 'MOVIE',
    tag: null,
    items: PERSONALIZED_MOVIES,
  },
  {
    key: 'NEW_MOVIE',
    title: '새로 추가된 영화',
    description:
      '최근 MOPL에 추가된 영화를 확인해보세요.',
    contentType: 'MOVIE',
    tag: null,
    items: NEW_MOVIES,
  },
  {
    key: 'PREFERENCE_MOVIE_SF',
    title: 'SF 영화 TOP 10',
    description:
      'MOPL에서 인기 있는 SF 영화를 모아봤어요.',
    contentType: 'MOVIE',
    tag: 'SF',
    items: SF_MOVIES,
  },
  {
    key: 'PREFERENCE_MOVIE_ACTION',
    title: '액션 영화 TOP 10',
    description:
      '빠른 전개와 강렬한 액션이 돋보이는 영화예요.',
    contentType: 'MOVIE',
    tag: '액션',
    items: ACTION_MOVIES,
  },
  {
    key: 'PREFERENCE_MOVIE_THRILLER',
    title: '스릴러 영화 TOP 10',
    description:
      '긴장감 있는 인기 스릴러 영화를 모아봤어요.',
    contentType: 'MOVIE',
    tag: '스릴러',
    items: THRILLER_MOVIES,
  },
];

export default function MovieTab() {
  return (
    <div className="flex min-w-0 flex-col gap-12">
      {MOCK_MOVIE_SECTIONS.map((section) => (
        <ContentRailSection
          key={section.key}
          title={section.title}
          description={section.description}
          contentType={section.contentType}
          tag={section.tag}
          items={section.items}
        />
      ))}
    </div>
  );
}