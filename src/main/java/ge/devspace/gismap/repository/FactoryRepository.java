package ge.devspace.gismap.repository;

import ge.devspace.gismap.entity.Factory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactoryRepository extends JpaRepository<Factory, Long> {
    
    @Query("SELECT f FROM Factory f WHERE f.industryType = :industryType")
    List<Factory> findByIndustryType(String industryType);
    
    @Query("SELECT f FROM Factory f WHERE f.status = :status")
    List<Factory> findByStatus(String status);
    
    @Query("SELECT f FROM Factory f WHERE f.establishedYear > :year")
    List<Factory> findByEstablishedYearAfter(Integer year);
}