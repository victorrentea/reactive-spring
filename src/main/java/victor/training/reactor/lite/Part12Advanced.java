package victor.training.reactor.lite;

import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Part12Advanced {

   // TODO Create a Flux returning 3 different random numbers to anyone subscribing to it.
   // Note: multiple subscribers will subscribe!
   // Use generateRandomInts()!
   public Flux<Integer> defer() {
      return null;
   }

   private static List<Integer> generateRandomInts() {
      Random r = new Random(); // depends on current time
      return r.ints(3).boxed().collect(Collectors.toList());
   }
   //========================================================================================

   // TODO emits an number sequence (0,1,2,3...) every 100ms *even without a registered subscriber*
   // (the emissions should start immediately when hotPublisher() is called)
   // Hint: Flux.publish(): ConnectableFlux
   // Brain: connect(): Disposable > how to handle?
   public Flux<Long> hotPublisher() {
      return null;
   }

   //========================================================================================

   // TODO emit words from the text at every onNext signal in timeFlux. Late subscribers should receive words from the start.
   public Flux<String> replay(Flux<Long> timeFlux, String text) {
      return null;
   }

   //========================================================================================

   // TODO The returned Mono emits after 1 second "Hello " + the current username got from Reactor Context
   // Hint: Mono.deferContextual allows access to context propagated from downstream
   public Mono<String> reactorContext() {
      return null;
   }
}
