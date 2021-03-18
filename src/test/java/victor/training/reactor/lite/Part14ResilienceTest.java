package victor.training.reactor.lite;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
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
   }
}
