package com.example.dataserverspringboot.entities.details;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Input DTO for the {@code POST /api/details/update_score} endpoint.
 *
 * <p>Replaces the previous {@code @RequestBody Map<String, Object>} approach.
 * Spring automatically deserialises the JSON body into this typed class,
 * and {@code @Valid} triggers bean validation before the controller method
 * runs — eliminating all manual null-checks, instanceof casts, and try/catch
 * blocks that were previously needed in the controller.</p>
 *
 * <p>Validation rules:</p>
 * <ul>
 *   <li>{@code @NotNull} — ensures each field is present in the request body.</li>
 *   <li>{@code @DecimalMin} / {@code @DecimalMax} — ensures score is in
 *       the range [{@code 0.00}, {@code 10.00}].</li>
 * </ul>
 *
 * <p>If any constraint fails, Spring automatically returns {@code 400 Bad Request}
 * with a structured error body — no extra controller code is needed.</p>
 *
 * <p>Note on setters: unlike output DTOs, this class requires setters because
 * Jackson needs them to deserialise the incoming JSON body into an instance.</p>
 */
@Schema(description = "Request body for updating an anime score")
public class UpdateScoreRequestDTO {

    /** Anime MAL ID to update — must not be null. */
    @Schema(description = "MyAnimeList ID of the anime to update", example = "1", required = true)
    @NotNull(message = "mal_id is required")
    private Integer malId;

    /** New score value — must be between 0.00 and 10.00. */
    @Schema(description = "New score value (must be between 0.00 and 10.00)", example = "8.50", required = true)
    @NotNull(message = "score is required")
    @DecimalMin(value = "0.00", message = "score must be >= 0.00")
    @DecimalMax(value = "10.00", message = "score must be <= 10.00")
    private BigDecimal score;

    /**
     * Returns the anime MAL ID.
     * {@code @JsonProperty} maps the JSON key {@code "mal_id"} to this getter
     * for both serialisation (output) and deserialisation (input).
     *
     * @return anime MAL ID
     */
    @JsonProperty("mal_id")
    public Integer getMalId() { return malId; }

    /**
     * Returns the new score value.
     *
     * @return score value
     */
    public BigDecimal getScore() { return score; }

    /**
     * Sets the anime MAL ID from the JSON body.
     * {@code @JsonProperty} tells Jackson to read the key {@code "mal_id"}
     * from the request body and map it to this setter.
     *
     * @param malId anime MAL ID from the request body
     */
    @JsonProperty("mal_id")
    public void setMalId(Integer malId) { this.malId = malId; }

    /**
     * Sets the score from the JSON body.
     *
     * @param score new score value from the request body
     */
    public void setScore(BigDecimal score) { this.score = score; }
}
