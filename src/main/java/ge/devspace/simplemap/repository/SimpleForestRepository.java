package ge.devspace.simplemap.repository;

import ge.devspace.simplemap.entity.SimpleForest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimpleForestRepository extends JpaRepository<SimpleForest, Long> {
}