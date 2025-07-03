package ge.devspace.gismap.service;

import ge.devspace.gismap.config.GeoServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

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
                style.put("weight", 3);
                style.put("opacity", 0.8);
                break;
            case "factories":
                style.put("color", "#4169E1");
                style.put("fillColor", "#4169E1");
                style.put("fillOpacity", 0.7);
                style.put("radius", 8);
                break;
            default:
                style.put("color", "#000000");
                style.put("weight", 1);
        }
        
        return style;
    }
}