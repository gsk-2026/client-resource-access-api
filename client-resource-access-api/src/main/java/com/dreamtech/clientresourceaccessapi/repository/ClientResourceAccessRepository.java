package com.dreamtech.clientresourceaccessapi.repository;

import com.dreamtech.clientresourceaccessapi.model.ClientResourceAccess;
import com.dreamtech.clientresourceaccessapi.model.ClientResourceAccessId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientResourceAccessRepository extends JpaRepository<ClientResourceAccess, ClientResourceAccessId> {

    @Query("select cra from ClientResourceAccess cra where cra.accessCode like concat('%', :accessCode, '%') and cra.description like concat('%', :description, '%')")
    List<ClientResourceAccess> findByAccessCodeAndDescription(@Param("accessCode") String accessCode, @Param("description") String description);

    @Query("select cra from ClientResourceAccess cra where cra.accessCode like concat('%', :accessCode, '%')")
    List<ClientResourceAccess> findByAccessCode(@Param("accessCode") String accessCode);

    @Query("select cra from ClientResourceAccess cra where cra.description like concat('%', :description, '%')")
    List<ClientResourceAccess> findByDescription(@Param("description") String description);

    @Query("SELECT cra FROM ClientResourceAccess cra WHERE cra.clientId = :clientId AND cra.resourceId = :resourceId")
    Optional<ClientResourceAccess> findByIds(@Param("clientId") Long clientId, @Param("resourceId") Long resourceId);

    //@Query(value="SELECT description FROM client_resource WHERE client_id = :clientId AND resource_id = :resourceId", nativeQuery = true)
    @Query("SELECT crs.description FROM ClientResourceAccess crs WHERE crs.clientId = :clientId AND crs.resourceId = :resourceId")
    Optional<String> findDescriptionByIds(@Param("clientId") Long clientId, @Param("resourceId") Long resourceId);

    //@Query(value = "SELECT access_code FROM client_resource WHERE client_id = :clientId AND resource_id = :resourceid", nativeQuery = true)
    @Query("SELECT cra.accessCode FROM ClientResourceAccess cra WHERE cra.clientId = :clientId AND cra.resourceId = :resourceId")
    Optional<String> findAccessCodeByIds(@Param("clientId") Long clientId, @Param("resourceId") Long resourceId);

    @Modifying
    @Query("DELETE FROM ClientResourceAccess WHERE clientId = :clientId AND resourceId = :resourceId")
    void deleteByIds(@Param("clientId") Long clientId, @Param("resourceId") Long resourceId);

}
