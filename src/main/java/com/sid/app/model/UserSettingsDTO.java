package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettingsDTO {

    @JsonProperty("userSettingId")
    private Long userSettingId;

    @JsonProperty("userId")
    @NotNull(message = "userId is required")
    private Long userId;

    @JsonProperty("timezone")
    @Size(max = 64)
    private String timezone;

    @JsonProperty("workWeekStart")
    @Min(1)
    @Max(7)
    private Integer workWeekStart;

    @JsonProperty("language")
    @Size(max = 16)
    private String language;

    @JsonProperty("dateFormat")
    @Size(max = 32)
    private String dateFormat;

}
