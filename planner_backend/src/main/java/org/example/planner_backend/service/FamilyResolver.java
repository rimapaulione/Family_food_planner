package org.example.planner_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.model.entity.AppUser;
import org.example.planner_backend.model.entity.Family;
import org.example.planner_backend.repository.AppUserRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FamilyResolver {

    private final AppUserRepository appUserRepository;

    public Family getFamilyByEmail(final String email) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));
        if (user.getFamily() == null) {
            throw new ResourceNotFoundException("User has no family");
        }
        return user.getFamily();
    }

    public UUID getFamilyIdByEmail(final String email) {
        return getFamilyByEmail(email).getId();
    }
}
