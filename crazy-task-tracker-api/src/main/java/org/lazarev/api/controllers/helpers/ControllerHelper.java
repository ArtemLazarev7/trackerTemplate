package org.lazarev.api.controllers.helpers;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.lazarev.api.exceptions.NotFoundException;
import org.lazarev.store.entities.ProjectEntity;
import org.lazarev.store.repositories.ProjectRepository;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Component
@Transactional
public class ControllerHelper {
    ProjectRepository projectRepository;

    public ProjectEntity getProjectOrThrowException(Long projectId) {
        ProjectEntity project = projectRepository
                .findById(projectId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Project with\"%s\" doesn't exist.", projectId)));
        return project;
    }
}
