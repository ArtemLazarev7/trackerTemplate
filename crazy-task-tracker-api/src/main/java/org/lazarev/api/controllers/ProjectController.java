package org.lazarev.api.controllers;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.lazarev.api.controllers.helpers.ControllerHelper;
import org.lazarev.api.dto.AskDto;
import org.lazarev.api.dto.ProjectDto;
import org.lazarev.api.exceptions.BadRequestException;
import org.lazarev.api.exceptions.NotFoundException;
import org.lazarev.api.factories.ProjectDtoFactory;
import org.lazarev.store.entities.ProjectEntity;
import org.lazarev.store.repositories.ProjectRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@RestController
public class ProjectController {

    ProjectRepository projectRepository;

    ProjectDtoFactory projectDtoFactory;

    ControllerHelper controllerHelper;

    public static final String FETCH_PROJECT = "/api/projects";
    //    public static final String CREATE_PROJECT = "/api/projects";
    //   public static final String EDIT_PROJECT = "/api/projects/{project_id}";
    public static final String DELETE_PROJECT = "/api/projects/{project_id}";
    public static final String CREATE_OR_UPDATE_PROJECT = "/api/projects";

    @GetMapping(FETCH_PROJECT)
    public List<ProjectDto> fetchProject(
            @RequestParam(value = "prefix_name", required = false) Optional<String> optionalPrefixName) {

        optionalPrefixName = optionalPrefixName.filter(prefixName -> !prefixName.trim().isEmpty());

        Stream<ProjectEntity> projectStream = optionalPrefixName
                .map(projectRepository::streamAllByNameStartsWithIgnoreCase)
                .orElse(projectRepository.streamAllBy());

        return projectStream.map(projectDtoFactory::makeProjectDto)
                .collect(Collectors.toList());
    }

//    @PostMapping(CREATE_PROJECT)
//    public ProjectDto createProject(@RequestParam String projectName) {
//
//        if (projectName.trim().isEmpty()) {
//            throw new BadRequestException("Name can't be Empty");
//        }
//
//        projectRepository
//                .findByName(projectName)
//                .ifPresent(project -> {
//                    throw new BadRequestException(String.format("Project \"s%\" already exist.", projectName));
//                });
//        ProjectEntity project = projectRepository.saveAndFlush(
//                ProjectEntity.builder()
//                        .name(projectName)
//                        .build()
//        );
//
//        return projectDtoFactory.makeProjectDto(project);
//
//    }

    @PutMapping(CREATE_OR_UPDATE_PROJECT)
    public ProjectDto createOrUpdateProject(@RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
                                            @RequestParam(value = "project_name", required = false) Optional<String> optionalProjectName) {

        optionalProjectName = optionalProjectName.filter(projectName -> !projectName.trim().isEmpty());

        boolean isCreate = !optionalProjectId.isPresent();

        if (isCreate && !optionalProjectName.isPresent()) {
            throw new BadRequestException("Project name can't be empty.");
        }

        final ProjectEntity project = optionalProjectId
                .map(controllerHelper::getProjectOrThrowException)
                .orElseGet(() -> ProjectEntity.builder().build());


        optionalProjectName.ifPresent(projectName ->
        {
            projectRepository.findByName(projectName)
                    .filter(anotherProject -> !Objects.equals(anotherProject.getId(), project.getId()))
                    .ifPresent(anotherProject -> {
                        throw new BadRequestException(String.format("Project \"s%\" already exist.", projectName));
                    });
            project.setName(projectName);
        });

        final ProjectEntity savedProject = projectRepository.saveAndFlush(project);

        return projectDtoFactory.makeProjectDto(savedProject);

    }

//    @PatchMapping(EDIT_PROJECT)
//    public ProjectDto editProject(@PathVariable("project_id") Long projectId
//            , @RequestParam String projectName) {
//
//        if (projectName.trim().isEmpty()) {
//            throw new BadRequestException("Name can't be Empty");
//        }
//
//
//        ProjectEntity project = getProjectOrThrowException(projectId);
//
//        projectRepository
//                .findByName(projectName)
//                .filter(anotherProject -> !Objects.equals(anotherProject.getId(), projectId))
//                .ifPresent(anotherProject -> {
//                    throw new BadRequestException(String.format("Project \"s%\" already exist.", projectName));
//                });
//        project.setName(projectName);
//
//        project = projectRepository.saveAndFlush(project);
//
//
//        return projectDtoFactory.makeProjectDto(project);
//
//    }

    @DeleteMapping(DELETE_PROJECT)
    public AskDto deleteProject(@PathVariable("project_id") Long projectId) {
        controllerHelper.getProjectOrThrowException(projectId);

        projectRepository.deleteById(projectId);

        return AskDto.makeDefault(true);
    }

}
