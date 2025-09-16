package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddressDTO {

    @JsonProperty("userAddressId")
    private Long userAddressId;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("address")
    private String address;

    @JsonProperty("city")
    @Size(max = 100)
    private String city;

    @JsonProperty("state")
    @Size(max = 100)
    private String state;

    @JsonProperty("country")
    @Size(max = 100)
    private String country;

    @JsonProperty("postalCode")
    @Size(max = 30)
    private String postalCode;

    @JsonProperty("isPrimary")
    private Boolean isPrimary;
}
