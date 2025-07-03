package ge.devspace.gismap.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "factories")
public class Factory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "industry_type")
    private String industryType;
    
    @Column(name = "capacity")
    private Integer capacity;
    
    @Column(name = "established_year")
    private Integer establishedYear;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "geom", columnDefinition = "geometry(Point, 4326)")
    @JsonIgnore
    private Geometry geometry;
    
    public Factory() {}
    
    public Factory(String name, String industryType, Integer capacity, Integer establishedYear, String status, Geometry geometry) {
        this.name = name;
        this.industryType = industryType;
        this.capacity = capacity;
        this.establishedYear = establishedYear;
        this.status = status;
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
    
    public String getIndustryType() {
        return industryType;
    }
    
    public void setIndustryType(String industryType) {
        this.industryType = industryType;
    }
    
    public Integer getCapacity() {
        return capacity;
    }
    
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    
    public Integer getEstablishedYear() {
        return establishedYear;
    }
    
    public void setEstablishedYear(Integer establishedYear) {
        this.establishedYear = establishedYear;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Geometry getGeometry() {
        return geometry;
    }
    
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
}