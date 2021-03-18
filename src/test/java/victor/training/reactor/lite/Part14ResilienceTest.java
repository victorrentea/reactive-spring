package victor.training.reactor.lite;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import static java.time.Duration.ofMillis;
import static org.assertj.core.api.Assertions.assertThat;

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
      assertThat(workshop.retry(Mono.just("ok")).block()).isEqualTo("ok");

      TestPublisher<String> fail1 = TestPublisher.<String>createCold()
          .error(new RuntimeException())
          .next("ok");
      assertThat(workshop.retry(fail1.mono()).block()).isEqualTo("ok");
      assertThat(fail1.subscribeCount()).isEqualTo(1);

      TestPublisher<String> fail2 = TestPublisher.<String>createCold()
          .error(new RuntimeException())
          .error(new RuntimeException())
          .next("ok");
      assertThat(workshop.retry(fail2.mono()).block()).isEqualTo("ok");
      assertThat(fail2.subscribeCount()).isEqualTo(3);

      TestPublisher<String> fail3 = TestPublisher.<String>createCold()
          .error(new RuntimeException())
          .error(new RuntimeException())
          .error(new RuntimeException());
      assertThat(workshop.retry(fail3.mono()).block()).isNull();
      assertThat(fail3.subscribeCount()).isEqualTo(3);
   }
}
