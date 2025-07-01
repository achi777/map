# GIS Map Application

Interactive GIS Map using Spring Boot, GeoServer, PostGIS, and Leaflet.js

## Prerequisites

- Java 17+
- Maven 3.6+
- Docker & Docker Compose

## Quick Start

1. **Start the infrastructure:**
   ```bash
   docker-compose up -d
   ```

2. **Build and run the application:**
   ```bash
   mvn spring-boot:run
   ```

3. **Access the application:**
   - Map Interface: http://localhost:8081/map
   - GeoServer Admin: http://localhost:8080/geoserver (admin/admin)

## Configuration

### GeoServer Setup

1. Access GeoServer at http://localhost:8080/geoserver
2. Login with admin/admin
3. Create workspace: `gisproject`
4. Add PostGIS store:
   - Host: `db`
   - Port: `5432`
   - Database: `gisdb`
   - User: `gisuser`
   - Password: `gispassword`

### Loading Sample Data

```bash
# Import shapefile to PostGIS
shp2pgsql -I -s 4326 path/to/your_data.shp public.your_layer | psql -h localhost -U gisuser -d gisdb
```

## API Endpoints

- `GET /api/geo/config` - Get GeoServer configuration
- `GET /api/geo/layers` - List available layers

## Development

The application follows standard Spring Boot structure:
- Controllers: `src/main/java/ge/devspace/gismap/controller/`
- Services: `src/main/java/ge/devspace/gismap/service/`
- Configuration: `src/main/java/ge/devspace/gismap/config/`
- Templates: `src/main/resources/templates/`

## Author

Archil Odishelidze - Head of IT at DevSpace  
Contact: achi@devspace.ge