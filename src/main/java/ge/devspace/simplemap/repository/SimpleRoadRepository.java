package ge.devspace.simplemap.repository;

import ge.devspace.simplemap.entity.SimpleRoad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimpleRoadRepository extends JpaRepository<SimpleRoad, Long> {
}