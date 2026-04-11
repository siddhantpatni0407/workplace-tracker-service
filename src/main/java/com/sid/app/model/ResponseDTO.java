package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Generic response DTO to standardize API responses.
 *
 * @param <T> The type of the response data.
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response envelope used by all endpoints")
public class ResponseDTO<T> {

    @JsonProperty("status")
    @Schema(description = "Operation result indicator", example = "SUCCESS",
            allowableValues = {"SUCCESS", "FAILED"})
    private String status;

    @JsonProperty("message")
    @Schema(description = "Human-readable message describing the result",
            example = "Record retrieved successfully")
    private String message;

    @JsonProperty("data")
    @Schema(description = "Response payload — null for void operations or on error")
    private T data;

}