package org.example.planner_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.tag.TagResponseDto;
import org.example.planner_backend.mapper.TagMapper;
import org.example.planner_backend.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public List<TagResponseDto> getTags() {
        return tagRepository.findAll().stream().map(tagMapper::toResponse).toList();
    }
}
