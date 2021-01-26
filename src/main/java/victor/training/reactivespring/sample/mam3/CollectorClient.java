package victor.training.reactivespring.sample.mam3;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public class CollectorClient {
   public <R> Mono<List<CollectorResponse>> retrieveCollectorItems(List<UUID> uuids) {
      return null;
   }
}
