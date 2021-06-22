package victor.training.reactive.reactor.complex;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toList;

@Slf4j
public class ComplexFlow {

   public static void main(String[] args) {
      List<Long> productIds = LongStream.rangeClosed(1, 10).boxed().collect(toList());
//      mainFlow(productIds).flatMap(cassandraRepo::save);
      Mono<List<Product>> listMono = mainFlow(productIds).collectList();


//      CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> "a");
//      Mono<String> stringMono = Mono.fromCompletionStage(cf);

//      listMono.subscribe(products -> {
         List<Product> products = listMono.block(); // unusual, only here to stop main thread from exiting
         log.info("Done. Got {} products: {}", products.size(), products);
//      });
// here main() = the only live thread.  dies
   }


   private static Flux<Product> mainFlow(List<Long> productIds) {
       return Flux.fromIterable(productIds)
          .buffer(2)// Flux<List<id>>   = Flux<page of ids>
          // the set if ids will be broken in pages of 2 items (eg 10 items => [1,2], [3,4], ... [9,10]
          .flatMap(ComplexFlow::getProductById, 1 ) // only 3 pages will be sent in parallel to their API. eg [1,2] || [3,4] || [5,6]

          // flux of 10 products (5 of them are resealed)
//          .filter(p -> p.isResealed())
          // flux of 5 products, all resealed

           // fire and forget : 1 of the 2 valid use cases to to .subscribe manually()
           // no errors propagating
           // we don't wait.
          .doOnNext(p -> auditResealedProduct(p).subscribe())


            // propagate exceptions from audit into master flow
//          .flatMap(p->auditResealedProduct(p).thenReturn(p))
          // simplerdelayUntil(product -> auditResealedProduct(product))

         // here I want to return 10 items
          ;
   }

   // /audit-resealed should be called only for products having product.isResealed() = true

   @SneakyThrows
   public static Mono<Void> auditResealedProduct(Product product) {
      if (product.isResealed()) {
         // TODO only audit resealed products !
         return WebClient.create().get().uri("http://localhost:9999/api/audit-resealed/" + product)
             .retrieve()
             .toBodilessEntity()
             .doOnSubscribe(s -> log.info("Calling Audit REST"))
             .then();
      } else {
         return Mono.empty();
      }
   }

   @SneakyThrows
   public static Flux<Product> getProductById(List<Long> productIds) {

      log.info("Calling Get Product Details REST");
      String hugeString = productIds.toString(); // 10 MB
      return WebClient.create()
          .post()
          .uri("http://localhost:9999/api/product/many")

          .contentType(MediaType.APPLICATION_JSON)
          .body(Flux.range(1,10000), Integer.class) // GOOD streaming data as it's toStringed
//          .body(Flux.fromIterable(productIds), Long.class) // GOOD streaming data as it's toStringed
//          .body(Mono.just(hugeString), String.class) // BAD for huge request

          .retrieve()
          .bodyToFlux(ProductDetailsResponse.class)
          .subscribeOn(Schedulers.boundedElastic())
          .doOnSubscribe(s -> log.info("Calling REST for " + productIds))
          .doOnNext(d -> log.info("Got data for REST for " + productIds))
          .map(ProductDetailsResponse::toEntity)
          ;
   }

}

