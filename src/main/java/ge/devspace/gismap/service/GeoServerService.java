package ge.devspace.gismap.service;

import ge.devspace.gismap.config.GeoServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

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
}