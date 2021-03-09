package victor.training.reactor.lite;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

public class Part12VarieTest {

   private Part12Varie workshop = new Part12Varie();

   @Test
   public void defer() {
      Flux<Integer> flux = workshop.defer();
      List<Integer> sequence1 = flux.collectList().block();
      List<Integer> sequence2 = flux.collectList().block();
      Assertions.assertThat(sequence1).isNotEqualTo(sequence2);
   }
}
