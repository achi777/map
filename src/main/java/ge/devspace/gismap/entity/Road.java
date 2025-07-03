package ge.devspace.gismap.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "roads")
public class Road {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "road_type")
    private String roadType;
    
    @Column(name = "length_km")
    private Double lengthKm;
    
    @Column(name = "surface_type")
    private String surfaceType;
    
    @Column(name = "max_speed")
    private Integer maxSpeed;
    
    @Column(name = "geom", columnDefinition = "geometry(LineString, 4326)")
    @JsonIgnore
    private Geometry geometry;
    
    public Road() {}
    
    public Road(String name, String roadType, Double lengthKm, String surfaceType, Integer maxSpeed, Geometry geometry) {
        this.name = name;
        this.roadType = roadType;
        this.lengthKm = lengthKm;
        this.surfaceType = surfaceType;
        this.maxSpeed = maxSpeed;
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
    
    public String getRoadType() {
        return roadType;
    }
    
    public void setRoadType(String roadType) {
        this.roadType = roadType;
    }
    
    public Double getLengthKm() {
        return lengthKm;
    }
    
    public void setLengthKm(Double lengthKm) {
        this.lengthKm = lengthKm;
    }
    
    public String getSurfaceType() {
        return surfaceType;
    }
    
    public void setSurfaceType(String surfaceType) {
        this.surfaceType = surfaceType;
    }
    
    public Integer getMaxSpeed() {
        return maxSpeed;
    }
    
    public void setMaxSpeed(Integer maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
    
    public Geometry getGeometry() {
        return geometry;
    }
    
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
}