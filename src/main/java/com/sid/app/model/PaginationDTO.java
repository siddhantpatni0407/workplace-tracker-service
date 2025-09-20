package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pagination metadata for Special Days API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginationDTO {

    @JsonProperty("currentPage")
    private Integer currentPage;

    @JsonProperty("itemsPerPage")
    private Integer itemsPerPage;

    @JsonProperty("totalItems")
    private Long totalItems;

    @JsonProperty("totalPages")
    private Integer totalPages;

    @JsonProperty("hasPreviousPage")
    private Boolean hasPreviousPage;

    @JsonProperty("hasNextPage")
    private Boolean hasNextPage;
}
