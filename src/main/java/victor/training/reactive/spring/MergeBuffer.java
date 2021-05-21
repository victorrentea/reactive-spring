package victor.training.reactive.spring;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.intro.ThreadUtils;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class MergeBuffer {
   public static void main(String[] args) {

   AtomicInteger atomicInteger = new AtomicInteger();

      Sinks.Many<String> sink = Sinks.many().multicast().directBestEffort();
      Flux<String> flux = sink.asFlux();

      flux.bufferTimeout(4, Duration.ofSeconds(2))
          .publishOn(Schedulers.boundedElastic())
          .subscribe(b -> log.info("Sending " + b));

      for (int i=0;i<3;i++) {
         new Thread(() -> {
            for (int j = 0; j < 1; j++) {
               ThreadUtils.sleep(new Random().nextInt(4) * 1000 + 1000);
               int e = atomicInteger.incrementAndGet();
               log.info("Pushing elem " + e);
               sink.tryEmitNext("Elem " + e);
            }
         }).start();
      }

      ThreadUtils.sleep(10000);
   }


}
