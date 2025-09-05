package com.sid.app.entity;

import com.sid.app.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "encryption_keys")
public class EncryptionKey extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "encryption_keys_id", nullable = false)
    private Long id;

    @Column(name = "key_version", nullable = false, unique = true)
    private int keyVersion;

    @Column(name = "secret_key", nullable = false, unique = true)
    private String secretKey;

}