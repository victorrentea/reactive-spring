package victor.training.reactive.reactor.complex;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import static reactor.function.TupleUtils.function;

@Slf4j
public class Refire {
   public static void main(String[] args) {

      Mono<String> c=  getCEntry();
      System.out.println(c.block());

   }

   private static Mono<String> getCEntry() {
      // -- cache
//      Mono<String> aMono = getA().cache();
//
//      Mono<String> monoB = aMono.flatMap(a -> getB(a));
//
//      Mono<String> monoC = aMono.zipWith(monoB)
//          .flatMap(function((a, b) -> getC(a, b)));

      // --- preferred:
      return getA()
          .zipWhen(a -> getB(a))
          .flatMap(function((a, b) -> getC(a, b)))
          ;
   }

   public static Mono<String> getA() {
      return Mono.defer(() -> {
         log.info("getting A 1sec");
         return Mono.just("A");
      }).subscribeOn(Schedulers.boundedElastic());
   }
   public static Mono<String> getB(String a) {
      return Mono.defer(() -> {
         log.info("getting B 1sec");
         return Mono.just(a + "B");
      });
   }
   public static Mono<String> getC(String a, String b) {
      return Mono.defer(() -> {
         log.info("getting C 1sec");
         return Mono.just(a+b+"C");
      });
   }
}
