package org.coffeeshop.stations.repositories;

import org.coffeeshop.stations.models.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository interface for managing Station entities in the database. */
@Repository
public interface StationRepository extends JpaRepository<Station, Long> {}
