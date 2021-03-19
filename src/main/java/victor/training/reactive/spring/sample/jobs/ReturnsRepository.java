package victor.training.reactive.spring.sample.jobs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReturnsRepository {
   public Flux<ReturnDetails> findByKeyLocationIdAndKeyId(LocationId locationId, Long id) {
      return null;
   }

   public Mono<Object> save(ReturnDetails returnDetails) {
      return null;
   }
}
