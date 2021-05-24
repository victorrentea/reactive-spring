package victor.training.reactive.reactor.advanced;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.intro.ThreadUtils;

import java.util.List;

import static reactor.core.scheduler.Schedulers.boundedElastic;

@Slf4j
public class Grouping {
   public static void main(String[] args) {
      // TODO send odd numbers to sendOdd(), while even numbers to sendEven(), respectively
      // TODO send even numbers in pages of 10 items
      // TODO for odd numbers call preSendOdd() before sendOdd()
      // TODO item 0 should not trigger any sending

      Flux.range(0, 100)
          .flatMap(Grouping::sendOdd)
          .subscribe();

      ThreadUtils.sleep(2000);
   }

   public static NumberType getType(int number) {
      return number % 2 == 1 ? NumberType.ODD : NumberType.EVEN;
   }

   public static Mono<Void> sendEven(Integer item) {
      return Mono.<Void>fromRunnable(() -> log.info("Sending even numbers: {}", item))
          .subscribeOn(boundedElastic())
          ;
   }

   public static Mono<Void> sendOdd(Integer item) {
      return Mono.<Void>fromRunnable(() -> log.info("Sending odd numbers: {}", item))
          .subscribeOn(boundedElastic())
          ;
   }

   public static Mono<Void> preSendOdd(Integer item) {
      return Mono.fromRunnable(() -> System.out.println("Pre send odd " + item));
   }
}

enum NumberType {
   ODD, EVEN
}