package victor.training.reactor.pitfalls;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LeakingFlux {


   private TestPublisher<String> coldPublisher = TestPublisher.createCold();
   private Flux<String> flux = coldPublisher.flux();

   @Test
   public void iterableDoesntCancelPublisher() {
      coldPublisher.next("a","b");

      Iterable<String> iterable = flux.toIterable();

      assertEquals("a", iterable.iterator().next());;

      coldPublisher.assertNotCancelled();  //oups
   }
}
