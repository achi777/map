package ge.devspace.simplemap.controller;

import ge.devspace.simplemap.entity.SimpleFactory;
import ge.devspace.simplemap.entity.SimpleRoad;
import ge.devspace.simplemap.repository.SimpleFactoryRepository;
import ge.devspace.simplemap.repository.SimpleRoadRepository;
import ge.devspace.simplemap.service.GeoServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/geo")
public class SimpleGeoController {

    @Autowired
    private SimpleRoadRepository roadRepository;
    
    @Autowired
    private SimpleFactoryRepository factoryRepository;
    
    @Autowired
    private GeoServerService geoServerService;

    @GetMapping("/roads")
    public Map<String, Object> getRoads() {
        List<SimpleRoad> roads = roadRepository.findAll();
        
        Map<String, Object> geoJson = new HashMap<>();
        geoJson.put("type", "FeatureCollection");
        
        List<Map<String, Object>> features = roads.stream().map(road -> {
            Map<String, Object> feature = new HashMap<>();
            feature.put("type", "Feature");
            
            Map<String, Object> properties = new HashMap<>();
            properties.put("id", road.getId());
            properties.put("name", road.getName());
            properties.put("type", road.getType());
            properties.put("length", road.getLength());
            properties.put("material", road.getMaterial());
            feature.put("properties", properties);
            
            Map<String, Object> geometry = new HashMap<>();
            geometry.put("type", "LineString");
            double[][] coords = {{road.getStartLng(), road.getStartLat()}, {road.getEndLng(), road.getEndLat()}};
            geometry.put("coordinates", coords);
            feature.put("geometry", geometry);
            
            return feature;
        }).collect(Collectors.toList());
        
        geoJson.put("features", features);
        return geoJson;
    }

    @GetMapping("/factories")
    public Map<String, Object> getFactories() {
        List<SimpleFactory> factories = factoryRepository.findAll();
        
        Map<String, Object> geoJson = new HashMap<>();
        geoJson.put("type", "FeatureCollection");
        
        List<Map<String, Object>> features = factories.stream().map(factory -> {
            Map<String, Object> feature = new HashMap<>();
            feature.put("type", "Feature");
            
            Map<String, Object> properties = new HashMap<>();
            properties.put("id", factory.getId());
            properties.put("name", factory.getName());
            properties.put("type", factory.getType());
            properties.put("capacity", factory.getCapacity());
            properties.put("status", factory.getStatus());
            feature.put("properties", properties);
            
            Map<String, Object> geometry = new HashMap<>();
            geometry.put("type", "Point");
            double[] coords = {factory.getLongitude(), factory.getLatitude()};
            geometry.put("coordinates", coords);
            feature.put("geometry", geometry);
            
            return feature;
        }).collect(Collectors.toList());
        
        geoJson.put("features", features);
        return geoJson;
    }

    @GetMapping("/forests")
    public Map<String, Object> getForests() {
        // Return empty forest collection for now
        Map<String, Object> geoJson = new HashMap<>();
        geoJson.put("type", "FeatureCollection");
        geoJson.put("features", List.of());
        return geoJson;
    }

    // Factory CRUD operations
    @PostMapping("/factories")
    public ResponseEntity<SimpleFactory> createFactory(@RequestBody Map<String, Object> factoryData) {
        SimpleFactory factory = new SimpleFactory(
            (String) factoryData.get("name"),
            (String) factoryData.get("type"),
            (String) factoryData.get("status"),
            (Integer) factoryData.get("capacity"),
            (Double) factoryData.get("latitude"),
            (Double) factoryData.get("longitude")
        );
        
        SimpleFactory savedFactory = factoryRepository.save(factory);
        
        // Synchronize with GeoServer
        geoServerService.synchronizeFactory(savedFactory, "create");
        
        return ResponseEntity.ok(savedFactory);
    }

    @PutMapping("/factories/{id}")
    public ResponseEntity<SimpleFactory> updateFactory(@PathVariable Long id, @RequestBody Map<String, Object> factoryData) {
        return factoryRepository.findById(id)
            .map(factory -> {
                factory.setName((String) factoryData.get("name"));
                factory.setType((String) factoryData.get("type"));
                factory.setStatus((String) factoryData.get("status"));
                factory.setCapacity((Integer) factoryData.get("capacity"));
                factory.setLatitude((Double) factoryData.get("latitude"));
                factory.setLongitude((Double) factoryData.get("longitude"));
                
                SimpleFactory updatedFactory = factoryRepository.save(factory);
                
                // Synchronize with GeoServer
                geoServerService.synchronizeFactory(updatedFactory, "update");
                
                return ResponseEntity.ok(updatedFactory);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/factories/{id}")
    public ResponseEntity<?> deleteFactory(@PathVariable Long id) {
        return factoryRepository.findById(id)
            .map(factory -> {
                // Synchronize with GeoServer before deleting
                geoServerService.synchronizeFactory(factory, "delete");
                
                factoryRepository.delete(factory);
                return ResponseEntity.ok().build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/factories/{id}")
    public ResponseEntity<SimpleFactory> getFactory(@PathVariable Long id) {
        return factoryRepository.findById(id)
            .map(factory -> ResponseEntity.ok().body(factory))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/geoserver/status")
    public ResponseEntity<Map<String, Object>> getGeoServerStatus() {
        Map<String, Object> status = new HashMap<>();
        boolean isConnected = geoServerService.testConnection();
        status.put("connected", isConnected);
        status.put("message", isConnected ? "GeoServer-თან კავშირი წარმატებულია" : "GeoServer-თან კავშირი ვერ მოხერხდა");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/geoserver/test-detailed")
    public ResponseEntity<Map<String, Object>> testGeoServerDetailed() {
        Map<String, Object> result = geoServerService.testConnectionDetailed();
        return ResponseEntity.ok(result);
    }

}