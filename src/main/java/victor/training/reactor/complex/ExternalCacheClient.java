package victor.training.reactor.complex;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactivespring.start.ThreadUtils;

import java.time.Duration;

import static java.time.Duration.ofMillis;

@Slf4j
public class ExternalCacheClient {
   public static Mono<ProductRating> lookupInCache(Long productId) { // ~Optional<ProductRating>
      return Mono.defer(() -> {
         if (Math.random() < .5) {
            log.debug("Cache hit");
            return Mono.just(new ProductRating(5));
         } else {
            return Mono.empty();
         }
      }).delayElement(ofMillis(10));//.publishOn(Schedulers.single());
   }

   public static Mono<Void> putInCache(Long productId, ProductRating rating) {

    return Mono.fromRunnable( () -> {
       log.info("Put in cache " + productId);
      ThreadUtils.sleep(1000);
       if (Math.random() < .3) {
          throw new RuntimeException("buba");
       }
    }).then();
//      return Mono.empty();//.delayElement(ofMillis(10)).then();
   }
}
