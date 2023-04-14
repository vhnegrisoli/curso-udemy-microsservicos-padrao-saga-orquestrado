package br.com.microservices.orchestrated.orchestratorservice.core.repository;

import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventRepository extends MongoRepository<Event, String> {
}
