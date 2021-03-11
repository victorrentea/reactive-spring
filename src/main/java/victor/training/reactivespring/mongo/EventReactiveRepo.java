package victor.training.reactivespring.mongo;

import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface EventReactiveRepo extends ReactiveCrudRepository<Event, String> {
   @Tailable
//   Flux<Event> findAllByValueNotNull();
   Flux<Event> findAllByValueNotNull();
}
