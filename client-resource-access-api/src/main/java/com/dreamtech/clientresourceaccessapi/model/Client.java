package com.dreamtech.clientresourceaccessapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name="client")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String key;

    //@Column(name = "secret_hash", nullable = false, length = 500)  // TODO: Make this nullable for now, will be addressed later
    @Column(name = "secret_hash", length = 500)
    private String secretHash;

    private String description;

    /* There could be more columns:
    private String status;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime secretHashExpiresAt;
     */

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}