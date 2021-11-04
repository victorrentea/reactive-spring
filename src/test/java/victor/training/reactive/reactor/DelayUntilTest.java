package victor.training.reactive.reactor;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static java.lang.System.currentTimeMillis;

@Slf4j
public class DelayUntilTest {


   @Test
   void delayUntil_serializesSubscribeSignals() {
      long t0 = currentTimeMillis();

      List<Long> list = Flux.interval(Duration.ofMillis(100))
          .take(2)
          .delayUntil(t -> expensiveMustHaveFunction())
//          .doOnNext(t -> expensiveMustHaveFunction().subscribe()) // rateaza erori
          .collectList()
          .block();
      long t1 = currentTimeMillis();

      log.debug("List: " + list);
      log.debug("Took {}", t1-t0);
      Assertions.assertThat(t1 - t0).isGreaterThan(2000);
   }
   @Test
   void flatMapFiresRequestsASAP() {
      long t0 = currentTimeMillis();

      List<Long> list = Flux.interval(Duration.ofMillis(100))
          .take(2)
          .flatMap(t -> expensiveMustHaveFunction().thenReturn(t))
          .collectList()
          .block();
      long t1 = currentTimeMillis();

      log.debug("List: " + list);
      log.debug("Took {}", t1-t0);
      Assertions.assertThat(t1 - t0).isLessThan(1500);
   }

   private Mono<Long> expensiveMustHaveFunction() {
      return Mono.delay(Duration.ofMillis(1000))
          .doOnSubscribe(s -> log.info("Now fires delay"))
          .doOnSuccess(d -> log.info("Completed delay"));
   }
}
