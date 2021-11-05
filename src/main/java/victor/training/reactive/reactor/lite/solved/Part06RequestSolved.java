package victor.training.reactive.reactor.lite.solved;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import victor.training.reactive.reactor.lite.Part06Request;
import victor.training.reactive.reactor.lite.domain.User;

public class Part06RequestSolved extends Part06Request {

   @Override
   public StepVerifier requestAllExpectFourThenComplete(Flux<User> flux) {
      return StepVerifier.create(flux)
          .expectNextCount(3)
          .expectComplete();
   }

   public Flux<Integer> throttleUpstreamRequest(Flux<Integer> upstream) {
      return upstream.limitRate(10);
   }
}
