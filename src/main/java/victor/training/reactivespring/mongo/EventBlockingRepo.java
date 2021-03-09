package victor.training.reactivespring.mongo;

import org.springframework.data.repository.CrudRepository;

public interface EventBlockingRepo extends CrudRepository<Event, Long> {
}
