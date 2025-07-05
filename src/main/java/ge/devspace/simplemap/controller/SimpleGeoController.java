package ge.devspace.simplemap.controller;

import ge.devspace.simplemap.entity.SimpleFactory;
import ge.devspace.simplemap.entity.SimpleRoad;
import ge.devspace.simplemap.entity.SimpleForest;
import ge.devspace.simplemap.repository.SimpleFactoryRepository;
import ge.devspace.simplemap.repository.SimpleRoadRepository;
import ge.devspace.simplemap.repository.SimpleForestRepository;
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
    private SimpleForestRepository forestRepository;
    
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
        List<SimpleForest> forests = forestRepository.findAll();
        
        Map<String, Object> geoJson = new HashMap<>();
        geoJson.put("type", "FeatureCollection");
        
        List<Map<String, Object>> features = forests.stream().map(forest -> {
            Map<String, Object> feature = new HashMap<>();
            feature.put("type", "Feature");
            
            Map<String, Object> properties = new HashMap<>();
            properties.put("id", forest.getId());
            properties.put("name", forest.getName());
            properties.put("type", forest.getType());
            properties.put("area", forest.getArea());
            properties.put("density", forest.getDensity());
            properties.put("status", forest.getStatus());
            feature.put("properties", properties);
            
            Map<String, Object> geometry = new HashMap<>();
            if (forest.getCoordinates() != null && !forest.getCoordinates().isEmpty()) {
                try {
                    // Parse coordinates JSON string
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Object coords = mapper.readValue(forest.getCoordinates(), Object.class);
                    geometry.put("type", "Polygon");
                    geometry.put("coordinates", coords);
                } catch (Exception e) {
                    // Fallback to point if polygon parsing fails
                    geometry.put("type", "Point");
                    double[] coords = {forest.getCenterLng(), forest.getCenterLat()};
                    geometry.put("coordinates", coords);
                }
            } else {
                geometry.put("type", "Point");
                double[] coords = {forest.getCenterLng(), forest.getCenterLat()};
                geometry.put("coordinates", coords);
            }
            feature.put("geometry", geometry);
            
            return feature;
        }).collect(Collectors.toList());
        
        geoJson.put("features", features);
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

    // Road CRUD operations
    @PostMapping("/roads")
    public ResponseEntity<SimpleRoad> createRoad(@RequestBody Map<String, Object> roadData) {
        SimpleRoad road = new SimpleRoad(
            (String) roadData.get("name"),
            (String) roadData.get("type"),
            (String) roadData.get("material"),
            (Double) roadData.get("startLat"),
            (Double) roadData.get("startLng"),
            (Double) roadData.get("endLat"),
            (Double) roadData.get("endLng"),
            (Double) roadData.get("length")
        );
        
        SimpleRoad savedRoad = roadRepository.save(road);
        
        // Synchronize with GeoServer
        geoServerService.synchronizeRoad(savedRoad, "create");
        
        return ResponseEntity.ok(savedRoad);
    }

    @PutMapping("/roads/{id}")
    public ResponseEntity<SimpleRoad> updateRoad(@PathVariable Long id, @RequestBody Map<String, Object> roadData) {
        return roadRepository.findById(id)
            .map(road -> {
                road.setName((String) roadData.get("name"));
                road.setType((String) roadData.get("type"));
                road.setMaterial((String) roadData.get("material"));
                road.setStartLat((Double) roadData.get("startLat"));
                road.setStartLng((Double) roadData.get("startLng"));
                road.setEndLat((Double) roadData.get("endLat"));
                road.setEndLng((Double) roadData.get("endLng"));
                road.setLength((Double) roadData.get("length"));
                
                SimpleRoad updatedRoad = roadRepository.save(road);
                
                // Synchronize with GeoServer
                geoServerService.synchronizeRoad(updatedRoad, "update");
                
                return ResponseEntity.ok(updatedRoad);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/roads/{id}")
    public ResponseEntity<?> deleteRoad(@PathVariable Long id) {
        return roadRepository.findById(id)
            .map(road -> {
                // Synchronize with GeoServer before deleting
                geoServerService.synchronizeRoad(road, "delete");
                
                roadRepository.delete(road);
                return ResponseEntity.ok().build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/roads/{id}")
    public ResponseEntity<SimpleRoad> getRoad(@PathVariable Long id) {
        return roadRepository.findById(id)
            .map(road -> ResponseEntity.ok().body(road))
            .orElse(ResponseEntity.notFound().build());
    }

    // Forest CRUD operations
    @PostMapping("/forests")
    public ResponseEntity<SimpleForest> createForest(@RequestBody Map<String, Object> forestData) {
        SimpleForest forest = new SimpleForest(
            (String) forestData.get("name"),
            (String) forestData.get("type"),
            (Double) forestData.get("area"),
            (String) forestData.get("density"),
            (String) forestData.get("status"),
            (Double) forestData.get("centerLat"),
            (Double) forestData.get("centerLng"),
            (String) forestData.get("coordinates")
        );
        
        SimpleForest savedForest = forestRepository.save(forest);
        
        // Synchronize with GeoServer
        geoServerService.synchronizeForest(savedForest, "create");
        
        return ResponseEntity.ok(savedForest);
    }

    @PutMapping("/forests/{id}")
    public ResponseEntity<SimpleForest> updateForest(@PathVariable Long id, @RequestBody Map<String, Object> forestData) {
        return forestRepository.findById(id)
            .map(forest -> {
                forest.setName((String) forestData.get("name"));
                forest.setType((String) forestData.get("type"));
                forest.setArea((Double) forestData.get("area"));
                forest.setDensity((String) forestData.get("density"));
                forest.setStatus((String) forestData.get("status"));
                forest.setCenterLat((Double) forestData.get("centerLat"));
                forest.setCenterLng((Double) forestData.get("centerLng"));
                forest.setCoordinates((String) forestData.get("coordinates"));
                
                SimpleForest updatedForest = forestRepository.save(forest);
                
                // Synchronize with GeoServer
                geoServerService.synchronizeForest(updatedForest, "update");
                
                return ResponseEntity.ok(updatedForest);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/forests/{id}")
    public ResponseEntity<?> deleteForest(@PathVariable Long id) {
        return forestRepository.findById(id)
            .map(forest -> {
                // Synchronize with GeoServer before deleting
                geoServerService.synchronizeForest(forest, "delete");
                
                forestRepository.delete(forest);
                return ResponseEntity.ok().build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/forests/{id}")
    public ResponseEntity<SimpleForest> getForest(@PathVariable Long id) {
        return forestRepository.findById(id)
            .map(forest -> ResponseEntity.ok().body(forest))
            .orElse(ResponseEntity.notFound().build());
    }

}