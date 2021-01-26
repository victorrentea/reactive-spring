package victor.training.reactivespring.sample.mam1;

import reactor.core.publisher.Mono;

import java.util.UUID;

public class KafkaItemRepository {
   public Mono<AnalyserItem> findById(UUID masterKey) {
      return null;
   }

   public Mono<Object> save(AnalyserItem persistentItem) {
      return null;
   }
}
