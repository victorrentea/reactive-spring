package victor.training.reactive.reactor.complex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
public class ComplexFlow {

   public static void main(String[] args) {
      List<Long> productIds = LongStream.rangeClosed(1, 100).boxed().collect(Collectors.toList());
      heatupWebClient();
//      BlockHound.install();

      long t0 = System.currentTimeMillis();
      Mono<List<Product>> listMono = mainFlow(productIds);
      List<Product> products = listMono.block(); // unusual, only here to stop main thread from exiting
      long t1 = System.currentTimeMillis();


      log.info("Took {}" , t1-t0);
      log.info("Done. Got {} products: {}", products.size(), products);
   }

   private static void heatupWebClient() {
      WebClient.create()
          .get()
          .uri("http://localhost:9999/api/product/" + 1)
          .retrieve()
          .bodyToMono(ProductDetailsResponse.class)
          .map(ProductDetailsResponse::toEntity)
          .block();
   }

   private static Mono<List<Product>> mainFlow(List<Long> productIds) {

      return Flux.fromIterable(productIds)
          .buffer(2)
          .flatMap(ComplexFlow::retrieveProductByIdInPages, 10)
//          .doOnNext( p-> ExternalAPIs.auditResealedProduct(p)
//              .doOnError(t -> log.error("BUM", t)) // daca nu pui e ca un catch() {}
//              .subscribe())
          .flatMap(p -> ComplexFlow.auditProduct(p).thenReturn(p))
//          .delayUntil(ComplexFlow::auditProduct)
          .collectList();
   }

   // equivalent coroutine
   // for (id in productIds) {
   //  val p = async { getProduct }
   //  if (p.resealed) audit (p)
   // }


   private static Mono<Void> auditProduct(Product product) {
      if (product.isResealed())
        return ExternalAPIs.auditResealedProduct(product);
      else {
         return Mono.empty();
      }
   }

   private static Flux<Product> retrieveProductByIdInPages(List<Long> productIds) {
      return WebClient.create()
          .post()
          .uri("http://localhost:9999/api/product/many")
          .body(Mono.just(productIds.toString()), String.class)
          .retrieve()
          .bodyToFlux(ProductDetailsResponse.class)
          .map(ProductDetailsResponse::toEntity)
          ;
   }
}

