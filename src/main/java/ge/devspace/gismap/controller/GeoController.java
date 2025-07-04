package ge.devspace.gismap.controller;

import ge.devspace.gismap.config.GeoServerConfig;
import ge.devspace.gismap.entity.Forest;
import ge.devspace.gismap.entity.Road;
import ge.devspace.gismap.entity.Factory;
import ge.devspace.gismap.repository.ForestRepository;
import ge.devspace.gismap.repository.RoadRepository;
import ge.devspace.gismap.repository.FactoryRepository;
import ge.devspace.gismap.service.GeoServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.scheduling.annotation.Async;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.WKTWriter;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/geo")
public class GeoController {

    @Autowired
    private GeoServerConfig geoServerConfig;

    @Autowired
    private ForestRepository forestRepository;

    @Autowired
    private RoadRepository roadRepository;

    @Autowired
    private FactoryRepository factoryRepository;

    @Autowired
    private GeoServerService geoServerService;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getGeoConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("geoserverUrl", geoServerConfig.getBaseUrl());
        config.put("workspace", geoServerConfig.getWorkspace());
        return ResponseEntity.ok(config);
    }

    @GetMapping("/layers")
    public ResponseEntity<Map<String, Object>> getLayers() {
        Map<String, Object> response = new HashMap<>();
        response.put("layers", new String[]{"forests", "roads", "factories"});
        return ResponseEntity.ok(response);
    }

    @GetMapping("/forests")
    public ResponseEntity<List<Forest>> getForests() {
        List<Forest> forests = forestRepository.findAll();
        return ResponseEntity.ok(forests);
    }

    @GetMapping("/roads")
    public ResponseEntity<List<Road>> getRoads() {
        List<Road> roads = roadRepository.findAll();
        return ResponseEntity.ok(roads);
    }

    @GetMapping("/factories")
    public ResponseEntity<List<Factory>> getFactories() {
        List<Factory> factories = factoryRepository.findAll();
        return ResponseEntity.ok(factories);
    }

    @GetMapping("/forests/type/{forestType}")
    public ResponseEntity<List<Forest>> getForestsByType(@PathVariable String forestType) {
        List<Forest> forests = forestRepository.findByForestType(forestType);
        return ResponseEntity.ok(forests);
    }

    @GetMapping("/roads/type/{roadType}")
    public ResponseEntity<List<Road>> getRoadsByType(@PathVariable String roadType) {
        List<Road> roads = roadRepository.findByRoadType(roadType);
        return ResponseEntity.ok(roads);
    }

    @GetMapping("/factories/industry/{industryType}")
    public ResponseEntity<List<Factory>> getFactoriesByIndustry(@PathVariable String industryType) {
        List<Factory> factories = factoryRepository.findByIndustryType(industryType);
        return ResponseEntity.ok(factories);
    }

    @GetMapping("/layers/available")
    public ResponseEntity<List<Map<String, Object>>> getAvailableLayers() {
        List<Map<String, Object>> layers = geoServerService.getAvailableLayers();
        return ResponseEntity.ok(layers);
    }

    @GetMapping("/layers/{layerName}/style")
    public ResponseEntity<Map<String, Object>> getLayerStyle(@PathVariable String layerName) {
        Map<String, Object> style = geoServerService.getLayerStyle(layerName);
        return ResponseEntity.ok(style);
    }

    @GetMapping("/forests/geojson")
    public ResponseEntity<Map<String, Object>> getForestsGeoJSON() {
        List<Forest> forests = forestRepository.findAll();
        Map<String, Object> geoJson = createGeoJSON(forests, "forests");
        return ResponseEntity.ok(geoJson);
    }

    @GetMapping("/roads/geojson")
    public ResponseEntity<Map<String, Object>> getRoadsGeoJSON() {
        List<Road> roads = roadRepository.findAll();
        Map<String, Object> geoJson = createGeoJSON(roads, "roads");
        return ResponseEntity.ok(geoJson);
    }

    @GetMapping("/factories/geojson")
    public ResponseEntity<Map<String, Object>> getFactoriesGeoJSON() {
        List<Factory> factories = factoryRepository.findAll();
        Map<String, Object> geoJson = createGeoJSON(factories, "factories");
        return ResponseEntity.ok(geoJson);
    }

    private Map<String, Object> createGeoJSON(List<?> entities, String layerType) {
        Map<String, Object> geoJson = new HashMap<>();
        geoJson.put("type", "FeatureCollection");
        
        List<Map<String, Object>> features = new ArrayList<>();
        WKTWriter wktWriter = new WKTWriter();
        
        for (Object entity : entities) {
            Map<String, Object> feature = new HashMap<>();
            feature.put("type", "Feature");
            
            Map<String, Object> properties = new HashMap<>();
            Map<String, Object> geometry = new HashMap<>();
            
            if (entity instanceof Forest) {
                Forest forest = (Forest) entity;
                properties.put("id", forest.getId());
                properties.put("name", forest.getName());
                properties.put("forestType", forest.getForestType());
                properties.put("areaHectares", forest.getAreaHectares());
                properties.put("protectionStatus", forest.getProtectionStatus());
                geometry = createGeometryJSON(forest.getGeometry());
            } else if (entity instanceof Road) {
                Road road = (Road) entity;
                properties.put("id", road.getId());
                properties.put("name", road.getName());
                properties.put("roadType", road.getRoadType());
                properties.put("lengthKm", road.getLengthKm());
                properties.put("surfaceType", road.getSurfaceType());
                properties.put("maxSpeed", road.getMaxSpeed());
                geometry = createGeometryJSON(road.getGeometry());
            } else if (entity instanceof Factory) {
                Factory factory = (Factory) entity;
                properties.put("id", factory.getId());
                properties.put("name", factory.getName());
                properties.put("industryType", factory.getIndustryType());
                properties.put("capacity", factory.getCapacity());
                properties.put("establishedYear", factory.getEstablishedYear());
                properties.put("status", factory.getStatus());
                geometry = createGeometryJSON(factory.getGeometry());
            }
            
            feature.put("properties", properties);
            feature.put("geometry", geometry);
            features.add(feature);
        }
        
        geoJson.put("features", features);
        return geoJson;
    }

    private Map<String, Object> createGeometryJSON(Geometry geom) {
        Map<String, Object> geometry = new HashMap<>();
        
        if (geom.getGeometryType().equals("Point")) {
            geometry.put("type", "Point");
            geometry.put("coordinates", new double[]{geom.getCoordinate().x, geom.getCoordinate().y});
        } else if (geom.getGeometryType().equals("LineString")) {
            geometry.put("type", "LineString");
            List<double[]> coordinates = new ArrayList<>();
            for (int i = 0; i < geom.getNumPoints(); i++) {
                coordinates.add(new double[]{geom.getCoordinates()[i].x, geom.getCoordinates()[i].y});
            }
            geometry.put("coordinates", coordinates);
        } else if (geom.getGeometryType().equals("Polygon")) {
            geometry.put("type", "Polygon");
            List<List<double[]>> coordinates = new ArrayList<>();
            List<double[]> ring = new ArrayList<>();
            for (int i = 0; i < geom.getNumPoints(); i++) {
                ring.add(new double[]{geom.getCoordinates()[i].x, geom.getCoordinates()[i].y});
            }
            coordinates.add(ring);
            geometry.put("coordinates", coordinates);
        }
        
        return geometry;
    }

    // CRUD Operations for Factories
    
    @PostMapping("/factories")
    public ResponseEntity<Factory> createFactory(@RequestBody Map<String, Object> factoryData) {
        try {
            Factory factory = new Factory();
            factory.setName((String) factoryData.get("name"));
            factory.setIndustryType((String) factoryData.get("industryType"));
            factory.setCapacity((Integer) factoryData.get("capacity"));
            factory.setEstablishedYear((Integer) factoryData.get("establishedYear"));
            factory.setStatus((String) factoryData.get("status"));
            
            // Create geometry from coordinates
            Double longitude = (Double) factoryData.get("longitude");
            Double latitude = (Double) factoryData.get("latitude");
            Geometry geometry = geometryFactory.createPoint(new Coordinate(longitude, latitude));
            factory.setGeometry(geometry);
            
            Factory savedFactory = factoryRepository.save(factory);
            
            // Sync with GeoServer asynchronously
            syncFactoryLayerAsync();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFactory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/factories/{id}")
    public ResponseEntity<Map<String, Object>> getFactory(@PathVariable Long id) {
        Optional<Factory> factory = factoryRepository.findById(id);
        if (factory.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            Factory f = factory.get();
            response.put("id", f.getId());
            response.put("name", f.getName());
            response.put("industryType", f.getIndustryType());
            response.put("capacity", f.getCapacity());
            response.put("establishedYear", f.getEstablishedYear());
            response.put("status", f.getStatus());
            response.put("longitude", f.getGeometry().getCoordinate().x);
            response.put("latitude", f.getGeometry().getCoordinate().y);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }
    
    @PutMapping("/factories/{id}")
    public ResponseEntity<Factory> updateFactory(@PathVariable Long id, @RequestBody Map<String, Object> factoryData) {
        Optional<Factory> existingFactory = factoryRepository.findById(id);
        if (existingFactory.isPresent()) {
            try {
                Factory factory = existingFactory.get();
                factory.setName((String) factoryData.get("name"));
                factory.setIndustryType((String) factoryData.get("industryType"));
                factory.setCapacity((Integer) factoryData.get("capacity"));
                factory.setEstablishedYear((Integer) factoryData.get("establishedYear"));
                factory.setStatus((String) factoryData.get("status"));
                
                // Update geometry if coordinates provided
                if (factoryData.containsKey("longitude") && factoryData.containsKey("latitude")) {
                    Double longitude = (Double) factoryData.get("longitude");
                    Double latitude = (Double) factoryData.get("latitude");
                    Geometry geometry = geometryFactory.createPoint(new Coordinate(longitude, latitude));
                    factory.setGeometry(geometry);
                }
                
                Factory savedFactory = factoryRepository.save(factory);
                
                // Sync with GeoServer asynchronously
                syncFactoryLayerAsync();
                
                return ResponseEntity.ok(savedFactory);
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/factories/{id}")
    public ResponseEntity<Map<String, Object>> deleteFactory(@PathVariable Long id) {
        if (factoryRepository.existsById(id)) {
            factoryRepository.deleteById(id);
            
            // Sync with GeoServer asynchronously
            syncFactoryLayerAsync();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Factory deleted successfully");
            response.put("id", id);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }
    
    @Async
    private void syncFactoryLayerAsync() {
        try {
            Thread.sleep(1000); // Small delay to ensure database transaction is committed
            geoServerService.syncLayerToGeoServer("factories");
        } catch (Exception e) {
            System.err.println("Error syncing factory layer to GeoServer: " + e.getMessage());
        }
    }
}