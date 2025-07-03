# GIS Map Application

Interactive GIS Map using Spring Boot, GeoServer, PostGIS, and Leaflet.js

## 🚀 Quick Start

### Option 1: Using Convenience Scripts (Recommended)

1. **Install dependencies:**
   ```bash
   ./install.sh
   ```

2. **Start the application:**
   ```bash
   ./start.sh
   ```

3. **Access the application:**
   - Map Interface: http://localhost:8081/map
   - GeoServer Admin: http://localhost:8080/geoserver (admin/admin)

4. **Stop the application:**
   ```bash
   ./stop.sh
   ```

### Option 2: Manual Setup

1. **Install prerequisites:**
   - Java 17+
   - Maven 3.6+
   - Docker & Docker Compose

2. **Start the infrastructure:**
   ```bash
   docker-compose up -d
   ```

3. **Build and run the application:**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application:**
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

## 📜 Scripts Reference

### install.sh
Installs all required dependencies and sets up the development environment:
- Docker & Docker Compose
- Java 17
- Maven
- Creates necessary directories

```bash
./install.sh
```

### start.sh
Starts all services in the correct order:
- PostgreSQL/PostGIS database
- GeoServer
- Spring Boot application
- Verifies all services are ready

```bash
./start.sh
```

### stop.sh
Gracefully stops all services:
- Spring Boot application
- Docker containers
- Cleans up log files

```bash
./stop.sh           # Graceful stop
./stop.sh --force   # Force stop all processes
```

## 🗺️ Sample Data

The application includes sample data for Tbilisi, Georgia:
- **8 Factories**: Various industries (wine, steel, textile, automotive, pharmaceutical)
- **6 Major Roads**: Including Rustaveli Ave, Agmashenebeli Ave, Vazha-Pshavela Ave
- **4 Forest Areas**: Mtatsminda Park, Vake Park, Turtle Lake, Lisi Lake

## Author

Archil Odishelidze - Head of IT at DevSpace  
Contact: achi@devspace.ge