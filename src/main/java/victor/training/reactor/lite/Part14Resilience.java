package victor.training.reactor.lite;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Function;
import java.util.function.Supplier;

public class Part14Resilience {

   //========================================================================================

   // TODO Allow max 500 ms for the publisher to emit a string, otherwise complete with "DUMMY"
   public Mono<String> timeout(Mono<String> possiblySlow) {
      return null;
   }


}
