package com.sid.app.model;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotesBulkDeleteRequest {
    private List<Long> noteIds;
    private Boolean permanentDelete;
}
