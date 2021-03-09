package victor.training.reactor.lite;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import victor.training.reactivespring.start.ThreadUtils;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class Part12AdvancedTest {

   private Part12Advanced workshop = new Part12Advanced();

   @Test
   public void defer() {
      Flux<Integer> flux = workshop.defer();
      List<Integer> sequence1 = flux.collectList().block();
      List<Integer> sequence2 = flux.collectList().block();
      assertThat(sequence1).isNotEqualTo(sequence2);
   }


   @Test
   public void hotPublisher() throws InterruptedException {
      assertThat(workshop.hotPublisher().blockFirst()).isLessThan(2);

      Flux<Long> hot = workshop.hotPublisher();
      Thread.sleep(400);
      assertThat(hot.blockFirst()).isGreaterThan(3);
   }
   
   @Test
   public void replay() {
      String reactiveManifesto = "Large systems are composed of smaller ones and therefore " +
                                 "depend on the Reactive properties of their constituents. [...] ";
      Flux<String> timedFlux = workshop.replay(Flux.interval(Duration.ofMillis(100)), reactiveManifesto);

      assertThat(timedFlux.take(3).collectList().block()).containsExactly("Large", "systems", "are");

      ThreadUtils.sleep(600);
      assertThat(timedFlux.take(3).collectList().block()).containsExactly("Large", "systems", "are");
   }
   
   @Test
   public void reactorContext() {
      Mono<String> withContext = workshop.reactorContext()
          .contextWrite(context -> context.put("username", "John"));

      Duration duration = StepVerifier.create(withContext)
          .expectNext("Hello John")
          .verifyComplete();
      assertThat(duration).isGreaterThan(Duration.ofMillis(900));
   }
}
