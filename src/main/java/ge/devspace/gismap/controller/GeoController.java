package ge.devspace.gismap.controller;

import ge.devspace.gismap.config.GeoServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/geo")
public class GeoController {

    @Autowired
    private GeoServerConfig geoServerConfig;

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
        response.put("layers", new String[]{"your_layer"});
        return ResponseEntity.ok(response);
    }
}