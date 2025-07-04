package ge.devspace.gismap.config;

import ge.devspace.gismap.service.DataInitializationService;
import ge.devspace.gismap.service.GeoServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private DataInitializationService dataInitializationService;

    @Autowired
    private GeoServerService geoServerService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Initializing sample data for Tbilisi...");
        dataInitializationService.initializeSampleData();
        System.out.println("Sample data initialization completed.");
        
        // Setup GeoServer integration
        Thread.sleep(2000); // Wait for database to be ready
        geoServerService.setupGeoServer();
    }
}