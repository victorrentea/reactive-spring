package victor.training.reactive.reactor.advanced;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.intro.ThreadUtils;

import static victor.training.reactive.intro.ThreadUtils.sleep;

@Slf4j
public class BackpressureBufferAndDie {
   public static void main(String[] args) {

      Many<Long> emitter = Sinks.many().unicast().onBackpressureError();
      
      new Thread(() -> {
         for (int i = 0; i < 1000; i++) {
            sleep(10);
            log.info("Emit " + i);
            log.debug(":"+emitter.tryEmitNext((long) i));
         }
      }).start();

      emitter.asFlux().publishOn(Schedulers.boundedElastic())
          .log()
          .limitRate(10)
          .subscribe(e-> {
         sleep(50);
         log.info("Received " + e);
      });

      ThreadUtils.sleep(10000);
   }
}
