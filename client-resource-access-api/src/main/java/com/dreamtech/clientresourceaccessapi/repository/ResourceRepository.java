package com.dreamtech.clientresourceaccessapi.repository;

import com.dreamtech.clientresourceaccessapi.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    @Query("select r from Resource r where r.key like concat('%', :key, '%')")
    List<Resource> findByKey(@Param("key") String key);

    @Query("select r from Resource r where r.type like concat('%', :type, '%')")
    List<Resource> findByType(@Param("type") String type);

    @Query("select r from Resource r where r.description like concat('%', :description, '%')")
    List<Resource> findByDescription(@Param("description") String description);

    @Query("select r from Resource r where r.key like concat('%', :key, '%') and r.type like concat('%', :type, '%')")
    List<Resource> findByKeyAndType(@Param("key") String key, @Param("type") String type);

    @Query("select r from Resource r where r.key like concat('%', :key, '%') and r.description like concat('%', :description, '%')")
    List<Resource> findByKeyAndDescription(@Param("key") String key, @Param("description") String description);

    @Query("select r from Resource r where r.type like concat('%', :type, '%') and r.description like concat('%', :description, '%')")
    List<Resource> findByTypeAndDescription(@Param("type") String type, @Param("description") String description);

    @Query("select r from Resource r where r.key like concat('%', :key, '%') and r.type like concat('%', :type, '%') and r.description like concat('%', :description, '%')")
    List<Resource> findByKeyAndTypeAndDescription(@Param("key") String key, @Param("type") String type, @Param("description") String description);

    @Query("select key from Resource where id = :id")
    Optional<String> findKeyById(@Param("id") Long id);

    @Query("SELECT type FROM Resource WHERE id = :id")
    Optional<String> findTypeById(@Param("id") Long id);

    @Query("select description from Resource where id = :id")
    Optional<String> findDescriptionById(@Param("id") Long id);

}
