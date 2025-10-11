package com.sid.app.model;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotesListResponseDTO {
    private List<UserNotesDTO> data;
    private PaginationDTO pagination;
}
