package victor.training.reactor.complex;

import reactor.core.publisher.Mono;

import java.time.Duration;

import static java.time.Duration.ofMillis;

public class ExternalCacheClient {
   public Mono<ProductRating> lookupInCache(Long productId) {
      return Mono.defer(() -> {
         if (Math.random() < .5) {
            return Mono.just(new ProductRating(5));
         } else {
            return Mono.empty();
         }
      }).delayElement(ofMillis(10));
   }

   public Mono<Void> putInCache(Long productId, ProductRating rating) {
      return Mono.empty().delayElement(ofMillis(10)).then();
   }
}
