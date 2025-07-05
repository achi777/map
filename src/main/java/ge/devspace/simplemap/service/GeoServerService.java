package ge.devspace.simplemap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ge.devspace.simplemap.entity.SimpleFactory;
import ge.devspace.simplemap.entity.SimpleRoad;
import ge.devspace.simplemap.entity.SimpleForest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeoServerService {

    @Value("${geoserver.url:http://localhost:8080/geoserver}")
    private String geoServerUrl;

    @Value("${geoserver.username:admin}")
    private String geoServerUsername;

    @Value("${geoserver.password:geoserver}")
    private String geoServerPassword;

    @Value("${geoserver.workspace:simple_map}")
    private String workspace;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeoServerService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String auth = Base64.getEncoder().encodeToString((geoServerUsername + ":" + geoServerPassword).getBytes());
        headers.set("Authorization", "Basic " + auth);
        return headers;
    }

    public void synchronizeFactory(SimpleFactory factory, String operation) {
        try {
            switch (operation.toLowerCase()) {
                case "create":
                case "update":
                    createOrUpdateFactoryInGeoServer(factory);
                    break;
                case "delete":
                    deleteFactoryFromGeoServer(factory.getId());
                    break;
            }
        } catch (Exception e) {
            System.err.println("GeoServer synchronization failed: " + e.getMessage());
        }
    }

    private void createOrUpdateFactoryInGeoServer(SimpleFactory factory) {
        try {
            // Create GeoJSON feature for the factory
            Map<String, Object> feature = createGeoJsonFeature(factory);
            
            // For simplicity, we'll create a WFS-T transaction
            // In a real implementation, you would use proper WFS-T XML
            String transactionXml = createWfsTransaction(factory, "update");
            
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            
            HttpEntity<String> request = new HttpEntity<>(transactionXml, headers);
            
            String url = geoServerUrl + "/wfs";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Factory synchronized with GeoServer successfully");
            }
        } catch (Exception e) {
            System.err.println("Failed to sync factory with GeoServer: " + e.getMessage());
        }
    }

    private void deleteFactoryFromGeoServer(Long factoryId) {
        try {
            String transactionXml = createWfsDeleteTransaction(factoryId);
            
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            
            HttpEntity<String> request = new HttpEntity<>(transactionXml, headers);
            
            String url = geoServerUrl + "/wfs";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Factory deleted from GeoServer successfully");
            }
        } catch (Exception e) {
            System.err.println("Failed to delete factory from GeoServer: " + e.getMessage());
        }
    }

    private Map<String, Object> createGeoJsonFeature(SimpleFactory factory) {
        Map<String, Object> feature = new HashMap<>();
        feature.put("type", "Feature");
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("id", factory.getId());
        properties.put("name", factory.getName());
        properties.put("type", factory.getType());
        properties.put("status", factory.getStatus());
        properties.put("capacity", factory.getCapacity());
        feature.put("properties", properties);
        
        Map<String, Object> geometry = new HashMap<>();
        geometry.put("type", "Point");
        geometry.put("coordinates", Arrays.asList(factory.getLongitude(), factory.getLatitude()));
        feature.put("geometry", geometry);
        
        return feature;
    }

    private String createWfsTransaction(SimpleFactory factory, String operation) {
        // Simplified WFS-T transaction XML
        // In a real implementation, you would create proper WFS-T XML with namespace declarations
        return String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <wfs:Transaction version="1.1.0" service="WFS"
                xmlns:wfs="http://www.opengis.net/wfs"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:simple_map="%s/simple_map">
                <wfs:Insert>
                    <simple_map:factories>
                        <simple_map:id>%d</simple_map:id>
                        <simple_map:name>%s</simple_map:name>
                        <simple_map:type>%s</simple_map:type>
                        <simple_map:status>%s</simple_map:status>
                        <simple_map:capacity>%d</simple_map:capacity>
                        <simple_map:geom>
                            <gml:Point srsName="EPSG:4326">
                                <gml:coordinates>%f,%f</gml:coordinates>
                            </gml:Point>
                        </simple_map:geom>
                    </simple_map:factories>
                </wfs:Insert>
            </wfs:Transaction>
            """, 
            geoServerUrl, 
            factory.getId(),
            escapeXml(factory.getName()),
            escapeXml(factory.getType()),
            escapeXml(factory.getStatus()),
            factory.getCapacity(),
            factory.getLongitude(),
            factory.getLatitude()
        );
    }

    private String createWfsDeleteTransaction(Long factoryId) {
        return String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <wfs:Transaction version="1.1.0" service="WFS"
                xmlns:wfs="http://www.opengis.net/wfs"
                xmlns:ogc="http://www.opengis.net/ogc"
                xmlns:simple_map="%s/simple_map">
                <wfs:Delete typeName="simple_map:factories">
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>id</ogc:PropertyName>
                            <ogc:Literal>%d</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
                    </ogc:Filter>
                </wfs:Delete>
            </wfs:Transaction>
            """, 
            geoServerUrl, 
            factoryId
        );
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }

    public boolean testConnection() {
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            String url = geoServerUrl + "/rest/workspaces.json";
            System.out.println("Testing GeoServer connection to: " + url);
            System.out.println("Using credentials: " + geoServerUsername + "/***");
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            
            System.out.println("Response Status: " + response.getStatusCode());
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("GeoServer connection successful!");
            } else {
                System.out.println("GeoServer connection failed with status: " + response.getStatusCode());
                System.out.println("Response Body: " + response.getBody());
            }
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("GeoServer connection test failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            
            // Log more details for HTTP errors
            if (e instanceof org.springframework.web.client.HttpClientErrorException) {
                org.springframework.web.client.HttpClientErrorException httpError = 
                    (org.springframework.web.client.HttpClientErrorException) e;
                System.err.println("HTTP Status Code: " + httpError.getStatusCode());
                System.err.println("Response Body: " + httpError.getResponseBodyAsString());
            }
            
            return false;
        }
    }
    
    public Map<String, Object> testConnectionDetailed() {
        Map<String, Object> result = new HashMap<>();
        result.put("geoserver_url", geoServerUrl);
        result.put("username", geoServerUsername);
        result.put("workspace", workspace);
        result.put("timestamp", new Date().toString());
        
        try {
            // Test 1: Basic connectivity
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            String url = geoServerUrl + "/rest/workspaces.json";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            
            result.put("success", true);
            result.put("statusCode", response.getStatusCode().value());
            result.put("statusText", response.getStatusCode().toString());
            result.put("responseBody", response.getBody());
            
            // Test 2: Parse response
            try {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                result.put("workspaces", jsonResponse.get("workspaces"));
            } catch (Exception e) {
                result.put("parseError", e.getMessage());
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            
            if (e instanceof org.springframework.web.client.HttpClientErrorException) {
                org.springframework.web.client.HttpClientErrorException httpError = 
                    (org.springframework.web.client.HttpClientErrorException) e;
                result.put("httpStatusCode", httpError.getStatusCode().value());
                result.put("httpStatusText", httpError.getStatusCode().toString());
                result.put("httpResponseBody", httpError.getResponseBodyAsString());
            }
        }
        
        return result;
    }

    // Road synchronization methods
    public void synchronizeRoad(SimpleRoad road, String operation) {
        try {
            switch (operation.toLowerCase()) {
                case "create":
                case "update":
                    createOrUpdateRoadInGeoServer(road);
                    break;
                case "delete":
                    deleteRoadFromGeoServer(road.getId());
                    break;
            }
        } catch (Exception e) {
            System.err.println("GeoServer road synchronization failed: " + e.getMessage());
        }
    }

    private void createOrUpdateRoadInGeoServer(SimpleRoad road) {
        try {
            String transactionXml = createRoadWfsTransaction(road);
            
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            
            HttpEntity<String> request = new HttpEntity<>(transactionXml, headers);
            
            String url = geoServerUrl + "/wfs";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Road synchronized with GeoServer successfully");
            }
        } catch (Exception e) {
            System.err.println("Failed to sync road with GeoServer: " + e.getMessage());
        }
    }

    private void deleteRoadFromGeoServer(Long roadId) {
        try {
            String transactionXml = createRoadDeleteTransaction(roadId);
            
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            
            HttpEntity<String> request = new HttpEntity<>(transactionXml, headers);
            
            String url = geoServerUrl + "/wfs";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Road deleted from GeoServer successfully");
            }
        } catch (Exception e) {
            System.err.println("Failed to delete road from GeoServer: " + e.getMessage());
        }
    }

    private String createRoadWfsTransaction(SimpleRoad road) {
        return String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <wfs:Transaction version="1.1.0" service="WFS"
                xmlns:wfs="http://www.opengis.net/wfs"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:simple_map="%s/simple_map">
                <wfs:Insert>
                    <simple_map:roads>
                        <simple_map:id>%d</simple_map:id>
                        <simple_map:name>%s</simple_map:name>
                        <simple_map:type>%s</simple_map:type>
                        <simple_map:material>%s</simple_map:material>
                        <simple_map:length>%f</simple_map:length>
                        <simple_map:geom>
                            <gml:LineString srsName="EPSG:4326">
                                <gml:coordinates>%f,%f %f,%f</gml:coordinates>
                            </gml:LineString>
                        </simple_map:geom>
                    </simple_map:roads>
                </wfs:Insert>
            </wfs:Transaction>
            """, 
            geoServerUrl, 
            road.getId(),
            escapeXml(road.getName()),
            escapeXml(road.getType()),
            escapeXml(road.getMaterial()),
            road.getLength(),
            road.getStartLng(),
            road.getStartLat(),
            road.getEndLng(),
            road.getEndLat()
        );
    }

    private String createRoadDeleteTransaction(Long roadId) {
        return String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <wfs:Transaction version="1.1.0" service="WFS"
                xmlns:wfs="http://www.opengis.net/wfs"
                xmlns:ogc="http://www.opengis.net/ogc"
                xmlns:simple_map="%s/simple_map">
                <wfs:Delete typeName="simple_map:roads">
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>id</ogc:PropertyName>
                            <ogc:Literal>%d</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
                    </ogc:Filter>
                </wfs:Delete>
            </wfs:Transaction>
            """, 
            geoServerUrl, 
            roadId
        );
    }

    // Forest synchronization methods
    public void synchronizeForest(SimpleForest forest, String operation) {
        try {
            switch (operation.toLowerCase()) {
                case "create":
                case "update":
                    createOrUpdateForestInGeoServer(forest);
                    break;
                case "delete":
                    deleteForestFromGeoServer(forest.getId());
                    break;
            }
        } catch (Exception e) {
            System.err.println("GeoServer forest synchronization failed: " + e.getMessage());
        }
    }

    private void createOrUpdateForestInGeoServer(SimpleForest forest) {
        try {
            String transactionXml = createForestWfsTransaction(forest);
            
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            
            HttpEntity<String> request = new HttpEntity<>(transactionXml, headers);
            
            String url = geoServerUrl + "/wfs";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Forest synchronized with GeoServer successfully");
            }
        } catch (Exception e) {
            System.err.println("Failed to sync forest with GeoServer: " + e.getMessage());
        }
    }

    private void deleteForestFromGeoServer(Long forestId) {
        try {
            String transactionXml = createForestDeleteTransaction(forestId);
            
            HttpHeaders headers = createAuthHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            
            HttpEntity<String> request = new HttpEntity<>(transactionXml, headers);
            
            String url = geoServerUrl + "/wfs";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Forest deleted from GeoServer successfully");
            }
        } catch (Exception e) {
            System.err.println("Failed to delete forest from GeoServer: " + e.getMessage());
        }
    }

    private String createForestWfsTransaction(SimpleForest forest) {
        // Use polygon coordinates if available, otherwise create a simple polygon around center point
        String geometryXml;
        if (forest.getCoordinates() != null && !forest.getCoordinates().isEmpty()) {
            // Parse and use provided coordinates
            geometryXml = String.format("""
                <simple_map:geom>
                    <gml:Polygon srsName="EPSG:4326">
                        <gml:exterior>
                            <gml:LinearRing>
                                <gml:coordinates>%s</gml:coordinates>
                            </gml:LinearRing>
                        </gml:exterior>
                    </gml:Polygon>
                </simple_map:geom>
                """, parseCoordinatesForGml(forest.getCoordinates()));
        } else {
            // Create a simple square around the center point
            double lat = forest.getCenterLat();
            double lng = forest.getCenterLng();
            double offset = 0.001; // Small offset for polygon
            geometryXml = String.format("""
                <simple_map:geom>
                    <gml:Polygon srsName="EPSG:4326">
                        <gml:exterior>
                            <gml:LinearRing>
                                <gml:coordinates>%f,%f %f,%f %f,%f %f,%f %f,%f</gml:coordinates>
                            </gml:LinearRing>
                        </gml:exterior>
                    </gml:Polygon>
                </simple_map:geom>
                """, 
                lng-offset, lat-offset,
                lng+offset, lat-offset,
                lng+offset, lat+offset,
                lng-offset, lat+offset,
                lng-offset, lat-offset
            );
        }

        return String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <wfs:Transaction version="1.1.0" service="WFS"
                xmlns:wfs="http://www.opengis.net/wfs"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:simple_map="%s/simple_map">
                <wfs:Insert>
                    <simple_map:forests>
                        <simple_map:id>%d</simple_map:id>
                        <simple_map:name>%s</simple_map:name>
                        <simple_map:type>%s</simple_map:type>
                        <simple_map:area>%f</simple_map:area>
                        <simple_map:density>%s</simple_map:density>
                        <simple_map:status>%s</simple_map:status>
                        %s
                    </simple_map:forests>
                </wfs:Insert>
            </wfs:Transaction>
            """, 
            geoServerUrl, 
            forest.getId(),
            escapeXml(forest.getName()),
            escapeXml(forest.getType()),
            forest.getArea(),
            escapeXml(forest.getDensity()),
            escapeXml(forest.getStatus()),
            geometryXml
        );
    }

    private String createForestDeleteTransaction(Long forestId) {
        return String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <wfs:Transaction version="1.1.0" service="WFS"
                xmlns:wfs="http://www.opengis.net/wfs"
                xmlns:ogc="http://www.opengis.net/ogc"
                xmlns:simple_map="%s/simple_map">
                <wfs:Delete typeName="simple_map:forests">
                    <ogc:Filter>
                        <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>id</ogc:PropertyName>
                            <ogc:Literal>%d</ogc:Literal>
                        </ogc:PropertyIsEqualTo>
                    </ogc:Filter>
                </wfs:Delete>
            </wfs:Transaction>
            """, 
            geoServerUrl, 
            forestId
        );
    }

    private String parseCoordinatesForGml(String coordinatesJson) {
        try {
            // Parse JSON coordinates and convert to GML format
            ObjectMapper mapper = new ObjectMapper();
            Object coords = mapper.readValue(coordinatesJson, Object.class);
            // This is a simplified implementation - in production you'd want more robust parsing
            return coordinatesJson.replaceAll("[\\[\\]\"]", "").replace(",", " ");
        } catch (Exception e) {
            System.err.println("Failed to parse coordinates: " + e.getMessage());
            return "";
        }
    }
}