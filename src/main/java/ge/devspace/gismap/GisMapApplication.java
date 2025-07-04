package ge.devspace.gismap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GisMapApplication {
    public static void main(String[] args) {
        SpringApplication.run(GisMapApplication.class, args);
    }
}