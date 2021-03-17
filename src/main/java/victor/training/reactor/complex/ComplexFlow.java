package victor.training.reactor.complex;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
public class ComplexFlow {
   public static void main(String[] args) {
      List<Long> productIds = LongStream.range(1, 10).boxed().collect(Collectors.toList());
      Mono<List<Product>> listMono = mainFlow(productIds).timeout(Duration.ofSeconds(2));
      List<Product> products = listMono.block(); // unusual, only here to stop main thread from exiting
      log.info("Done: " + products);
   }


   private static Mono<List<Product>> mainFlow(List<Long> productIds) {
      return Flux.fromIterable(productIds)
          .buffer(2)
          .flatMap(ComplexFlow::getSingleProductDetails, 3)

          .delayUntil(product -> auditResealedProduct(product))
          // the returned mono is just waited to complete, before the product is pushed further down the chain

          .collectList();
   }


   @SneakyThrows
   public static Mono<Void> auditResealedProduct(Product product) {
      log.info("Calling Audit REST");
      return WebClient.create().get().uri("http://localhost:9999/api/audit-resealed/" + product.getId())
          .retrieve()
          .toBodilessEntity()
          .then()
//         .subscribe()
      ;

   }

   @SneakyThrows
   public static Flux<Product> getSingleProductDetails(List<Long> productId) {
      return WebClient.create().post().uri("http://localhost:9999/api/product/many", productId)
          .body(Mono.just(new ManyProductRequest(productId)), ManyProductRequest.class)
          .retrieve()
          .bodyToFlux(ProductDto.class)
          .map(dto -> dto.toEntity())
          .subscribeOn(Schedulers.boundedElastic());
   }

}

@Value
class ManyProductRequest {
   List<Long> ids;
}
