package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserTasksListResponseDTO {

    @JsonProperty("data")
    private List<UserTasksDTO> data;

    @JsonProperty("pagination")
    private PaginationDTO pagination;

    @JsonProperty("totalElements")
    private Long totalElements;

    @JsonProperty("totalPages")
    private Integer totalPages;

    @JsonProperty("currentPage")
    private Integer currentPage;

    @JsonProperty("pageSize")
    private Integer pageSize;

    @JsonProperty("hasNext")
    private Boolean hasNext;

    @JsonProperty("hasPrevious")
    private Boolean hasPrevious;
}
