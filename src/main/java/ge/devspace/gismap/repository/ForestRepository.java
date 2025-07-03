package ge.devspace.gismap.repository;

import ge.devspace.gismap.entity.Forest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForestRepository extends JpaRepository<Forest, Long> {
    
    @Query("SELECT f FROM Forest f WHERE f.forestType = :forestType")
    List<Forest> findByForestType(String forestType);
    
    @Query("SELECT f FROM Forest f WHERE f.protectionStatus = :status")
    List<Forest> findByProtectionStatus(String status);
    
    @Query("SELECT f FROM Forest f WHERE f.areaHectares > :minArea")
    List<Forest> findByAreaGreaterThan(Double minArea);
}