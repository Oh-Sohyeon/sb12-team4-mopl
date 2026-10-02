package com.mopl.playlist.ai.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.common.exception.CommonErrorCode;
import com.mopl.common.exception.MoplException;
import com.mopl.content.dto.ContentListItemResponse;
import com.mopl.content.dto.ContentResponse;
import com.mopl.content.dto.ContentSearchCondition;
import com.mopl.content.repository.ContentTagRepository;
import com.mopl.content.search.condition.ContentTagCondition;
import com.mopl.content.search.condition.SemanticCandidateCondition;
import com.mopl.content.search.dto.ContentCandidate;
import com.mopl.content.search.dto.ContentTagDto;
import com.mopl.content.search.service.SemanticCandidateSearchService;
import com.mopl.content.service.ContentService;
import com.mopl.core.common.dto.CursorResponse;
import com.mopl.core.common.enums.ContentType;
import com.mopl.core.domain.content.entity.ContentTag;
import com.mopl.playlist.ai.service.AiPlaylistCandidateStore;
import com.mopl.playlist.dto.PlaylistResponse;
import com.mopl.playlist.service.PlaylistService;
import com.mopl.recommendation.service.RecommendationService;
import com.mopl.review.dto.ReviewListResponse;
import com.mopl.review.repository.ReviewRepository;
import com.mopl.review.service.ReviewService;
import com.mopl.watchingsession.dto.WatchingSessionResponse;
import com.mopl.watchingsession.service.WatchingSessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiPlaylistQueryToolsTest {
    @Mock private PlaylistService playlistService;
    @Mock private SemanticCandidateSearchService semanticSearchService;
    @Mock private AiPlaylistCandidateStore candidateStore;
    @Mock private RecommendationService recommendationService;
    @Mock private ContentService contentService;
    @Mock private ContentTagRepository contentTagRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ReviewService reviewService;
    @Mock private WatchingSessionService watchingSessionService;
    @InjectMocks private AiPlaylistTools tools;

    private final UUID currentUserId = UUID.randomUUID();
    private final UUID contentId = UUID.randomUUID();
    private final ToolContext context = new ToolContext(Map.of(
        "currentUserId", currentUserId, "sessionId", "query-session"));

    @Test
    void defaultSearchStillUsesSemanticTenAndRegistersCandidates() {
        ContentCandidate candidate = mock(ContentCandidate.class);
        when(candidate.contentId()).thenReturn(contentId);
        when(semanticSearchService.search(eq("비 오는 날"), any(), eq(10)))
            .thenReturn(List.of(candidate));

        var result = tools.searchContents("비 오는 날", null, null, context);

        assertEquals(AiPlaylistTools.SearchMode.SEMANTIC, result.mode());
        assertEquals(List.of(candidate), result.semanticResults());
        assertNull(result.keywordResults());
        verify(candidateStore).saveCandidates(currentUserId, "query-session", Set.of(contentId));
        verifyNoInteractions(contentService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void semanticSearchRejectsMissingOrBlankQuery(String queryText) {
        var defaultOptions = new AiPlaylistTools.ContentSearchOptions(
            null, null, null, null, null, null, null, null);
        var semanticOptions = new AiPlaylistTools.ContentSearchOptions(
            AiPlaylistTools.SearchMode.SEMANTIC, null, null, null, null, null, null, null);

        for (var options : Arrays.asList(null, defaultOptions, semanticOptions)) {
            MoplException exception = assertThrows(MoplException.class,
                () -> tools.searchContents(queryText, null, options, context));
            assertEquals(CommonErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
        }
        verifyNoInteractions(semanticSearchService, contentService, candidateStore);
    }

    @Test
    void keywordSearchAllowsNullQueryForListing() {
        var response = CursorResponse.<ContentListItemResponse>of(List.of(), null, null,
            false, 0, "createdAt", "DESCENDING");
        when(contentService.getContents(any(), isNull(), isNull(), eq(20),
            eq("createdAt"), eq("DESCENDING"))).thenReturn(response);
        var options = new AiPlaylistTools.ContentSearchOptions(
            AiPlaylistTools.SearchMode.KEYWORD, null, null, null, null, null, null, null);

        var result = tools.searchContents(null, null, options, context);

        assertEquals(AiPlaylistTools.SearchMode.KEYWORD, result.mode());
        assertSame(response, result.keywordResults());
        verify(contentService).getContents(new ContentSearchCondition(null, null, null),
            null, null, 20, "createdAt", "DESCENDING");
        verifyNoInteractions(semanticSearchService);
    }

    @Test
    void semanticSearchPassesActualTagPairsTypeAndLimit() {
        var tags = List.of(new ContentTagCondition("genre", "Drama"));
        var options = new AiPlaylistTools.ContentSearchOptions(
            AiPlaylistTools.SearchMode.SEMANTIC, null, tags, 12, null, null, null, null);
        when(semanticSearchService.search(anyString(), any(), anyInt())).thenReturn(List.of());

        tools.searchContents("따뜻한 영화", ContentType.MOVIE, options, context);

        verify(semanticSearchService).search("따뜻한 영화",
            new SemanticCandidateCondition(ContentType.MOVIE, tags), 12);
    }

    @Test
    void keywordSearchPassesFiltersSortCursorAndRegistersIds() {
        UUID idAfter = UUID.randomUUID();
        var item = new ContentListItemResponse(contentId, ContentType.MOVIE, "영화", "설명",
            null, List.of("Drama"), 4.2, 5, 10);
        var response = CursorResponse.of(List.of(item), "next", contentId.toString(),
            true, 30, "rate", "DESCENDING");
        when(contentService.getContents(any(), any(), any(), anyInt(), any(), any()))
            .thenReturn(response);
        var options = new AiPlaylistTools.ContentSearchOptions(
            AiPlaylistTools.SearchMode.KEYWORD, List.of("Drama"), null,
            20, "rate", "DESCENDING", "4.5", idAfter);

        var result = tools.searchContents("영화", ContentType.MOVIE, options, context);

        assertSame(response, result.keywordResults());
        assertTrue(result.semanticResults().isEmpty());
        verify(contentService).getContents(new ContentSearchCondition(ContentType.MOVIE,
            "영화", List.of("Drama")), "4.5", idAfter, 20, "rate", "DESCENDING");
        verify(candidateStore).saveCandidates(currentUserId, "query-session", Set.of(contentId));
        verifyNoInteractions(semanticSearchService);
    }

    @Test
    void unsupportedSemanticSortingIsRejectedInsteadOfIgnored() {
        var options = new AiPlaylistTools.ContentSearchOptions(
            null, null, null, null, "rate", null, null, null);
        assertThrows(MoplException.class,
            () -> tools.searchContents("영화", null, options, context));
        verifyNoInteractions(semanticSearchService, contentService, candidateStore);
    }

    @Test
    void keywordSearchRejectsSemanticTagPairs() {
        var options = new AiPlaylistTools.ContentSearchOptions(
            AiPlaylistTools.SearchMode.KEYWORD, null,
            List.of(new ContentTagCondition("genre", "Drama")), null, null, null, null, null);
        assertThrows(MoplException.class,
            () -> tools.searchContents("영화", null, options, context));
        verifyNoInteractions(contentService, candidateStore);
    }

    @Test
    void contentDetailsCombineExistingDetailTagsAndLiveReviewStatistics() {
        ContentResponse content = mock(ContentResponse.class);
        ContentTag tag = mock(ContentTag.class);
        when(contentService.getContent(contentId)).thenReturn(content);
        when(contentTagRepository.findAllByContentId(contentId)).thenReturn(List.of(tag));
        when(tag.getTag()).thenReturn("genre");
        when(tag.getValue()).thenReturn("Drama");
        when(reviewRepository.findAverageRatingByContentId(contentId)).thenReturn(4.2);
        when(reviewRepository.countByContentId(contentId)).thenReturn(5L);

        var result = tools.getContent(contentId);

        assertSame(content, result.content());
        assertEquals(List.of(new ContentTagDto("genre", "Drama")), result.tags());
        assertEquals(4.2, result.averageRating());
        assertEquals(5, result.reviewCount());
        verifyNoInteractions(candidateStore);
    }

    @Test
    void contentWithoutReviewsUsesZeroAverage() {
        when(contentService.getContent(contentId)).thenReturn(mock(ContentResponse.class));
        when(contentTagRepository.findAllByContentId(contentId)).thenReturn(List.of());
        assertEquals(0.0, tools.getContent(contentId).averageRating());
    }

    @Test
    void reviewsKeepRawResponseAndUseRequestedPageRatingOrder() {
        var response = new ReviewListResponse(List.of(), 2, 20, 45, 3);
        when(reviewService.getReviews(eq(contentId), any())).thenReturn(response);

        assertSame(response, tools.getContentReviews(contentId, 2, 20, "rating", "ASCENDING"));

        var pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(reviewService).getReviews(eq(contentId), pageable.capture());
        assertEquals(2, pageable.getValue().getPageNumber());
        assertEquals(20, pageable.getValue().getPageSize());
        assertEquals(Sort.Direction.ASC, pageable.getValue().getSort().getOrderFor("rating").getDirection());
    }

    @Test
    void reviewPagingAndSortValidationRejectInvalidInputs() {
        assertThrows(MoplException.class, () -> tools.getContentReviews(contentId, -1, null, null, null));
        assertThrows(MoplException.class, () -> tools.getContentReviews(contentId, null, 0, null, null));
        assertThrows(MoplException.class, () -> tools.getContentReviews(contentId, null, null, "unknown", null));
        verifyNoInteractions(reviewService);
    }

    @Test
    void playlistQueryCombinesMyOwnershipAndSubscriptionWithoutLosingCursor() {
        UUID idAfter = UUID.randomUUID();
        var query = new AiPlaylistTools.PlaylistQuery(null, true, true, "주말", 10,
            "subscribeCount", "ASCENDING", "5", idAfter);
        var response = CursorResponse.<PlaylistResponse>of(List.of(), "next", idAfter.toString(),
            true, 25, "subscribeCount", "ASCENDING");
        when(playlistService.getPlaylists(any(), any(), anyInt(), any(), any(), any(), any(), any(), any()))
            .thenReturn(response);

        assertSame(response, tools.getPlaylists(query, context));

        verify(playlistService).getPlaylists("5", idAfter, 10, "subscribeCount", "ASCENDING",
            currentUserId, currentUserId, currentUserId, "주말");
    }

    @Test
    void anotherUsersPlaylistsUseExplicitOwnerAndCurrentViewer() {
        UUID ownerId = UUID.randomUUID();
        tools.getPlaylists(new AiPlaylistTools.PlaylistQuery(ownerId, null, null,
            null, null, null, null, null, null), context);
        verify(playlistService).getPlaylists(null, null, 20, "updatedAt", "DESCENDING",
            currentUserId, null, ownerId, null);
    }

    @Test
    void conflictingOwnerSelectorsAreRejected() {
        var query = new AiPlaylistTools.PlaylistQuery(UUID.randomUUID(), true, null,
            null, null, null, null, null, null);
        assertThrows(MoplException.class, () -> tools.getPlaylists(query, context));
        verifyNoInteractions(playlistService);
    }

    @Test
    void playlistDetailsUseCurrentUserForSubscriptionStatus() {
        UUID playlistId = UUID.randomUUID();
        var response = mock(PlaylistResponse.class);
        when(playlistService.getPlaylist(playlistId, currentUserId)).thenReturn(response);
        assertSame(response, tools.getPlaylist(playlistId, context));
    }

    @Test
    void currentWatchingSessionDefaultsToCurrentUser() {
        var response = mock(WatchingSessionResponse.class);
        when(watchingSessionService.getWatchingSession(currentUserId)).thenReturn(response);
        assertSame(response, tools.getWatchingSessions(null, null, null, context).session());
    }

    @Test
    void contentWatchingQueryForwardsFiltersAndCursor() {
        UUID idAfter = UUID.randomUUID();
        var query = new AiPlaylistTools.WatchingQuery("이름", 30, "ASCENDING", "time", idAfter);
        var response = CursorResponse.<WatchingSessionResponse>of(List.of(), null, null,
            false, 0, "createdAt", "ASCENDING");
        when(watchingSessionService.getWatchingSessions(contentId, "이름", "time", idAfter,
            30, "createdAt", "ASCENDING")).thenReturn(response);
        assertSame(response, tools.getWatchingSessions(contentId, null, query, context).sessions());
    }

    @Test
    void conflictingWatchingSelectorsAreRejected() {
        assertThrows(MoplException.class,
            () -> tools.getWatchingSessions(contentId, currentUserId, null, context));
        verifyNoInteractions(watchingSessionService);
    }

    @Test
    void toolRegistrationHasOneSearchToolAndOptionalSchemas() throws Exception {
        var callbacks = ToolCallbacks.from(tools);
        assertEquals(Set.of("searchContents", "getContent", "getContentReviews", "getPlaylists",
            "getPlaylist", "getWatchingSessions", "recommendContentsForUser", "createPlaylist"),
            Arrays.stream(callbacks).map(callback -> callback.getToolDefinition().name())
                .collect(java.util.stream.Collectors.toSet()));
        assertEquals(8, callbacks.length);
        var callback = Arrays.stream(callbacks)
            .filter(value -> value.getToolDefinition().name().equals("searchContents")).findFirst().orElseThrow();
        JsonNode schema = new ObjectMapper().readTree(callback.getToolDefinition().inputSchema());
        assertFalse(schema.path("properties").has("toolContext"));
        assertFalse(schema.path("required").toString().contains("options"));
        JsonNode options = schema.path("properties").path("options");
        assertTrue(options.path("properties").has("mode"));
        assertFalse(options.path("required").toString().contains("cursor"));
    }
}
