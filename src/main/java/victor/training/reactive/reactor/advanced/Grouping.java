package victor.training.reactive.reactor.advanced;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.intro.ThreadUtils;

import java.util.List;

@Slf4j
public class Grouping {
   public static void main(String[] args) {
      // TODO send odd numbers to sendOdd, while even numbers to sendEven, respectively; all in pages of 10 items

      Flux.range(1, 100)
          .buffer(10)
          .flatMap(Grouping::sendOdd)
          .subscribe();

      ThreadUtils.sleep(2000);
   }

   public static NumberType getType(int number) {
      return number % 2 == 1 ? NumberType.ODD : NumberType.EVEN;
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

enum NumberType {
   ODD, EVEN
}