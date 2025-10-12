package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserTasksBulkDeleteRequest {

    @JsonProperty("userTaskIds")
    @NotEmpty(message = "Task IDs list cannot be empty")
    private List<Long> userTaskIds;

    @JsonProperty("cascadeDelete")
    @Builder.Default
    private Boolean cascadeDelete = false;
}
