package victor.training.reactive.reactor.complex;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static victor.training.reactive.reactor.complex.ExternalCacheClient.lookupInCache;

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


          .groupBy(p -> p.isResealed())
          .flatMap(groupedFlux -> {
             if (groupedFlux.key()) {
                return groupedFlux.buffer(2).flatMap(paginaDeProduseDeAudit -> dummy(paginaDeProduseDeAudit));
             } else {
                return groupedFlux;
             }
          })

//          .scan(Collections.emptyMap(), (map, product) -> map  . contcat (product.key , produs))
//          .scan

          .delayUntil(ComplexFlow::auditProduct) // e mai lenta daca audit dureaza timp: trage pe rand (singe threaded)
          .flatMap(product -> fetchCachedRating(product)
              .map(product::withRating))
          .collectList();
   }

   private static Flux<Product> dummy(List<Product> pagina) {
      System.out.println("Cica auditez doar cate 2 produse resealed: " + pagina);
      return Flux.fromIterable(pagina);
   }

   private static Mono<ProductRatingResponse> fetchCachedRating(Product product) {
      // return lookupInCache(id) ?:
      //    fetchProductRating(id)
      //    ?.also { FireAndForget.launch {  ExternalCacheClient.putInCache(id, valoare) } }
      return
          lookupInCache(product.getId())
              .switchIfEmpty(
                  ExternalAPIs.fetchProductRating(product.getId())
                      .doOnNext(rating -> ExternalCacheClient.putInCache(product.getId(), rating)./*doOnError().*/subscribe())

              );
   }

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

