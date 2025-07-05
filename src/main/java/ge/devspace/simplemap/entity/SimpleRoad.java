package ge.devspace.simplemap.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "simple_roads")
public class SimpleRoad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String type;
    private String material;
    private Double startLat;
    private Double startLng;
    private Double endLat;
    private Double endLng;
    private Double length;

    public SimpleRoad() {}

    public SimpleRoad(String name, String type, String material, Double startLat, Double startLng, Double endLat, Double endLng, Double length) {
        this.name = name;
        this.type = type;
        this.material = material;
        this.startLat = startLat;
        this.startLng = startLng;
        this.endLat = endLat;
        this.endLng = endLng;
        this.length = length;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public Double getStartLat() { return startLat; }
    public void setStartLat(Double startLat) { this.startLat = startLat; }

    public Double getStartLng() { return startLng; }
    public void setStartLng(Double startLng) { this.startLng = startLng; }

    public Double getEndLat() { return endLat; }
    public void setEndLat(Double endLat) { this.endLat = endLat; }

    public Double getEndLng() { return endLng; }
    public void setEndLng(Double endLng) { this.endLng = endLng; }

    public Double getLength() { return length; }
    public void setLength(Double length) { this.length = length; }
}