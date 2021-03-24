package victor.training.reactive.reactor.complex;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.intro.ThreadUtils;

import java.sql.Connection;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
public class ComplexFlow {

   public static void main(String[] args) {
      List<Long> productIds = LongStream.range(1, 11).boxed().collect(Collectors.toList());

      Mono<List<Product>> listMono = mainFlow(productIds);

      listMono.subscribe(list -> log.info("Done: " + list));
      log.info("GATA, threadul  d http se intoarce in pool");
      ThreadUtils.sleep(20000);
   }


   private static Mono<List<Product>> mainFlow(List<Long> productIds) {


      Flux<Long> idFlux = Flux.fromIterable(productIds);

      Flux<Product> productFlux = idFlux
          .buffer(2)
          .flatMap(ComplexFlow::getMultipleProductDetails, 4)

//          .delayUntil(product -> auditResealedProduct(product)) // intarzia Produsele pana cand sunt si auditate

          .doOnNext(product ->
             auditResealedProduct(product).subscribe() // Mandatory sa faci .subscribe in RX
          ) // intarzia Produsele pana cand sunt si auditate

         .flatMap(product -> fetchRating(product.getId())
             .map(rating -> product.withRating(rating)))


          .subscribeOn(Schedulers.single())// ma dau mare... pute a NodeJS
           ;

      return productFlux.collectList();
   }



   @SneakyThrows
   public static Mono<ProductRatingDto> fetchRating(Long productId) {
      return WebClient.create().get().uri("http://localhost:9999/api/rating/{}", productId)
          .retrieve()
          .bodyToMono(ProductRatingDto.class);
   }

   @SneakyThrows
   public static Mono<Void> auditResealedProduct(Product product) {

//      Connection c;
//      c.ab

      if (!product.isResealed()) {
         return Mono.empty(); // ~CompletableFuture.completed.
      }
      log.info("Calling Audit REST");
      return WebClient.create().get().uri("http://localxxxxhost:9999/api/audit-resealed/" + product)
          .retrieve()
          .toBodilessEntity()
          .then();

   }

   @SneakyThrows
   public static Mono<Product> getSingleProductDetails(Long productId) {
      log.info("Calling REST");

      return WebClient.create().get().uri("http://localhost:9999/api/product/1")
          .retrieve()
          .bodyToMono(ProductDto.class)
          .map(dto -> dto.toEntity())
          .timeout(Duration.ofMillis(500))
          .retry(3)
          ;
   }
   @SneakyThrows
   public static Flux<Product> getMultipleProductDetails(List<Long> productIdPage) {
      log.info("Calling REST");

      return WebClient.create().post().uri("http://localhost:9999/api/product/many")
          .bodyValue(productIdPage)
          .retrieve()
          .bodyToFlux(ProductDto.class)
          .map(dto -> dto.toEntity());
   }



}

