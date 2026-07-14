
package com.dreamtech.clientresourceaccessapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.dreamtech.clientresourceaccessapi.model.Client;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("select c from Client c where c.key like concat('%', :key, '%') and c.description like concat('%', :description, '%')")
    List<Client> findByKeyAndDescription(@Param("key") String key, @Param("description") String description);

    @Query("select c from Client c where c.key like concat('%', :key, '%')")
    List<Client> findByKey(@Param("key") String key);

    @Query("select c from Client c where c.description like concat('%', :description, '%')")
    List<Client> findByDescription(@Param("description") String description);

    @Query("select key from Client where id = :id")
    Optional<String> findKeyById(@Param("id") Long id);

    @Query("select description from Client where id = :id")
    Optional<String> findDescriptionById(@Param("id") Long id);

}
