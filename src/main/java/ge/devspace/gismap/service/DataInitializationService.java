package ge.devspace.gismap.service;

import ge.devspace.gismap.entity.Factory;
import ge.devspace.gismap.entity.Road;
import ge.devspace.gismap.entity.Forest;
import ge.devspace.gismap.repository.FactoryRepository;
import ge.devspace.gismap.repository.RoadRepository;
import ge.devspace.gismap.repository.ForestRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
public class DataInitializationService {

    @Autowired
    private FactoryRepository factoryRepository;

    @Autowired
    private RoadRepository roadRepository;

    @Autowired
    private ForestRepository forestRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Transactional
    public void initializeSampleData() {
        if (factoryRepository.count() == 0) {
            createSampleFactories();
        }
        if (roadRepository.count() == 0) {
            createSampleRoads();
        }
        if (forestRepository.count() == 0) {
            createSampleForests();
        }
    }

    private void createSampleFactories() {
        // Tbilisi area factories
        Factory[] factories = {
            new Factory("Georgian Wine Factory", "Food & Beverage", 500, 1995, "Active",
                geometryFactory.createPoint(new Coordinate(44.8271, 41.7151))),
            
            new Factory("Tbilisi Steel Works", "Heavy Industry", 1200, 1980, "Active",
                geometryFactory.createPoint(new Coordinate(44.8340, 41.7100))),
            
            new Factory("Caucasus Textile Mill", "Textile", 300, 2000, "Active",
                geometryFactory.createPoint(new Coordinate(44.8200, 41.7200))),
            
            new Factory("Georgia Auto Assembly", "Automotive", 800, 2010, "Active",
                geometryFactory.createPoint(new Coordinate(44.8400, 41.7050))),
            
            new Factory("Tbilisi Pharmaceutical", "Pharmaceutical", 250, 2005, "Active",
                geometryFactory.createPoint(new Coordinate(44.8150, 41.7250))),
            
            new Factory("Old Soviet Factory", "Heavy Industry", 1500, 1970, "Closed",
                geometryFactory.createPoint(new Coordinate(44.8500, 41.7000))),
            
            new Factory("Georgian Electronics", "Electronics", 400, 2015, "Active",
                geometryFactory.createPoint(new Coordinate(44.8100, 41.7300))),
            
            new Factory("Mtkvari Food Processing", "Food & Beverage", 600, 1985, "Active",
                geometryFactory.createPoint(new Coordinate(44.8450, 41.7180)))
        };

        factoryRepository.saveAll(Arrays.asList(factories));
    }

    private void createSampleRoads() {
        // Major roads in Tbilisi
        Road[] roads = {
            // Rustaveli Avenue - main boulevard
            new Road("Rustaveli Avenue", "Main Street", 2.5, "Asphalt", 50,
                geometryFactory.createLineString(new Coordinate[]{
                    new Coordinate(44.8050, 41.7200),
                    new Coordinate(44.8100, 41.7180),
                    new Coordinate(44.8150, 41.7160),
                    new Coordinate(44.8200, 41.7140),
                    new Coordinate(44.8250, 41.7120)
                })),
            
            // Agmashenebeli Avenue
            new Road("Agmashenebeli Avenue", "Secondary Road", 3.2, "Asphalt", 60,
                geometryFactory.createLineString(new Coordinate[]{
                    new Coordinate(44.8300, 41.7250),
                    new Coordinate(44.8350, 41.7220),
                    new Coordinate(44.8400, 41.7190),
                    new Coordinate(44.8450, 41.7160)
                })),
            
            // Vazha-Pshavela Avenue
            new Road("Vazha-Pshavela Avenue", "Highway", 4.8, "Asphalt", 80,
                geometryFactory.createLineString(new Coordinate[]{
                    new Coordinate(44.8100, 41.7400),
                    new Coordinate(44.8200, 41.7350),
                    new Coordinate(44.8300, 41.7300),
                    new Coordinate(44.8400, 41.7250),
                    new Coordinate(44.8500, 41.7200)
                })),
            
            // Pekini Avenue
            new Road("Pekini Avenue", "Main Street", 2.8, "Asphalt", 50,
                geometryFactory.createLineString(new Coordinate[]{
                    new Coordinate(44.7900, 41.7100),
                    new Coordinate(44.8000, 41.7120),
                    new Coordinate(44.8100, 41.7140),
                    new Coordinate(44.8200, 41.7160)
                })),
            
            // Chavchavadze Avenue
            new Road("Chavchavadze Avenue", "Secondary Road", 3.5, "Asphalt", 60,
                geometryFactory.createLineString(new Coordinate[]{
                    new Coordinate(44.7950, 41.7250),
                    new Coordinate(44.8050, 41.7280),
                    new Coordinate(44.8150, 41.7310),
                    new Coordinate(44.8250, 41.7340)
                })),
            
            // Mountain road to Mtatsminda
            new Road("Mtatsminda Road", "Mountain Road", 1.8, "Concrete", 30,
                geometryFactory.createLineString(new Coordinate[]{
                    new Coordinate(44.8000, 41.7200),
                    new Coordinate(44.7980, 41.7250),
                    new Coordinate(44.7960, 41.7300),
                    new Coordinate(44.7940, 41.7350)
                }))
        };

        roadRepository.saveAll(Arrays.asList(roads));
    }

    private void createSampleForests() {
        // Forest areas around Tbilisi
        Forest[] forests = {
            // Mtatsminda Park forest
            new Forest("Mtatsminda Park Forest", "Urban Forest", 45.5, "Protected",
                geometryFactory.createPolygon(new Coordinate[]{
                    new Coordinate(44.7900, 41.7350),
                    new Coordinate(44.7950, 41.7350),
                    new Coordinate(44.7950, 41.7400),
                    new Coordinate(44.7900, 41.7400),
                    new Coordinate(44.7900, 41.7350)
                })),
            
            // Vake Park forest
            new Forest("Vake Park", "Urban Forest", 28.3, "Municipal",
                geometryFactory.createPolygon(new Coordinate[]{
                    new Coordinate(44.7800, 41.7200),
                    new Coordinate(44.7850, 41.7200),
                    new Coordinate(44.7850, 41.7250),
                    new Coordinate(44.7800, 41.7250),
                    new Coordinate(44.7800, 41.7200)
                })),
            
            // Turtle Lake forest
            new Forest("Turtle Lake Forest", "Natural Forest", 120.8, "Protected",
                geometryFactory.createPolygon(new Coordinate[]{
                    new Coordinate(44.7600, 41.7400),
                    new Coordinate(44.7700, 41.7400),
                    new Coordinate(44.7700, 41.7500),
                    new Coordinate(44.7600, 41.7500),
                    new Coordinate(44.7600, 41.7400)
                })),
            
            // Lisi Lake forest
            new Forest("Lisi Lake Forest", "Natural Forest", 95.2, "Protected",
                geometryFactory.createPolygon(new Coordinate[]{
                    new Coordinate(44.7400, 41.7600),
                    new Coordinate(44.7500, 41.7600),
                    new Coordinate(44.7500, 41.7700),
                    new Coordinate(44.7400, 41.7700),
                    new Coordinate(44.7400, 41.7600)
                }))
        };

        forestRepository.saveAll(Arrays.asList(forests));
    }
}