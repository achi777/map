package ge.devspace.gismap.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "ge.devspace.gismap.repository")
public class DatabaseConfig {
}