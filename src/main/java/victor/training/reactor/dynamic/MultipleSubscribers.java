package victor.training.reactor.dynamic;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactivespring.start.ThreadUtils;

import static java.time.Duration.ofSeconds;

@Slf4j
public class MultipleSubscribers {
   public static void main(String[] args) {

      // COLD PUBLISHER
      Mono<String> restMono = restCall().cache();

      restMono.subscribe(data -> log.info("Got response: " + data));
      restMono.subscribe(data -> log.info("Got response: " + data));


//      Flux.just("a","b").cache()
      Mono<String> monoA = Mono.just("a")
          .log()
          ;

      monoA.subscribe();
      monoA.subscribe();

      ThreadUtils.sleep(5000);

   }

   public static Mono<String> restCall() {
      return Mono.fromCallable(() -> {
         log.info("Sending REST call...");
         ThreadUtils.sleep(1000);
         return "Data";
      }).subscribeOn(Schedulers.boundedElastic());
   }
}
