package victor.training.reactive.reactor.lite;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VerifySubscription {
   @Test
   public void test() {
      TestPublisher<Void> testPublisher = TestPublisher.<Void>createCold()          ;
      Mono<Void> mono = testPublisher.mono();

      Mono.just("a")
          .flatMap(e -> mono)
          .subscribe();

      testPublisher.assertWasSubscribed();
   }

}
