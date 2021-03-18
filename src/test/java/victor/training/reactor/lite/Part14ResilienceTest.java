package victor.training.reactor.lite;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.util.concurrent.atomic.AtomicInteger;

import static java.time.Duration.ofMillis;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class Part14ResilienceTest {
   private Part14Resilience workshop = new Part14Resilience();

   @Test
   public void timeout() {
      Mono<String> immediate = Mono.just("immediate");
      assertThat(workshop.timeout(immediate).block()).isEqualTo("immediate");

      Mono<String> fast = Mono.just("fast").delayElement(ofMillis(10));
      assertThat(workshop.timeout(fast).block()).isEqualTo("fast");

      Mono<String> slow = Mono.just("slow").delayElement(ofMillis(1000));
      assertThat(workshop.timeout(slow).block()).isEqualTo("DUMMY");

      Mono<String> upstreamError = Mono.error(new RuntimeException());
      Mono<String> result = workshop.timeout(upstreamError);
      StepVerifier.create(result).verifyError();
   }

   @Test
   public void retry() {
//      assertThat(workshop.retry(Mono.just("ok")).block()).isEqualTo("ok");
//
//      TestPublisher<String> fail1 = TestPublisher.<String>createCold()
//          .error(new RuntimeException())
//          .next("ok");
//      assertThat(workshop.retry(fail1.mono()).block()).isEqualTo("ok");
//      assertThat(fail1.subscribeCount()).isEqualTo(1);

      AtomicInteger failuresLeft = new AtomicInteger(2);
      Mono<String> flakyMono = Mono.fromCallable(() -> {
         if (failuresLeft.decrementAndGet() >= 0) {
            throw new IllegalStateException();
         } else {
            return "ok";
         }
      }).doOnSubscribe(s -> log.info("Subscribing..."));


      log.info("0 failures ----");
      failuresLeft.set(0);
      assertThat(workshop.retry(flakyMono).block()).isEqualTo("ok");
      assertThat(failuresLeft.get()).isLessThanOrEqualTo(0);


      log.info("1 failures ----");
      failuresLeft.set(1);
      assertThat(workshop.retry(flakyMono).block()).isEqualTo("ok");
      assertThat(failuresLeft.get()).isLessThanOrEqualTo(0);

      log.info("2 failures ----");
      failuresLeft.set(2);
      assertThat(workshop.retry(flakyMono).block()).isEqualTo("ok");
      assertThat(failuresLeft.get()).isLessThanOrEqualTo(0);

      log.info("3 failures ----");
      failuresLeft.set(3);
      assertThatThrownBy(() -> workshop.retry(flakyMono).block())
         .isInstanceOf(IllegalStateException.class);
      assertThat(failuresLeft.get()).isEqualTo(0);
   }
}
