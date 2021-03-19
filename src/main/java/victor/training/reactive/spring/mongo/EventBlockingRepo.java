package victor.training.reactive.spring.mongo;

import org.springframework.data.repository.CrudRepository;

public interface EventBlockingRepo extends CrudRepository<Event, Long> {
}
