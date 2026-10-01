import {
  useCallback,
  useEffect,
  useMemo,
  useState,
} from 'react';
import { useInView } from 'react-intersection-observer';

import useContentStore from '@/lib/stores/useContentStore';
import type { ContentDto } from '@/lib/types';

import ContentGrid from './components/ContentGrid';
import FilterTabs, { type ContentsTab } from './components/FilterTabs';
import HomeTab from './components/HomeTab';
import MovieTab from './components/MovieTab';
import NewTab from './components/NewTab';
import SearchBar from './components/SearchBar';
import SortDropdown, { type SortOption } from './components/SortDropdown';
import SportTab from './components/SportTab';
import TvSeriesTab from './components/TvSeriesTab';
// [추가] 디자인 검증용 하단 전체 목록 mock
import {
  MOCK_CONTENTS,
  MOCK_NEW_CONTENT_IDS,
} from './mocks/contentMocks';

// [추가] 현재는 프론트 UI 검증 단계이므로 true.
// 실제 백엔드 연결 단계에서 false로 바꾸는 것이 아니라,
// 이 상수와 mock 관련 분기를 최종적으로 제거한다.
const USE_MOCK_DATA = true;

const TAB_LIST_TITLE: Record<ContentsTab, string> = {
  home: '전체 콘텐츠',
  new: '새로 추가된 콘텐츠 전체',
  movie: '영화 - 전체',
  tvSeries: 'TV 시리즈 - 전체',
  sport: '스포츠 - 전체',
};

const TAB_LIST_DESCRIPTION: Partial<Record<ContentsTab, string>> = {
  home: 'MOPL의 모든 콘텐츠를 둘러보세요.',
  new: '최근 MOPL에 새로 추가된 콘텐츠를 확인해보세요.',
  movie: 'MOPL에 등록된 영화를 둘러보세요.',
  tvSeries: 'MOPL에 등록된 TV 시리즈를 둘러보세요.',
  sport: 'MOPL에 등록된 스포츠 콘텐츠를 둘러보세요.',
};

export default function ContentsPage() {
  const {
    data,
    loading,
    fetchMore,
    hasNext,
    updateParams,
  } = useContentStore();

  const [selectedTab, setSelectedTab] = useState<ContentsTab>('home');
  const [sortValue, setSortValue] = useState('popular');
  const [searchQuery, setSearchQuery] = useState('');

  const normalizedSearchQuery = searchQuery.trim();
  const isSearchMode = normalizedSearchQuery.length > 0;

  const {
    ref: sentinelRef,
    inView,
  } = useInView({
    threshold: 0,
    rootMargin: '100px',
  });

  // [수정] mock 사용 중에는 실제 cursor pagination API를 호출하지 않는다.
  useEffect(() => {
    if (USE_MOCK_DATA) return;

    if (inView && hasNext() && !loading) {
      fetchMore();
    }
  }, [
    inView,
    hasNext,
    loading,
    fetchMore,
  ]);

  const applyTabParams = useCallback(
    (tab: ContentsTab) => {
      if (tab === 'home') {
        setSortValue('popular');

        // [추가] mock UI 확인 중에는 실제 API 요청 방지
        if (USE_MOCK_DATA) return;

        updateParams({
          keywordLike: undefined,
          typeEqual: undefined,
          sortBy: 'watcherCount',
          sortDirection: 'DESCENDING',
        });
        return;
      }

      if (tab === 'new') {
        setSortValue('latest');

        // [추가] mock UI 확인 중에는 실제 API 요청 방지
        if (USE_MOCK_DATA) return;

        updateParams({
          keywordLike: undefined,
          typeEqual: undefined,
          sortBy: 'createdAt',
          sortDirection: 'DESCENDING',
        });
        return;
      }

      setSortValue('popular');

      // [추가] mock UI 확인 중에는 실제 API 요청 방지
      if (USE_MOCK_DATA) return;

      updateParams({
        keywordLike: undefined,
        typeEqual: tab,
        sortBy: 'watcherCount',
        sortDirection: 'DESCENDING',
      });
    },
    [updateParams],
  );

  const handleTabChange = useCallback(
    (tab: ContentsTab) => {
      setSelectedTab(tab);

      if (!normalizedSearchQuery) {
        applyTabParams(tab);
      }
    },
    [
      applyTabParams,
      normalizedSearchQuery,
    ],
  );

  const handleSearch = useCallback(
    (query: string) => {
      const normalizedQuery = query.trim();

      setSearchQuery(query);

      // [추가] mock에서는 아래 useMemo가 검색 결과를 직접 만든다.
      if (USE_MOCK_DATA) return;

      if (normalizedQuery) {
        updateParams({
          keywordLike: normalizedQuery,
          typeEqual: undefined,
        });
        return;
      }

      applyTabParams(selectedTab);
    },
    [
      applyTabParams,
      selectedTab,
      updateParams,
    ],
  );

  const handleSortChange = useCallback(
    (option: SortOption) => {
      const nextSortValue =
        option.sortBy === 'createdAt'
          ? 'latest'
          : option.sortBy === 'watcherCount'
            ? 'popular'
            : 'rating';

      setSortValue(nextSortValue);

      // [추가] mock에서는 아래 useMemo가 정렬을 담당한다.
      if (USE_MOCK_DATA) return;

      updateParams({
        sortBy: option.sortBy,
        sortDirection: option.sortDirection,
      });
    },
    [updateParams],
  );

  // [추가] 실제 ContentDto와 같은 타입을 사용한 하단 mock 목록.
  // 검색 중에는 탭과 관계없이 MOPL 전체 콘텐츠에서 검색한다.
  const mockContents = useMemo<ContentDto[]>(() => {
    let contents = [...MOCK_CONTENTS];

    if (isSearchMode) {
      const keyword = normalizedSearchQuery.toLowerCase();

      contents = contents.filter((content) => {
        const searchableText = [
          content.title,
          content.description,
          ...content.tags,
        ]
          .join(' ')
          .toLowerCase();

        return searchableText.includes(keyword);
      });
    } else {
      switch (selectedTab) {
        case 'new':
          contents = contents.filter((content) =>
            MOCK_NEW_CONTENT_IDS.has(content.id),
          );
          break;

        case 'movie':
          contents = contents.filter(
            (content) => content.type === 'movie',
          );
          break;

        case 'tvSeries':
          contents = contents.filter(
            (content) => content.type === 'tvSeries',
          );
          break;

        case 'sport':
          contents = contents.filter(
            (content) => content.type === 'sport',
          );
          break;

        case 'home':
        default:
          break;
      }
    }

    if (sortValue === 'popular') {
      return contents.sort(
        (a, b) => b.watcherCount - a.watcherCount,
      );
    }

    if (sortValue === 'rating') {
      return contents.sort(
        (a, b) => b.averageRating - a.averageRating,
      );
    }

    // [추가] ContentDto에는 createdAt이 없으므로
    // latest mock은 MOCK_CONTENTS / MOCK_NEW_CONTENT_IDS의 작성 순서를 사용한다.
    return contents;
  }, [
    isSearchMode,
    normalizedSearchQuery,
    selectedTab,
    sortValue,
  ]);

  // [추가] mock과 실제 서버 데이터를 동일한 ContentGrid에 전달한다.
  const displayedContents = USE_MOCK_DATA
    ? mockContents
    : data;

  const displayedLoading = USE_MOCK_DATA
    ? false
    : loading;

  const displayedHasNext = USE_MOCK_DATA
    ? false
    : hasNext();

  const listTitle = isSearchMode
    ? `'${normalizedSearchQuery}' 검색 결과`
    : TAB_LIST_TITLE[selectedTab];

  const listDescription = isSearchMode
    ? 'MOPL 전체 콘텐츠에서 검색한 결과예요.'
    : TAB_LIST_DESCRIPTION[selectedTab];

  return (
    <div
      className="
        flex
        min-w-0
        flex-col
        gap-9
        px-[70px]
        py-10
      "
    >
      <header className="flex min-w-0 flex-col gap-6">
        <div
          className="
            flex
            flex-wrap
            items-center
            justify-between
            gap-5
          "
        >
          <h1 className="text-header1-b text-white">
            콘텐츠 같이 보기
          </h1>

          <SearchBar
            onSearch={handleSearch}
            placeholder="MOPL 전체 콘텐츠 통합 검색"
          />
        </div>

        <FilterTabs
          selectedTab={selectedTab}
          onTabChange={handleTabChange}
        />

        <div className="h-px w-full bg-gray-900" />
      </header>

      {!isSearchMode && selectedTab === 'home' && (
        <HomeTab />
      )}

      {!isSearchMode && selectedTab === 'new' && (
        <NewTab />
      )}

      {!isSearchMode && selectedTab === 'movie' && (
        <MovieTab />
      )}

      {!isSearchMode && selectedTab === 'tvSeries' && (
        <TvSeriesTab />
      )}

      {!isSearchMode && selectedTab === 'sport' && (
        <SportTab />
      )}

      <section
        className="
          flex
          min-w-0
          flex-col
          gap-7
          border-t
          border-gray-900
          pt-9
        "
      >
        <div
          className="
            flex
            flex-wrap
            items-end
            justify-between
            gap-4
          "
        >
          <div>
            <h2 className="text-title1-b text-white">
              {listTitle}
            </h2>

            {listDescription && (
              <p className="mt-2 text-body3-m text-gray-500">
                {listDescription}
              </p>
            )}
          </div>

          <SortDropdown
            value={sortValue}
            onValueChange={handleSortChange}
          />
        </div>

        <ContentGrid
          contents={displayedContents}
          loading={displayedLoading}
        />

        {!displayedLoading && displayedHasNext && (
          <div
            ref={sentinelRef}
            className="flex h-10 items-center justify-center"
          />
        )}

        {displayedLoading && (
          <div className="flex justify-center py-4">
            <div
              className="
                h-8
                w-8
                animate-spin
                rounded-full
                border-4
                border-gray-700
                border-t-pink-500
              "
            />
          </div>
        )}
      </section>
    </div>
  );
}