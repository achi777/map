package ge.devspace.simplemap.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "simple_forests")
public class SimpleForest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String type;
    private Double area;
    private String density;
    private String status;
    private Double centerLat;
    private Double centerLng;
    private String coordinates; // JSON string for polygon coordinates

    public SimpleForest() {}

    public SimpleForest(String name, String type, Double area, String density, String status, 
                       Double centerLat, Double centerLng, String coordinates) {
        this.name = name;
        this.type = type;
        this.area = area;
        this.density = density;
        this.status = status;
        this.centerLat = centerLat;
        this.centerLng = centerLng;
        this.coordinates = coordinates;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getArea() { return area; }
    public void setArea(Double area) { this.area = area; }

    public String getDensity() { return density; }
    public void setDensity(String density) { this.density = density; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getCenterLat() { return centerLat; }
    public void setCenterLat(Double centerLat) { this.centerLat = centerLat; }

    public Double getCenterLng() { return centerLng; }
    public void setCenterLng(Double centerLng) { this.centerLng = centerLng; }

    public String getCoordinates() { return coordinates; }
    public void setCoordinates(String coordinates) { this.coordinates = coordinates; }
}