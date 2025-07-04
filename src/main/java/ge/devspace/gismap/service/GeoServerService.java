package ge.devspace.gismap.service;

import ge.devspace.gismap.config.GeoServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

@Service
public class GeoServerService {

    @Autowired
    private GeoServerConfig geoServerConfig;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getWmsUrl(String layerName) {
        return String.format("%s/%s/wms", 
            geoServerConfig.getBaseUrl(), 
            geoServerConfig.getWorkspace());
    }

    public String getWfsUrl(String layerName) {
        return String.format("%s/%s/wfs", 
            geoServerConfig.getBaseUrl(), 
            geoServerConfig.getWorkspace());
    }

    public Map<String, Object> getLayerCapabilities(String layerName) {
        String url = getWmsUrl(layerName) + "?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetCapabilities";
        Map<String, Object> response = new HashMap<>();
        response.put("capabilitiesUrl", url);
        response.put("layerName", layerName);
        return response;
    }

    public String getFeatureInfo(String layerName, double lat, double lon, int width, int height) {
        return String.format("%s?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetFeatureInfo&" +
            "LAYERS=%s&QUERY_LAYERS=%s&INFO_FORMAT=application/json&" +
            "X=%d&Y=%d&WIDTH=%d&HEIGHT=%d&SRS=EPSG:4326",
            getWmsUrl(layerName), layerName, layerName, 
            (int)lon, (int)lat, width, height);
    }

    public List<Map<String, Object>> getAvailableLayers() {
        List<Map<String, Object>> layers = new ArrayList<>();
        
        Map<String, Object> forestLayer = new HashMap<>();
        forestLayer.put("name", "forests");
        forestLayer.put("displayName", "Forest Areas");
        forestLayer.put("type", "polygon");
        forestLayer.put("color", "#228B22");
        forestLayer.put("wmsUrl", getWmsUrl("forests"));
        forestLayer.put("wfsUrl", getWfsUrl("forests"));
        layers.add(forestLayer);
        
        Map<String, Object> roadLayer = new HashMap<>();
        roadLayer.put("name", "roads");
        roadLayer.put("displayName", "Roads");
        roadLayer.put("type", "linestring");
        roadLayer.put("color", "#FF6347");
        roadLayer.put("wmsUrl", getWmsUrl("roads"));
        roadLayer.put("wfsUrl", getWfsUrl("roads"));
        layers.add(roadLayer);
        
        Map<String, Object> factoryLayer = new HashMap<>();
        factoryLayer.put("name", "factories");
        factoryLayer.put("displayName", "Factories");
        factoryLayer.put("type", "point");
        factoryLayer.put("color", "#4169E1");
        factoryLayer.put("wmsUrl", getWmsUrl("factories"));
        factoryLayer.put("wfsUrl", getWfsUrl("factories"));
        layers.add(factoryLayer);
        
        return layers;
    }

    public Map<String, Object> getLayerStyle(String layerName) {
        Map<String, Object> style = new HashMap<>();
        
        switch (layerName) {
            case "forests":
                style.put("fillColor", "#228B22");
                style.put("fillOpacity", 0.6);
                style.put("color", "#006400");
                style.put("weight", 2);
                break;
            case "roads":
                style.put("color", "#FF6347");
                style.put("weight", 5);
                style.put("opacity", 0.8);
                break;
            case "factories":
                style.put("color", "#4169E1");
                style.put("fillColor", "#4169E1");
                style.put("fillOpacity", 0.8);
                style.put("radius", 12);
                break;
            default:
                style.put("color", "#000000");
                style.put("weight", 1);
        }
        
        return style;
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = geoServerConfig.getUsername() + ":" + geoServerConfig.getPassword();
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public boolean createWorkspace() {
        try {
            String url = geoServerConfig.getBaseUrl() + "/rest/workspaces";
            HttpHeaders headers = createAuthHeaders();
            
            Map<String, Object> workspace = new HashMap<>();
            Map<String, String> workspaceData = new HashMap<>();
            workspaceData.put("name", geoServerConfig.getWorkspace());
            workspace.put("workspace", workspaceData);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(workspace, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Error creating workspace: " + e.getMessage());
            return false;
        }
    }

    public boolean createDataStore() {
        try {
            String url = geoServerConfig.getBaseUrl() + "/rest/workspaces/" + 
                        geoServerConfig.getWorkspace() + "/datastores";
            HttpHeaders headers = createAuthHeaders();
            
            Map<String, Object> dataStore = new HashMap<>();
            Map<String, Object> dataStoreData = new HashMap<>();
            dataStoreData.put("name", "postgis_store");
            dataStoreData.put("type", "PostGIS");
            dataStoreData.put("enabled", true);
            
            Map<String, Object> connectionParams = new HashMap<>();
            connectionParams.put("host", "db");
            connectionParams.put("port", "5432");
            connectionParams.put("database", "gisdb");
            connectionParams.put("user", "gisuser");
            connectionParams.put("passwd", "gispassword");
            connectionParams.put("dbtype", "postgis");
            connectionParams.put("schema", "public");
            
            dataStoreData.put("connectionParameters", connectionParams);
            dataStore.put("dataStore", dataStoreData);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(dataStore, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Error creating datastore: " + e.getMessage());
            return false;
        }
    }

    public boolean publishLayer(String tableName, String layerName) {
        try {
            // First create feature type
            String url = geoServerConfig.getBaseUrl() + "/rest/workspaces/" + 
                        geoServerConfig.getWorkspace() + "/datastores/postgis_store/featuretypes";
            HttpHeaders headers = createAuthHeaders();
            
            Map<String, Object> featureType = new HashMap<>();
            Map<String, Object> featureTypeData = new HashMap<>();
            featureTypeData.put("name", layerName);
            featureTypeData.put("nativeName", tableName);
            featureTypeData.put("title", layerName);
            featureTypeData.put("srs", "EPSG:4326");
            featureTypeData.put("enabled", true);
            
            featureType.put("featureType", featureTypeData);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(featureType, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Error publishing layer: " + e.getMessage());
            return false;
        }
    }

    public boolean setupGeoServer() {
        System.out.println("Setting up GeoServer integration...");
        
        boolean workspaceCreated = createWorkspace();
        if (!workspaceCreated) {
            System.out.println("Workspace already exists or creation failed, continuing...");
        }
        
        boolean dataStoreCreated = createDataStore();
        if (!dataStoreCreated) {
            System.out.println("DataStore already exists or creation failed, continuing...");
        }
        
        boolean factoriesPublished = publishLayer("factories", "factories");
        boolean roadsPublished = publishLayer("roads", "roads");
        boolean forestsPublished = publishLayer("forests", "forests");
        
        if (factoriesPublished && roadsPublished && forestsPublished) {
            System.out.println("✅ All layers published to GeoServer successfully!");
            return true;
        } else {
            System.out.println("⚠️ Some layers may already exist in GeoServer");
            return true; // Continue anyway
        }
    }

    public void syncLayerToGeoServer(String layerName) {
        publishLayer(layerName, layerName);
    }
}