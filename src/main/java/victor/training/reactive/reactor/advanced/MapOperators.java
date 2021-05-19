package victor.training.reactive.reactor.advanced;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.intro.ThreadUtils;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.Duration.ofMillis;

@Slf4j
public class MapOperators {

   public static final Scheduler FR_GOV_SCHED = Schedulers.newSingle("fr.gov");

   public static void main(String[] args) {

      // TODO explore flatMap vs flatMapSequential vs concatMap
      Flux.range(1, 1000)
         .flatMap(MapOperators::openChildrenFlux, 6)
//         .flatMapSequential(MapOperators::openChildrenFlux)
//          .concatMap(n -> openChildrenFlux(n))
          .subscribe(log::info);

      ThreadUtils.sleep(3000);
   }

   public static final Random r = new Random();

   private static Flux<String> openChildrenFlux(Integer n) {
      AtomicInteger atomicInteger = new AtomicInteger();
      return Flux.interval(ofMillis(10 + r.nextInt(100)))
         .doOnNext(d -> log.info("Firing a HTTP request at my friend's microservice"))
//          .log()
          .map(tick -> "Parent " + n + " - Item " + atomicInteger.incrementAndGet())
          .take(1)
//          .subscribeOn(FR_GOV_SCHED)
          ;
   }

   private static Mono<String> oneCall(Integer n) {
      int delay = 10 + r.nextInt(100);
      return Mono.just("Data for " + n)
          .doOnNext(e -> log.info("Request is sent for " + n))
          .delayElement(Duration.ofMillis(delay));
   }
}
