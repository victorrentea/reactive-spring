package victor.training.reactive.reactor.lite;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import victor.training.reactive.intro.ThreadUtils;

import java.time.Duration;

@Slf4j
public class HotVsCold {

   public static void main(String[] args) {
      hot();
   }
   public static void hot() {
      // cold publisher
      ConnectableFlux<Long> interval = Flux.interval(Duration.ofMillis(100))
          .publish()
          ;
//      interval.subscribe(System.out::println);
//      interval.subscribe(System.out::println);
//      interval.subscribe(System.out::println);

      // here, register all subscribers that should receive the signals
      interval.connect(); // fires up the hot publisher ------------------

//      interval.subscribe(System.out::println);
      play(interval);
   }
   public static void cold() {
      // cold publisher
      Flux<Long> interval = Flux.interval(Duration.ofMillis(100));
      play(interval);
   }

   private static void play(Flux<Long> interval) {
//      interval = interval.doOnNext(e -> log.debug("element"));
      ThreadUtils.sleep(1000);
      System.out.println("------");
      interval.subscribe(n -> System.out.println("Subscr1 I see " + n));

      ThreadUtils.sleep(1000);
      System.out.println("Then, someone else: ..");
      interval.subscribe(n -> System.out.println("Subscr2 I see " + n));
      ThreadUtils.sleep(1000);
   }
}
