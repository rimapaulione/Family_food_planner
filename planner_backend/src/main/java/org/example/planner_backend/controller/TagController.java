package org.example.planner_backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.tag.TagResponseDto;
import org.example.planner_backend.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tags")
public class TagController {
    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagResponseDto>> getAll() {
        return ResponseEntity.ok(tagService.getTags());
    }
}
