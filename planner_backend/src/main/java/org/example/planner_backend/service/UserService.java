package org.example.planner_backend.service;


import lombok.RequiredArgsConstructor;
import org.example.planner_backend.dto.user.UserResponseDto;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.mapper.UserMapper;
import org.example.planner_backend.model.entity.AppUser;
import org.example.planner_backend.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository userRepository;
    private final UserMapper userMapper;


    @Transactional(readOnly = true)
    public UserResponseDto getUser(final String email) {

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        return userMapper.toResponse(user);
    }
}
