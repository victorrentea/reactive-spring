package victor.training.reactive.reactor.complex;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
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
          .uri("http://localhost:9999/api/product/-9999")
          .retrieve()
          .bodyToMono(ProductDetailsResponse.class)
          .map(ProductDetailsResponse::toEntity)
          .block();
   }

   private static Mono<List<Product>> mainFlow(List<Long> productIds) {
//      return mainFlowOriginal(productIds);
      return mainFlowOptimizat(productIds);
   }
   private static Mono<List<Product>> mainFlowOptimizat(List<Long> productIds) {
      Flux<Product> productFlux = Flux.fromIterable(productIds)
          .buffer(2)
          .flatMap(ComplexFlow::retrieveProductByIdInPages, 10)
          .flatMap(product -> fetchCachedRating(product)
              .map(product::withRating))
          .cache();

      Flux<Product> resealedProductsFlux = productFlux.filter(Product::isResealed)
          .buffer(2)
          .flatMap(ComplexFlow::dummy);

      return resealedProductsFlux
          .mergeWith(productFlux.filter(Predicate.not(Product::isResealed)))
          .collectList();
   }
   private static Mono<List<Product>> mainFlowOriginal(List<Long> productIds) {
      return Flux.fromIterable(productIds)
          .buffer(2)
          .flatMap(ComplexFlow::retrieveProductByIdInPages, 10)
          .flatMap(product -> fetchCachedRating(product)
              .map(product::withRating))
          .groupBy(p -> p.isResealed())
          .flatMap(groupedFlux -> {
             if (groupedFlux.key()) {
                return groupedFlux.buffer(2).flatMap(paginaDeProduseDeAudit -> dummy(paginaDeProduseDeAudit));
             } else {
                return groupedFlux;
             }
          }).collectList();
   }

   private static Flux<Product> dummy(List<Product> pagina) {
      System.out.println("Cica auditez doar cate 2 produse resealed: " + pagina);
      return Flux.fromIterable(pagina)
          .flatMap(p -> auditProduct(p).thenReturn(p));
   }


   private static Mono<ProductRatingResponse> fetchCachedRating(Product product) {
      // return lookupInCache(id) ?:
      //    fetchProductRating(id)
      //    ?.also { FireAndForget.launch {  ExternalCacheClient.putInCache(id, valoare) } }
      return
          lookupInCache(product.getId())
              .switchIfEmpty(
                  ExternalAPIs.fetchProductRating(product.getId())
//                      .retry(2)
//                      .retryWhen(Retry.backoff(3, Duration.ofMillis(5)))
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

