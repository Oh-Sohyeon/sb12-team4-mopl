package com.mopl.content.controller;

import com.mopl.content.dto.ContentListResponse;
import com.mopl.content.dto.ContentResponse;
import com.mopl.content.service.ContentService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/contents")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/{contentId}")
    public ResponseEntity<ContentResponse> getContent(@PathVariable UUID contentId) {
        ContentResponse response = contentService.getContent(contentId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ContentListResponse> getContents(
            @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable) {
        ContentListResponse response = contentService.getContents(pageable);

        return ResponseEntity.ok(response);
    }
}
