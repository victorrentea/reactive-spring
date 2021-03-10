package victor.training.reactor.complex;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static java.time.Duration.ofMillis;

@Slf4j
public class ExternalCacheClient {
   public static Mono<ProductRating> lookupInCache(Long productId) {
      return Mono.defer(() -> {
         if (Math.random() < .5) {
            log.debug("Cache hit");
            return Mono.just(new ProductRating(5));
         } else {
            return Mono.empty();
         }
      }).delayElement(ofMillis(10));
   }

   public static Mono<Void> putInCache(Long productId, ProductRating rating) {
      return Mono.empty().delayElement(ofMillis(10)).then();
   }
}
