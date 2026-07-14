package com.dreamtech.clientresourceaccessapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="client_resource_access")
@IdClass(ClientResourceAccessId.class)
@Data               // Generates getters, setters, toString, equals, and hashCode
@Builder
@NoArgsConstructor  // Generates a protected/public empty constructor needed by Hibernate
@AllArgsConstructor
public class ClientResourceAccess {

    @Id
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Id
    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @Column(name="access_code", nullable = false, length = 100)
    private String accessCode;

    private String description;

}
