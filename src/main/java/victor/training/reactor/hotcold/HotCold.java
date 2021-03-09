package victor.training.reactor.hotcold;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
public class HotCold {
   public static void main(String[] args) {


      Flux<Integer> flux = createFlux();// Flux.defer(() -> createFlux());
      flux
          .log()
            .subscribeOn(Schedulers.boundedElastic())
          .blockLast()
      ;
      flux
          .log()
            .subscribeOn(Schedulers.boundedElastic())
          .blockLast()
      ;
   }

   private static Flux<Integer> createFlux() {
      return Flux.fromIterable(generateRandomInts());
   }

   private static List<Integer> generateRandomInts() {
      Random r = new Random(); // depends on current time
      return r.ints(10).boxed().collect(Collectors.toList());
   }
}
