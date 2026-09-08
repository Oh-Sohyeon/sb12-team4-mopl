package com.mopl.content.service;

import com.mopl.common.exception.content.ContentNotFoundException;
import com.mopl.content.dto.ContentListResponse;
import com.mopl.content.dto.ContentResponse;
import com.mopl.content.repository.ContentRepository;
import com.mopl.core.domain.content.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ContentService {

    private final ContentRepository contentRepository;

    public ContentService(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    // 단건 조회
    public ContentResponse getContent(UUID contentId) {
        Content content = contentRepository.findByIdAndDeletedAtIsNull(contentId)
                .orElseThrow(ContentNotFoundException::new);

        return ContentResponse.from(content);
    }

    // 목록 조회
    public ContentListResponse getContents(Pageable pageable) {
        Page<ContentResponse> contentPage = contentRepository
                .findAllByDeletedAtIsNull(pageable)
                .map(ContentResponse::from);

        return ContentListResponse.from(contentPage);
    }
}
