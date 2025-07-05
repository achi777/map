package ge.devspace.simplemap.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "simple_factories")
public class SimpleFactory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String type;
    private String status;
    private Integer capacity;
    private Double latitude;
    private Double longitude;

    public SimpleFactory() {}

    public SimpleFactory(String name, String type, String status, Integer capacity, Double latitude, Double longitude) {
        this.name = name;
        this.type = type;
        this.status = status;
        this.capacity = capacity;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}