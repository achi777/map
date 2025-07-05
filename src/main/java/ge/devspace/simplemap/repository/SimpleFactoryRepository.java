package ge.devspace.simplemap.repository;

import ge.devspace.simplemap.entity.SimpleFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimpleFactoryRepository extends JpaRepository<SimpleFactory, Long> {
}