import ContentRailSection, {
  type SectionContentItem,
  type SectionContentType,
} from './ContentRailSection';

interface SportSection {
  key: string;
  title: string;
  description: string;
  contentType: SectionContentType;
  tag: string | null;
  items: SectionContentItem[];
}

const PERSONALIZED_SPORTS: SectionContentItem[] = [
  {
    contentId: 'personal-sport-1',
    title: 'Formula 1',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.9,
    tags: ['F1', '레이싱'],
  },
  {
    contentId: 'personal-sport-2',
    title: 'Premier League',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.8,
    tags: ['축구', '프리미어리그'],
  },
  {
    contentId: 'personal-sport-3',
    title: 'Champions League',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.9,
    tags: ['축구', '챔피언스리그'],
  },
  {
    contentId: 'personal-sport-4',
    title: 'NBA',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.7,
    tags: ['농구', 'NBA'],
  },
  {
    contentId: 'personal-sport-5',
    title: 'MotoGP',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.6,
    tags: ['모터스포츠', '레이싱'],
  },
];

const NEW_SPORTS: SectionContentItem[] = [
  {
    contentId: 'sport-new-1',
    title: 'Formula 1',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.9,
    tags: ['F1', '레이싱'],
    badge: '최근 추가',
  },
  {
    contentId: 'sport-new-2',
    title: 'Premier League',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.8,
    tags: ['축구', '프리미어리그'],
    badge: '최근 추가',
  },
  {
    contentId: 'sport-new-3',
    title: 'NBA',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.7,
    tags: ['농구', 'NBA'],
    badge: '최근 추가',
  },
  {
    contentId: 'sport-new-4',
    title: 'MLB',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.6,
    tags: ['야구', 'MLB'],
    badge: '최근 추가',
  },
  {
    contentId: 'sport-new-5',
    title: 'MotoGP',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.6,
    tags: ['레이싱', '모터스포츠'],
    badge: '최근 추가',
  },
];

const FOOTBALL_CONTENTS: SectionContentItem[] = [
  {
    contentId: 'sport-football-1',
    title: 'Premier League',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.9,
    tags: ['축구', '프리미어리그'],
  },
  {
    contentId: 'sport-football-2',
    title: 'Champions League',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.9,
    tags: ['축구', '챔피언스리그'],
  },
  {
    contentId: 'sport-football-3',
    title: 'La Liga',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.7,
    tags: ['축구', '라리가'],
  },
  {
    contentId: 'sport-football-4',
    title: 'Serie A',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.6,
    tags: ['축구', '세리에A'],
  },
  {
    contentId: 'sport-football-5',
    title: 'Bundesliga',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.6,
    tags: ['축구', '분데스리가'],
  },
];

const BASKETBALL_CONTENTS: SectionContentItem[] = [
  {
    contentId: 'sport-basketball-1',
    title: 'NBA',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.9,
    tags: ['농구', 'NBA'],
  },
  {
    contentId: 'sport-basketball-2',
    title: 'EuroLeague',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.7,
    tags: ['농구', '유로리그'],
  },
  {
    contentId: 'sport-basketball-3',
    title: 'WNBA',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.6,
    tags: ['농구', 'WNBA'],
  },
  {
    contentId: 'sport-basketball-4',
    title: 'KBL',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.5,
    tags: ['농구', 'KBL'],
  },
  {
    contentId: 'sport-basketball-5',
    title: 'NCAA Basketball',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.5,
    tags: ['농구', 'NCAA'],
  },
];

const MOTORSPORT_CONTENTS: SectionContentItem[] = [
  {
    contentId: 'sport-motorsport-1',
    title: 'Formula 1',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.9,
    tags: ['F1', '레이싱'],
  },
  {
    contentId: 'sport-motorsport-2',
    title: 'MotoGP',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.8,
    tags: ['모터스포츠', '레이싱'],
  },
  {
    contentId: 'sport-motorsport-3',
    title: 'WEC',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.7,
    tags: ['내구레이스', '레이싱'],
  },
  {
    contentId: 'sport-motorsport-4',
    title: 'IndyCar',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.6,
    tags: ['레이싱', '인디카'],
  },
  {
    contentId: 'sport-motorsport-5',
    title: 'WRC',
    thumbnailUrl: '/placeholder-movie.png',
    type: 'SPORT',
    rating: 4.6,
    tags: ['랠리', '레이싱'],
  },
];

const MOCK_SPORT_SECTIONS: SportSection[] = [
  {
    key: 'PERSONALIZED_SPORT',
    title: 'OO님을 위한 스포츠',
    description:
      '자주 보는 종목과 콘텐츠를 바탕으로 골라봤어요.',
    contentType: 'SPORT',
    tag: null,
    items: PERSONALIZED_SPORTS,
  },
  {
    key: 'NEW_SPORT',
    title: '새로 추가된 스포츠',
    description:
      '최근 MOPL에 추가된 스포츠 콘텐츠예요.',
    contentType: 'SPORT',
    tag: null,
    items: NEW_SPORTS,
  },
  {
    key: 'PREFERENCE_SPORT_FOOTBALL',
    title: '축구 콘텐츠 TOP 10',
    description:
      'MOPL에서 인기 있는 축구 콘텐츠를 모아봤어요.',
    contentType: 'SPORT',
    tag: '축구',
    items: FOOTBALL_CONTENTS,
  },
  {
    key: 'PREFERENCE_SPORT_BASKETBALL',
    title: '농구 콘텐츠 TOP 10',
    description:
      '인기 있는 농구 리그와 콘텐츠를 확인해보세요.',
    contentType: 'SPORT',
    tag: '농구',
    items: BASKETBALL_CONTENTS,
  },
  {
    key: 'PREFERENCE_SPORT_MOTORSPORT',
    title: '모터스포츠 TOP 10',
    description:
      '빠른 속도와 경쟁을 즐길 수 있는 모터스포츠 콘텐츠예요.',
    contentType: 'SPORT',
    tag: '모터스포츠',
    items: MOTORSPORT_CONTENTS,
  },
];

export default function SportTab() {
  return (
    <div className="flex min-w-0 flex-col gap-12">
      {MOCK_SPORT_SECTIONS.map((section) => (
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