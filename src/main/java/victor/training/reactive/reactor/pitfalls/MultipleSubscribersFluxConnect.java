package victor.training.reactive.reactor.pitfalls;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Objects;

import static victor.training.reactive.intro.Utils.waitForEnter;

@Slf4j
public class MultipleSubscribersFluxConnect {

   public static void main(String[] args) {

      Flux<String> hugeFlux = longRunningQuery(); // cold publisher

      // TODO send items in buffers of 10 items to writePage() in || with announce(), all on boundedElastic
      hugeFlux
          .buffer(10)
          .subscribe(MultipleSubscribersFluxConnect::writePage);

      hugeFlux
          .subscribe(MultipleSubscribersFluxConnect::announce);

      waitForEnter();
   }

   public static void writePage(List<String> page) {
      log.info("Writing {} items: {}", page.size(), page);
   }

   public static void announce(String item) {
      log.info("Announcing item {}", item);
   }

   private static Flux<String> longRunningQuery() {
      return Flux.defer(() -> {
         log.info("Open the long query");
         return Flux.range(1, 100).map(Objects::toString);
      })
          .subscribeOn(Schedulers.boundedElastic());
   }

}
