package ge.devspace.gismap.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "forests")
public class Forest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "forest_type")
    private String forestType;
    
    @Column(name = "area_hectares")
    private Double areaHectares;
    
    @Column(name = "protection_status")
    private String protectionStatus;
    
    @Column(name = "geom", columnDefinition = "geometry(Polygon, 4326)")
    @JsonIgnore
    private Geometry geometry;
    
    public Forest() {}
    
    public Forest(String name, String forestType, Double areaHectares, String protectionStatus, Geometry geometry) {
        this.name = name;
        this.forestType = forestType;
        this.areaHectares = areaHectares;
        this.protectionStatus = protectionStatus;
        this.geometry = geometry;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getForestType() {
        return forestType;
    }
    
    public void setForestType(String forestType) {
        this.forestType = forestType;
    }
    
    public Double getAreaHectares() {
        return areaHectares;
    }
    
    public void setAreaHectares(Double areaHectares) {
        this.areaHectares = areaHectares;
    }
    
    public String getProtectionStatus() {
        return protectionStatus;
    }
    
    public void setProtectionStatus(String protectionStatus) {
        this.protectionStatus = protectionStatus;
    }
    
    public Geometry getGeometry() {
        return geometry;
    }
    
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
}