package com.dreamtech.clientresourceaccessapi.model;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data               // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor  // Needed by Hibernate reflection engines
@AllArgsConstructor // For clean instantiation in the service layer
public class ClientResourceAccessId implements Serializable {

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "resource_id")
    private Long resourceId;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        ClientResourceAccessId clientResourceAccessId = (ClientResourceAccessId) obj;
        return Objects.equals(clientId, clientResourceAccessId.clientId) && Objects.equals(resourceId, clientResourceAccessId.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, resourceId);
    }

}
