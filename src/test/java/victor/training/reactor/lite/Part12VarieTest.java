package victor.training.reactor.lite;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class Part12VarieTest {

   private Part12Varie workshop = new Part12Varie();

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
}
