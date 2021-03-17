package victor.training.reactivespring.sample.mam1;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AnalyzerItemRepository { //extends ReactiveCassandraRepository<AnalyserItem, UUID> {
   public Mono<AnalyserItem> findById(UUID masterKey);
//
   public Mono<Object> save(AnalyserItem persistentItem);
}
