package victor.training.reactor.lite;

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

      TestPublisher<Void> testPublisher = TestPublisher.<Void>createCold()
//          .complete()
          ;
      Mono<Void> mono = testPublisher.mono();

      Mono.just("a")
          .flatMap(e -> mono)
          .subscribe();
//      mono.subscribe();

      testPublisher.assertWasSubscribed();
   }


   @Test
   void should_invalidate_session() {
      WebSession session = mock(WebSession.class);
      MockServerHttpRequest mockServerHttpRequest = MockServerHttpRequest.get("/resource").build();

      MockServerWebExchange mockServerWebExchange = MockServerWebExchange.builder(mockServerHttpRequest)
          .session(session).build();

      TestPublisher<Void> testPublisher = TestPublisher.<Void>createCold().complete();
      when(session.invalidate()).thenReturn(testPublisher.mono());

      invalidateSession(mockServerWebExchange).block();

      testPublisher.assertWasSubscribed();
   }


   public Mono<Void> invalidateSession(ServerWebExchange serverWebExchange) {
      URI redirectUri = UriComponentsBuilder.fromPath("/").build(true).toUri();

      return serverWebExchange.getSession()
          .log()
          .flatMap(webSession -> webSession.invalidate()
              .doOnSubscribe(c -> System.out.println("SUBSC")))
          .then(sendRedirect(serverWebExchange, redirectUri))
          .then()
          ;
   }

   private Mono<Void> sendRedirect(ServerWebExchange serverWebExchange, URI redirectUri) {
      return Mono.empty();
   }
}
