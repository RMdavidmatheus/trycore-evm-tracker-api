package com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.controller;

import com.business.trycoreevmtrackerapi.domain.model.Project;
import com.business.trycoreevmtrackerapi.domain.port.in.ProjectUseCase;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.dto.request.ProjectRequest;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.dto.response.ProjectResponse;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.exception.ApiErrorResponse;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.out.persistence.mapper.EvmDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Proyectos", description = "Gestión de proyectos y cálculo consolidado de Valor Ganado (EVM)")
public class ProjectController {

    private final ProjectUseCase projectUseCase;
    private final EvmDtoMapper evmDtoMapper;

    @Operation(summary = "Crear un nuevo proyecto", description = "Registra un proyecto y retorna sus métricas EVM consolidadas iniciales.")
    @ApiResponse(responseCode = "201", description = "Proyecto creado exitosamente",
            content = @Content(schema = @Schema(implementation = ProjectResponse.class)))
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        Project domain = evmDtoMapper.toDomain(request);
        Project created = projectUseCase.createProject(domain);
        return ResponseEntity.status(HttpStatus.CREATED).body(evmDtoMapper.toResponse(created));
    }

    @Operation(summary = "Listar todos los proyectos activos", description = "Obtiene todos los proyectos activos junto con sus actividades y métricas EVM consolidadas.")
    @ApiResponse(responseCode = "200", description = "Lista de proyectos obtenida exitosamente",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProjectResponse.class))))
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        List<ProjectResponse> response = projectUseCase.getAllProjects()
                .stream()
                .map(evmDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener proyecto por ID", description = "Retorna el detalle de un proyecto específico, sus actividades e indicadores EVM consolidados.")
    @ApiResponse(responseCode = "200", description = "Proyecto encontrado",
            content = @Content(schema = @Schema(implementation = ProjectResponse.class)))
    @ApiResponse(responseCode = "400", description = "Formato de UUID inválido",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Proyecto no encontrado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(
            @Parameter(description = "ID único del proyecto (UUID)", required = true)
            @PathVariable UUID id) {
        Project project = projectUseCase.getProjectById(id);
        return ResponseEntity.ok(evmDtoMapper.toResponse(project));
    }

    @Operation(summary = "Actualizar un proyecto", description = "Actualiza el nombre y descripción de un proyecto existente.")
    @ApiResponse(responseCode = "200", description = "Proyecto actualizado exitosamente",
            content = @Content(schema = @Schema(implementation = ProjectResponse.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos o UUID mal formado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Proyecto no encontrado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @Parameter(description = "ID único del proyecto (UUID)", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody ProjectRequest request) {
        Project domain = evmDtoMapper.toDomain(request);
        Project updated = projectUseCase.updateProject(id, domain);
        return ResponseEntity.ok(evmDtoMapper.toResponse(updated));
    }

    @Operation(summary = "Eliminar un proyecto", description = "Realiza un borrado lógico del proyecto y de todas sus actividades asociadas.")
    @ApiResponse(responseCode = "204", description = "Proyecto eliminado exitosamente")
    @ApiResponse(responseCode = "400", description = "Formato de UUID inválido",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Proyecto no encontrado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "ID único del proyecto (UUID)", required = true)
            @PathVariable UUID id) {
        projectUseCase.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
