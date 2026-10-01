import ContentRailSection, {
  type SectionContentItem,
  type SectionContentType,
} from './ContentRailSection';

interface TvSeriesSection {
  key: string;
  title: string;
  description: string;
  contentType: SectionContentType;
  tag: string | null;
  items: SectionContentItem[];
}

const PERSONALIZED_TV: SectionContentItem[] = [
  {
    contentId: 'personal-tv-1',
    title: 'Severance',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.8,
    tags: ['미스터리', 'SF'],
  },
  {
    contentId: 'personal-tv-2',
    title: 'The Last of Us',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.7,
    tags: ['드라마', '액션'],
  },
  {
    contentId: 'personal-tv-3',
    title: 'Dark',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.8,
    tags: ['미스터리', 'SF'],
  },
  {
    contentId: 'personal-tv-4',
    title: 'Shogun',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.8,
    tags: ['드라마', '역사'],
  },
  {
    contentId: 'personal-tv-5',
    title: 'Fallout',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.6,
    tags: ['SF', '액션'],
  },
];

const NEW_TV_SERIES: SectionContentItem[] = [
  {
    contentId: 'tv-new-1',
    title: 'Shogun',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.8,
    tags: ['드라마', '역사'],
    badge: '최근 추가',
  },
  {
    contentId: 'tv-new-2',
    title: 'Fallout',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.6,
    tags: ['SF', '액션'],
    badge: '최근 추가',
  },
  {
    contentId: 'tv-new-3',
    title: 'The Bear',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.7,
    tags: ['드라마', '코미디'],
    badge: '최근 추가',
  },
  {
    contentId: 'tv-new-4',
    title: '3 Body Problem',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.5,
    tags: ['SF', '미스터리'],
    badge: '최근 추가',
  },
  {
    contentId: 'tv-new-5',
    title: 'Ripley',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.5,
    tags: ['드라마', '범죄'],
    badge: '최근 추가',
  },
];

const MYSTERY_TV: SectionContentItem[] = [
  {
    contentId: 'tv-mystery-1',
    title: 'Severance',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.8,
    tags: ['미스터리', 'SF'],
  },
  {
    contentId: 'tv-mystery-2',
    title: 'Dark',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.8,
    tags: ['미스터리', 'SF'],
  },
  {
    contentId: 'tv-mystery-3',
    title: 'True Detective',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.7,
    tags: ['미스터리', '범죄'],
  },
  {
    contentId: 'tv-mystery-4',
    title: 'From',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.5,
    tags: ['미스터리', '공포'],
  },
  {
    contentId: 'tv-mystery-5',
    title: 'The OA',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.4,
    tags: ['미스터리', '드라마'],
  },
];

const SF_TV: SectionContentItem[] = [
  {
    contentId: 'tv-sf-1',
    title: 'Severance',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.8,
    tags: ['SF', '미스터리'],
  },
  {
    contentId: 'tv-sf-2',
    title: 'Dark',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.8,
    tags: ['SF', '미스터리'],
  },
  {
    contentId: 'tv-sf-3',
    title: 'Black Mirror',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.7,
    tags: ['SF', '스릴러'],
  },
  {
    contentId: 'tv-sf-4',
    title: 'Fallout',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.6,
    tags: ['SF', '액션'],
  },
  {
    contentId: 'tv-sf-5',
    title: '3 Body Problem',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.5,
    tags: ['SF', '미스터리'],
  },
];

const CRIME_TV: SectionContentItem[] = [
  {
    contentId: 'tv-crime-1',
    title: 'True Detective',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.8,
    tags: ['범죄', '미스터리'],
  },
  {
    contentId: 'tv-crime-2',
    title: 'Mindhunter',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.8,
    tags: ['범죄', '스릴러'],
  },
  {
    contentId: 'tv-crime-3',
    title: 'Narcos',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.7,
    tags: ['범죄', '드라마'],
  },
  {
    contentId: 'tv-crime-4',
    title: 'Ozark',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.6,
    tags: ['범죄', '드라마'],
  },
  {
    contentId: 'tv-crime-5',
    title: 'Ripley',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.5,
    tags: ['범죄', '드라마'],
  },
];

const MOCK_TV_SECTIONS: TvSeriesSection[] = [
  {
    key: 'PERSONALIZED_TV',
    title: 'OO님을 위한 TV 시리즈',
    description:
      '시청 기록과 리뷰 취향을 바탕으로 고른 TV 시리즈예요.',
    contentType: 'TV_SERIES',
    tag: null,
    items: PERSONALIZED_TV,
  },
  {
    key: 'NEW_TV_SERIES',
    title: '새로 추가된 TV 시리즈',
    description:
      '최근 MOPL에 추가된 TV 시리즈를 확인해보세요.',
    contentType: 'TV_SERIES',
    tag: null,
    items: NEW_TV_SERIES,
  },
  {
    key: 'PREFERENCE_TV_MYSTERY',
    title: '미스터리 TV 시리즈 TOP 10',
    description:
      '인기 있는 미스터리 TV 시리즈를 모아봤어요.',
    contentType: 'TV_SERIES',
    tag: '미스터리',
    items: MYSTERY_TV,
  },
  {
    key: 'PREFERENCE_TV_SF',
    title: 'SF TV 시리즈 TOP 10',
    description:
      '상상력을 자극하는 인기 SF 시리즈예요.',
    contentType: 'TV_SERIES',
    tag: 'SF',
    items: SF_TV,
  },
  {
    key: 'PREFERENCE_TV_CRIME',
    title: '범죄 TV 시리즈 TOP 10',
    description:
      '몰입감 높은 범죄 TV 시리즈를 모아봤어요.',
    contentType: 'TV_SERIES',
    tag: '범죄',
    items: CRIME_TV,
  },
];

export default function TvSeriesTab() {
  return (
    <div className="flex min-w-0 flex-col gap-12">
      {MOCK_TV_SECTIONS.map((section) => (
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