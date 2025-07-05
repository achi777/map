package ge.devspace.simplemap.service;

import ge.devspace.simplemap.entity.SimpleFactory;
import ge.devspace.simplemap.entity.SimpleRoad;
import ge.devspace.simplemap.entity.SimpleForest;
import ge.devspace.simplemap.repository.SimpleFactoryRepository;
import ge.devspace.simplemap.repository.SimpleRoadRepository;
import ge.devspace.simplemap.repository.SimpleForestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class SimpleDataService implements CommandLineRunner {

    @Autowired
    private SimpleRoadRepository roadRepository;
    
    @Autowired
    private SimpleFactoryRepository factoryRepository;
    
    @Autowired
    private SimpleForestRepository forestRepository;

    @Override
    public void run(String... args) throws Exception {
        // Clear existing data
        roadRepository.deleteAll();
        factoryRepository.deleteAll();
        forestRepository.deleteAll();

        // Create sample roads around Tbilisi
        SimpleRoad road1 = new SimpleRoad("რუსთაველის გამზირი", "მთავარი გზა", "ასფალტი", 
            41.7100, 44.7950, 41.7180, 44.8150, 2.3);
        roadRepository.save(road1);

        SimpleRoad road2 = new SimpleRoad("ჩავჩავაძის გამზირი", "მთავარი გზა", "ასფალტი", 
            41.7050, 44.7800, 41.7200, 44.7950, 1.8);
        roadRepository.save(road2);

        SimpleRoad road3 = new SimpleRoad("აღმაშენებლის გამზირი", "მთავარი გზა", "ასფალტი", 
            41.7050, 44.8200, 41.7200, 44.8350, 2.1);
        roadRepository.save(road3);

        SimpleRoad road4 = new SimpleRoad("პეკინის გამზირი", "მთავარი გზა", "ასფალტი", 
            41.7000, 44.7900, 41.7150, 44.8050, 1.7);
        roadRepository.save(road4);

        SimpleRoad road5 = new SimpleRoad("დამაკავშირებელი გზა", "მეორადი გზა", "ბეტონი", 
            41.7000, 44.8000, 41.7200, 44.8200, 1.5);
        roadRepository.save(road5);

        // Create sample factories
        SimpleFactory factory1 = new SimpleFactory("ტექსტილის ქარხანა", "ტექსტილი", "აქტიური", 500, 41.7120, 44.8080);
        factoryRepository.save(factory1);

        SimpleFactory factory2 = new SimpleFactory("საკვების ქარხანა", "საკვები", "აქტიური", 300, 41.7180, 44.8180);
        factoryRepository.save(factory2);

        SimpleFactory factory3 = new SimpleFactory("ქიმიური ქარხანა", "ქიმია", "შეჩერებული", 200, 41.7080, 44.8280);
        factoryRepository.save(factory3);

        SimpleFactory factory4 = new SimpleFactory("ელექტრონიკის ქარხანა", "ელექტრონიკა", "აქტიური", 150, 41.7200, 44.8000);
        factoryRepository.save(factory4);

        SimpleFactory factory5 = new SimpleFactory("ლითონის ქარხანა", "ლითონი", "აქტიური", 400, 41.7250, 44.8250);
        factoryRepository.save(factory5);

        // Create sample forests
        SimpleForest forest1 = new SimpleForest("მთაწმინდის პარკი", "რეკრეაციული", 120.5, "მაღალი", "დაცული", 41.6953, 44.8017, null);
        forestRepository.save(forest1);

        SimpleForest forest2 = new SimpleForest("ვაკის პარკი", "საქალაქო პარკი", 35.2, "საშუალო", "დაცული", 41.7241, 44.7727, null);
        forestRepository.save(forest2);

        SimpleForest forest3 = new SimpleForest("კუს ტბის ტყე", "ბუნებრივი ტყე", 89.7, "მაღალი", "დაცული", 41.7389, 44.7583, null);
        forestRepository.save(forest3);

        SimpleForest forest4 = new SimpleForest("ლისის ტბის ტყე", "ბუნებრივი ტყე", 156.8, "მაღალი", "დაცული", 41.7833, 44.7167, null);
        forestRepository.save(forest4);

        SimpleForest forest5 = new SimpleForest("თბილისის ზღვის ტყე", "რეკრეაციული", 203.4, "საშუალო", "დაცული", 41.8167, 44.8833, null);
        forestRepository.save(forest5);
    }
}