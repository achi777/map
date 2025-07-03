package ge.devspace.gismap.repository;

import ge.devspace.gismap.entity.Road;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadRepository extends JpaRepository<Road, Long> {
    
    @Query("SELECT r FROM Road r WHERE r.roadType = :roadType")
    List<Road> findByRoadType(String roadType);
    
    @Query("SELECT r FROM Road r WHERE r.surfaceType = :surfaceType")
    List<Road> findBySurfaceType(String surfaceType);
    
    @Query("SELECT r FROM Road r WHERE r.lengthKm > :minLength")
    List<Road> findByLengthGreaterThan(Double minLength);
}