package com.mopl.playlist.service;

import com.mopl.common.exception.CommonErrorCode;
import com.mopl.common.exception.MoplException;
import com.mopl.common.exception.playlist.PlaylistAccessDeniedException;
import com.mopl.common.exception.playlist.PlaylistNotFoundException;
import com.mopl.core.domain.playlist.entity.Playlist;
import com.mopl.core.domain.user.entity.User;
import com.mopl.playlist.dto.*;
import com.mopl.playlist.repository.PlaylistRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PlaylistService {

	private static final int MAX_LIMIT = 100;

	private final PlaylistRepository playlistRepository;
	private final EntityManager entityManager;

	public PlaylistService(PlaylistRepository playlistRepository, EntityManager entityManager) {
		this.playlistRepository = playlistRepository;
		this.entityManager = entityManager;
	}

	public PlaylistResponse getPlaylist(UUID playlistId) {
		return PlaylistResponse.from(findPlaylistOrThrow(playlistId));
	}

	public PlaylistListResponse getPlaylists(
		String cursor, UUID idAfter, int limit, String sortByParam, String sortDirectionParam
	) {
		if (limit <= 0) {
			throw new MoplException(CommonErrorCode.INVALID_INPUT_VALUE);
		}
		if ((cursor == null) != (idAfter == null)) {
			throw new MoplException(CommonErrorCode.INVALID_INPUT_VALUE);
		}

		int safeLimit = Math.min(limit, MAX_LIMIT);
		PlaylistSortBy sortBy = PlaylistSortBy.from(sortByParam);
		SortDirection sortDirection = SortDirection.from(sortDirectionParam);

		List<Playlist> playlists = playlistRepository.findAllByCursor(
			cursor, idAfter, safeLimit + 1, sortBy, sortDirection
		);

		boolean hasNext = playlists.size() > safeLimit;
		List<Playlist> pageContent = hasNext ? playlists.subList(0, safeLimit) : playlists;

		String nextCursor = null;
		UUID nextIdAfter = null;
		if (hasNext) {
			Playlist last = pageContent.get(pageContent.size() - 1);
			nextIdAfter = last.getId();
			nextCursor = sortBy == PlaylistSortBy.UPDATED_AT
				? last.getUpdatedAt().toString()
				: "0";
		}

		List<PlaylistResponse> data = pageContent.stream().map(PlaylistResponse::from).toList();
		long totalCount = playlistRepository.count();

		return new PlaylistListResponse(
			data, nextCursor, nextIdAfter, hasNext, sortByParam, sortDirectionParam, totalCount
		);
	}

	@Transactional
	public PlaylistResponse createPlaylist(UUID requesterId, PlaylistCreateRequest request) {
		User owner = entityManager.find(User.class, requesterId);
		if (owner == null) {
			// TODO: User 도메인 예외 체계(UserErrorCode.USER_NOT_FOUND 등) 생기면 교체 예정
			throw new MoplException(CommonErrorCode.INVALID_INPUT_VALUE);
		}

		Playlist playlist = new Playlist(owner, request.title(), request.description());
		Playlist saved = playlistRepository.save(playlist);

		return PlaylistResponse.from(saved);
	}

	@Transactional
	public PlaylistResponse updatePlaylist(UUID requesterId, UUID playlistId, PlaylistUpdateRequest request) {
		if (request.title() != null && request.title().isBlank()) {
			throw new MoplException(CommonErrorCode.INVALID_INPUT_VALUE);
		}
		if (request.description() != null && request.description().isBlank()) {
			throw new MoplException(CommonErrorCode.INVALID_INPUT_VALUE);
		}

		Playlist playlist = findPlaylistOrThrow(playlistId);
		validateOwner(playlist, requesterId);

		playlist.update(request.title(), request.description());
		entityManager.flush();  // @PreUpdate 즉시 실행 -> updatedAt 갱신 보장

		return PlaylistResponse.from(playlist);
	}

	@Transactional
	public void deletePlaylist(UUID requesterId, UUID playlistId) {
		Playlist playlist = findPlaylistOrThrow(playlistId);
		validateOwner(playlist, requesterId);
		playlistRepository.delete(playlist);
	}

	private Playlist findPlaylistOrThrow(UUID playlistId) {
		return playlistRepository.findById(playlistId).orElseThrow(PlaylistNotFoundException::new);
	}

	private void validateOwner(Playlist playlist, UUID requesterId) {
		if (!playlist.getOwner().getId().equals(requesterId)) {
			throw new PlaylistAccessDeniedException();
		}
	}
}