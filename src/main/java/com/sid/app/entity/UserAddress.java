package com.sid.app.entity;

import com.sid.app.audit.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "user_address", indexes = {
        @Index(name = "idx_user_address_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class UserAddress extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_address_id")
    private Long userAddressId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "postal_code", length = 30)
    private String postalCode;

    @Builder.Default
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = Boolean.TRUE;
}
