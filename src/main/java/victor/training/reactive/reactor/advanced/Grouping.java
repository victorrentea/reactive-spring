package victor.training.reactive.reactor.advanced;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.intro.ThreadUtils;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

enum NumberType {
   ODD(n -> n%2==1, Grouping::sendOdd),
   EVEN(n->n%2 == 0, Grouping::sendEven);

   private final Predicate<Integer> selector;
   public final Function<List<Integer>, Mono<Void>> dataSenderFunction;

   NumberType(Predicate<Integer> selector, Function<List<Integer>, Mono<Void>> dataSenderFunction) {
      this.selector = selector;
      this.dataSenderFunction = dataSenderFunction;
   }

   public static NumberType getFor(Integer number) {
      for (NumberType value : values()) {
         if (value.selector.test(number)) {
            return value;
         }
      }
      throw new IllegalArgumentException();
   }

}


@Slf4j
public class Grouping {
   public static void main(String[] args) {
      // TODO send odd numbers to sendOdd, while even numbers to sendEven, respectively; all in pages of 10 items


      Flux.range(1, 100)

          .groupBy(n -> NumberType.getFor(n))
          .flatMap(groupedFlux -> groupedFlux.buffer(10)
              .flatMap(list -> groupedFlux.key().dataSenderFunction.apply(list)))
          .subscribe();

      ThreadUtils.sleep(2000);
   }

   public static Mono<Void> sendEven(List<Integer> page) {
      return Mono.<Void>fromRunnable(() -> log.info("Sending even numbers: {}", page))
          .subscribeOn(Schedulers.boundedElastic())
          // every subscribe signal is performed on a separate thread acquired from the target scheduler,
          // effectively enabling parallelism of the sending
          ;
   }

   public static Mono<Void> sendOdd(List<Integer> page) {
      return Mono.<Void>fromRunnable(() -> log.info("Sending odd numbers: {}", page))
          .subscribeOn(Schedulers.boundedElastic())
          ;
   }
}
