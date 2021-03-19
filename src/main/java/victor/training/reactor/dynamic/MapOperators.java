package victor.training.reactor.dynamic;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactivespring.start.ThreadUtils;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.Duration.ofMillis;

@Slf4j
public class MapOperators {

   public static void main(String[] args) {
      Flux.range(1,10)
//          .concatMap(n -> openChildrenFlux(n))
          .concatMap(n -> requestOneCall(n))
         .subscribe(log::info);

      ThreadUtils.sleep(3000);
   }

   public static final Random r = new Random();
   private static Flux<String> openChildrenFlux(Integer n) {
      AtomicInteger atomicInteger = new AtomicInteger();
      return Flux.interval(ofMillis( 10 + r.nextInt(100)))
          .map(tick -> "Parent " + n + " - Item " + atomicInteger.incrementAndGet()).take(10);
   }

   private static Mono<String> requestOneCall(Integer n) {
      int delay = 10 + r.nextInt(100);
      return Mono.just("Data for " + n)
          .doOnNext(e -> log.info("Request is sent for " + n))
          .delayElement(Duration.ofMillis(delay));
   }
}
