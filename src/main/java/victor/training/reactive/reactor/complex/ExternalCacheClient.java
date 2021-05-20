package victor.training.reactive.reactor.complex;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import static java.time.Duration.ofMillis;

@Slf4j
public class ExternalCacheClient {
   public static Mono<ProductRatingResponse> lookupInCache(Long productId) {
      return Mono.delay(ofMillis(5000))
          .flatMap(n -> Mono.defer(() -> {
             if (Math.random() < .5) {
                log.debug("Cache hit");
                return Mono.just(new ProductRatingResponse(5));
             } else {
                return Mono.<ProductRatingResponse>empty();
             }
          }))

          .doOnSubscribe(s -> log.info("Calling CACHE for " + productId))
          .doOnTerminate(() -> log.info("Call DONE CACHE for " + productId))

          ;//.publishOn(Schedulers.single());
   }

   public static Mono<Void> putInCache(Long productId, ProductRatingResponse rating) {
      log.info("Put in cache " + productId);
      return Mono.empty().delayElement(ofMillis(10)).then();
   }
}
