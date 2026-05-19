package com.example.dataserverspringboot.entities.details;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Input DTO for the POST /api/details/update_score endpoint.
 *
 * PURPOSE:
 *   Replaces the previous @RequestBody Map<String, Object> approach.
 *   Spring automatically deserialises the JSON body into this typed class,
 *   and @Valid triggers bean validation before the controller method runs —
 *   eliminating all manual null-checks, instanceof casts, and try/catch blocks
 *   that were previously needed in the controller.
 *
 * VALIDATION:
 *   @NotNull  — ensures the field is present in the request body.
 *   @DecimalMin / @DecimalMax — ensures score is in the [0.00, 10.00] range.
 *   If any constraint fails, Spring returns 400 Bad Request automatically
 *   with a structured error body — no extra controller code needed.
 */
@Schema(description = "Request body for updating an anime score")
public class UpdateScoreRequestDTO {

    @Schema(description = "MyAnimeList ID of the anime to update", example = "1", required = true)
    @NotNull(message = "mal_id is required")
    private Integer malId;

    @Schema(description = "New score value (must be between 0.00 and 10.00)", example = "8.50", required = true)
    @NotNull(message = "score is required")
    @DecimalMin(value = "0.00", message = "score must be >= 0.00")
    @DecimalMax(value = "10.00", message = "score must be <= 10.00")
    private BigDecimal score;

    // ── Getters ───────────────────────────────────────────────────────────────
    @JsonProperty("mal_id")
    public Integer getMalId()    { return malId; }
    public BigDecimal getScore() { return score; }

    // ── Setters required by Jackson for deserialisation ───────────────────────
    @JsonProperty("mal_id")
    public void setMalId(Integer malId)       { this.malId = malId; }
    public void setScore(BigDecimal score)    { this.score = score; }
}
