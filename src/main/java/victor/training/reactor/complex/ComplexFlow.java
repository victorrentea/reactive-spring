package victor.training.reactor.complex;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import victor.training.reactivespring.start.ThreadUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static victor.training.reactor.complex.ExternalCacheClient.*;

@Slf4j
public class ComplexFlow {
   public static void main(String[] args) {
      List<Long> productIds = LongStream.range(1, 10).boxed().collect(Collectors.toList());
      Mono<List<Product>> listMono = mainFlow(productIds);//.timeout(Duration.ofSeconds(2));
      List<Product> products = listMono.block(); // unusual, only here to stop main thread from exiting
      log.info("Done {}: {}",products.size(), products);
   }


   // What if your caller is not rx ?
   // TODO get a callback from him to call with data
   // TODO toFuture : allowing him to .thenAccept() async

   private static Mono<List<Product>> mainFlow(List<Long> productIds) {
      return Flux.fromIterable(productIds)
          .buffer(2)
          .flatMap(ComplexFlow::getSingleProductDetails, 3)
          .delayUntil(ComplexFlow::auditResealedProduct)
          .flatMap(product -> lookupInCache(product.getId())
              .switchIfEmpty(getRemoteRating(product))
              .map(rating -> product.withRating(rating)))
          .collectList();
   }

   private static Mono<ProductRating> getRemoteRating(Product product) {
      return fetchRating(product.getId())
          .timeout(ofMillis(200))
          .onErrorResume(ex -> ex instanceof TimeoutException,
              ex -> Mono.just(null))
          .doOnNext(rating -> putInCache(product.getId(), rating)
              .subscribe(v -> {
              }, ex -> System.err.println(ex.getMessage()))); // "silencing the out of band exception"
   }


   @SneakyThrows
   public static Mono<ProductRating> fetchRating(Long productId) {
      log.info("Calling fetchRating");
      return WebClient.create().get().uri("http://localhost:9999/api/rating/{}", productId)
          .retrieve()
          .bodyToMono(ProductRating.class)
          .doOnNext(e -> log.info("Got Rating Response " + System.identityHashCode(e)))
          .delayUntil(rating -> Math.random() < .5 ?
              Mono.just("fast").delayElement(ofMillis(50)) :
              Mono.just("slow").delayElement(ofSeconds(1)))
          .doOnNext(e -> log.info("Emitting Rating Response " + System.identityHashCode(e)))
          ;
   }

   @SneakyThrows
   public static Mono<Void> auditResealedProduct(Product product) {
      if (!product.isResealed()) {
         return Mono.empty();
      }
      log.info("Calling Audit REST");
      return WebClient.create().get().uri("http://localhost:9999/api/audit-resealed/" + product.getId())
          .retrieve()
          .toBodilessEntity()
          .then();
   }

   public static final Scheduler myBounded = Schedulers.newBoundedElastic(200, 200, "prod-det");

   @SneakyThrows
   public static Flux<Product> getSingleProductDetails(List<Long> productId) {
      return WebClient.create().post().uri("http://localhost:9999/api/product/many", productId)
          .body(Mono.just(new ManyProductRequest(productId)), ManyProductRequest.class)
          .retrieve()
          .bodyToFlux(ProductDto.class)
          .map(dto -> dto.toEntity())
          .subscribeOn(myBounded)
          ;
   }


}

@Value
class ManyProductRequest {
   List<Long> ids;
}
