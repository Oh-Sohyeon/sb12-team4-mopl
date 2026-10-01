import RecommendationSection from './RecommendationSection';
import ContentRailSection, {
  type SectionContentItem,
  type SectionContentType,
} from './ContentRailSection';

interface HomeSection {
  key: string;
  title: string;
  description: string;
  contentType: SectionContentType | null;
  tag: string | null;
  items: SectionContentItem[];
}

const PERSONALIZED_NEW_ITEMS: SectionContentItem[] = [
  {
    contentId: 'home-new-1',
    title: 'Dune: Part Two',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.8,
    tags: ['SF', '모험'],
    badge: '최근 추가',
  },
  {
    contentId: 'home-new-2',
    title: 'Severance',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.7,
    tags: ['미스터리', 'SF'],
    badge: '최근 추가',
  },
  {
    contentId: 'home-new-3',
    title: 'Formula 1',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.6,
    tags: ['F1', '레이싱'],
    badge: '최근 추가',
  },
  {
    contentId: 'home-new-4',
    title: 'The Last of Us',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.7,
    tags: ['드라마', '액션'],
    badge: '최근 추가',
  },
  {
    contentId: 'home-new-5',
    title: 'Interstellar',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.9,
    tags: ['SF', '우주'],
    badge: '최근 추가',
  },
];

const SF_MOVIES: SectionContentItem[] = [
  {
    contentId: 'home-sf-1',
    title: 'Interstellar',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.9,
    tags: ['SF', '우주'],
  },
  {
    contentId: 'home-sf-2',
    title: 'Dune: Part Two',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.8,
    tags: ['SF', '모험'],
  },
  {
    contentId: 'home-sf-3',
    title: 'Arrival',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.7,
    tags: ['SF', '드라마'],
  },
  {
    contentId: 'home-sf-4',
    title: 'Blade Runner 2049',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.6,
    tags: ['SF', '미스터리'],
  },
  {
    contentId: 'home-sf-5',
    title: 'The Martian',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'MOVIE',
    rating: 4.6,
    tags: ['SF', '우주'],
  },
];

const FOOTBALL_CONTENTS: SectionContentItem[] = [
  {
    contentId: 'home-football-1',
    title: 'Premier League',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.8,
    tags: ['축구', '프리미어리그'],
  },
  {
    contentId: 'home-football-2',
    title: 'UEFA Champions League',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.9,
    tags: ['축구', '챔피언스리그'],
  },
  {
    contentId: 'home-football-3',
    title: 'La Liga',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.6,
    tags: ['축구', '라리가'],
  },
  {
    contentId: 'home-football-4',
    title: 'Serie A',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.5,
    tags: ['축구', '세리에A'],
  },
  {
    contentId: 'home-football-5',
    title: 'Bundesliga',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.5,
    tags: ['축구', '분데스리가'],
  },
];

const MYSTERY_TV_CONTENTS: SectionContentItem[] = [
  {
    contentId: 'home-mystery-1',
    title: 'Severance',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.8,
    tags: ['미스터리', 'SF'],
  },
  {
    contentId: 'home-mystery-2',
    title: 'Dark',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.8,
    tags: ['미스터리', 'SF'],
  },
  {
    contentId: 'home-mystery-3',
    title: 'True Detective',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.7,
    tags: ['미스터리', '범죄'],
  },
  {
    contentId: 'home-mystery-4',
    title: 'From',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.5,
    tags: ['미스터리', '공포'],
  },
  {
    contentId: 'home-mystery-5',
    title: 'The OA',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'TV_SERIES',
    rating: 4.4,
    tags: ['미스터리', '드라마'],
  },
];

/**
 * 추후 GET /api/recommendations/sections?tab=HOME 응답으로 교체될 mock 데이터.
 *
 * 현재 SF / 축구 / 미스터리와 OO님은 UI 확인용 placeholder이며,
 * 실제 연결 시 백엔드의 title, contentType, tag, items를 사용한다.
 */
const MOCK_HOME_SECTIONS: HomeSection[] = [
  {
    key: 'PERSONALIZED_NEW',
    title: '새로 들어온 콘텐츠 중 OO님 취향',
    description:
      '최근 MOPL에 추가된 콘텐츠 중 취향에 맞는 작품이에요.',
    contentType: null,
    tag: null,
    items: PERSONALIZED_NEW_ITEMS,
  },
  {
    key: 'PREFERENCE_TOP_MOVIE',
    title: 'OO님이 좋아하는 SF 영화 TOP 10',
    description:
      '시청 기록과 리뷰를 바탕으로 선호하는 영화 태그를 골랐어요.',
    contentType: 'MOVIE',
    tag: 'SF',
    items: SF_MOVIES,
  },
  {
    key: 'PREFERENCE_TOP_SPORT',
    title: 'OO님이 자주 보는 축구 콘텐츠 TOP 10',
    description:
      '시청 기록과 리뷰를 바탕으로 선호하는 스포츠 태그를 골랐어요.',
    contentType: 'SPORT',
    tag: '축구',
    items: FOOTBALL_CONTENTS,
  },
  {
    key: 'PREFERENCE_TOP_TV',
    title: 'OO님 취향의 미스터리 TV 시리즈 TOP 10',
    description:
      '시청 기록과 리뷰를 바탕으로 선호하는 TV 시리즈 태그를 골랐어요.',
    contentType: 'TV_SERIES',
    tag: '미스터리',
    items: MYSTERY_TV_CONTENTS,
  },
];

export default function HomeTab() {
  return (
    <div className="flex min-w-0 flex-col gap-12">
      <RecommendationSection />

      {MOCK_HOME_SECTIONS.map((section) => (
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