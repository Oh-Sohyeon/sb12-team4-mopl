package com.mopl.playlist.repository;

import com.mopl.core.domain.playlist.entity.PlaylistContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlaylistContentRepository extends JpaRepository<PlaylistContent, UUID> {

	boolean existsByPlaylistIdAndContentId(UUID playlistId, UUID contentId);

	Optional<PlaylistContent> findByPlaylistIdAndContentId(UUID playlistId, UUID contentId);
}