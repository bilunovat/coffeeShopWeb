package org.coffeeshop.stations.scripts;

import org.coffeeshop.stations.models.Station;
import org.coffeeshop.stations.repositories.StationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Component that loads initial station data into the database when the application starts. This is
 * useful for testing and development purposes to ensure there are stations available for the
 * application to work with.
 */
@Component
public class StationDataLoader implements CommandLineRunner {
    private final StationRepository stationRepository;

    /**
     * Creates a new StationDataLoader instance.
     *
     * @param stationRepository the repository for station database operations
     */
    public StationDataLoader(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    /**
     * Loads initial station data if the database is empty.
     *
     * @param args application arguments
     * @throws Exception if data loading fails
     */
    @Override
    public void run(String... args) throws Exception {
        if (stationRepository.count() > 0) {
            return;
        }
        stationRepository.save(new Station("Cramlington", "08:00-18:00", "09:00-17:00", true));
    }
}
