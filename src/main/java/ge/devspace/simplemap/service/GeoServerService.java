package ge.devspace.simplemap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ge.devspace.simplemap.entity.SimpleFactory;
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
}