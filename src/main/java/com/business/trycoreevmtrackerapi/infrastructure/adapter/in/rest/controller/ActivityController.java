package com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.controller;

import com.business.trycoreevmtrackerapi.domain.model.Activity;
import com.business.trycoreevmtrackerapi.domain.port.in.ActivityUseCase;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.dto.request.ActivityRequest;
import com.business.trycoreevmtrackerapi.infrastructure.adapter.in.rest.dto.response.ActivityResponse;
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
@RequestMapping("/api/v1/projects/{projectId}/activities")
@RequiredArgsConstructor
@Tag(name = "Actividades", description = "Gestión de actividades por proyecto y cálculo individual de indicadores EVM")
public class ActivityController {

    private final ActivityUseCase activityUseCase;
    private final EvmDtoMapper evmDtoMapper;

    @Operation(summary = "Crear actividad en un proyecto", description = "Registra una nueva actividad dentro de un proyecto y calcula automáticamente sus indicadores EVM.")
    @ApiResponse(responseCode = "201", description = "Actividad creada exitosamente",
            content = @Content(schema = @Schema(implementation = ActivityResponse.class)))
    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (ej. porcentajes fuera de rango 0-100 o montos negativos)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Proyecto no encontrado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PostMapping
    public ResponseEntity<ActivityResponse> createActivity(
            @Parameter(description = "ID único del proyecto (UUID)", required = true)
            @PathVariable UUID projectId,
            @Valid @RequestBody ActivityRequest request) {
        Activity domain = evmDtoMapper.toDomain(request);
        Activity created = activityUseCase.createActivity(projectId, domain);
        return ResponseEntity.status(HttpStatus.CREATED).body(evmDtoMapper.toResponse(created));
    }

    @Operation(summary = "Listar actividades de un proyecto", description = "Obtiene todas las actividades activas de un proyecto con sus métricas EVM calculadas.")
    @ApiResponse(responseCode = "200", description = "Lista de actividades obtenida exitosamente",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ActivityResponse.class))))
    @ApiResponse(responseCode = "404", description = "Proyecto no encontrado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getActivitiesByProjectId(
            @Parameter(description = "ID único del proyecto (UUID)", required = true)
            @PathVariable UUID projectId) {
        List<ActivityResponse> response = activityUseCase.getActivitiesByProjectId(projectId)
                .stream()
                .map(evmDtoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Actualizar una actividad", description = "Actualiza los datos de avance o costos de una actividad y recalcula sus indicadores EVM.")
    @ApiResponse(responseCode = "200", description = "Actividad actualizada exitosamente",
            content = @Content(schema = @Schema(implementation = ActivityResponse.class)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos o UUID mal formado",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Proyecto o actividad no encontrados",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @PutMapping("/{activityId}")
    public ResponseEntity<ActivityResponse> updateActivity(
            @Parameter(description = "ID único del proyecto (UUID)", required = true)
            @PathVariable UUID projectId,
            @Parameter(description = "ID único de la actividad (UUID)", required = true)
            @PathVariable UUID activityId,
            @Valid @RequestBody ActivityRequest request) {
        Activity domain = evmDtoMapper.toDomain(request);
        Activity updated = activityUseCase.updateActivity(projectId, activityId, domain);
        return ResponseEntity.ok(evmDtoMapper.toResponse(updated));
    }

    @Operation(summary = "Eliminar una actividad", description = "Realiza un borrado lógico de la actividad dentro del proyecto.")
    @ApiResponse(responseCode = "204", description = "Actividad eliminada exitosamente")
    @ApiResponse(responseCode = "400", description = "Formato de UUID inválido",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Proyecto o actividad no encontrados",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> deleteActivity(
            @Parameter(description = "ID único del proyecto (UUID)", required = true)
            @PathVariable UUID projectId,
            @Parameter(description = "ID único de la actividad (UUID)", required = true)
            @PathVariable UUID activityId) {
        activityUseCase.deleteActivity(projectId, activityId);
        return ResponseEntity.noContent().build();
    }
}
