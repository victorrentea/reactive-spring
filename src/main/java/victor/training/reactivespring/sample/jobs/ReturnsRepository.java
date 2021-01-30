package victor.training.reactivespring.sample.jobs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Iterator;

public class ReturnsRepository {
   public Flux<ReturnDetails> findByKeyLocationIdAndKeyId(LocationId locationId, Long id) {
      return null;
   }

   public Mono<Object> save(ReturnDetails returnDetails) {
      return null;
   }
}
