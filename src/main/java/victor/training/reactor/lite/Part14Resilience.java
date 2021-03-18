package victor.training.reactor.lite;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.time.Duration.ofMillis;

public class Part14Resilience {

   //========================================================================================

   // TODO Allow max 500 ms for the publisher to emit a string, otherwise complete with "DUMMY"
   public Mono<String> timeout(Mono<String> possiblySlow) {
      return possiblySlow
          .timeout(ofMillis(500), Mono.just("DUMMY"));
   }

   //========================================================================================

   // TODO issue a maximum of 2 retries when the mono emits an error. If still in error, return an empty mono.
   public Mono<String> retry(Mono<String> possiblyCrashing) {
      return possiblyCrashing.retry(2);
   }


}
