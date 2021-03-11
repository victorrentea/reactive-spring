package victor.training.reactor.pitfalls;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.stream.IntStream;

@Slf4j
public class UnboundedFlatMap {
   
   @Test
   public void test() {
      getDataFlux()
          .flatMap(this::operation, 4)
          .blockLast();
   }

   private Flux<Integer> getDataFlux() {
      return Flux.range(1, 300)
          .publishOn(Schedulers.boundedElastic());
   }

   private <R> Mono<Void> operation(Integer integer) {
      log.debug("Run");
      return WebClient.create().get().uri("https://google.com").retrieve().toBodilessEntity().then();
//      return Mono.empty();
   }
}
