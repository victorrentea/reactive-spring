package victor.training.reactive.reactor.complex;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.intro.ThreadUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
public class ComplexFlow {

   public static final Scheduler FR_GOV = Schedulers.newBoundedElastic(2, 100, "fr.gov");

   public static void main(String[] args) {
      List<Long> productIds = LongStream.range(0, 10).boxed().collect(Collectors.toList());

      log.info("START");
      Flux<Product> listMono = mainFlow(productIds);
      log.info("I got the mono");
      List<Product> products = listMono.collectList().block();
      log.info("Done. Got {} products: {}", products.size(), products);
      ThreadUtils.sleep(5000);
   }

   // in a reactive app, when you see a func return a Publisher, you expect to block ONLY when you subscribe
   private static Flux<Product> mainFlow(List<Long> productIds) {
      return Flux.fromIterable(productIds)
          .buffer(2)
          .flatMap(idPage -> fetchSingleProductDetails(idPage)/*, 4*/)
//          .doOnNext(product -> auditResealedProduct(product)
//              .doOnError(error -> log.error("Error auditing product:",error))
//               .subscribe()  ) // acceptable<< T
          // 1) THE ONLY place where you should subscribe(): because you don't care about FAILURES >> normally frameworks will subscribe to your publishers: eg return a Mono from a @GetMapping
          // 2) Problem: cancelling the subscription for the MainFlow will NOT cancel also the subscription to audit http req
          // >> the auditing will happen anyway despite .cancel on mainflow, if it got to .subscribe()

//          .flatMap(product -> auditResealedProduct(product).thenReturn(product)) // acceptable<< T or equivalent..
          .delayUntil(ComplexFlow::auditProduct)

          .flatMap(product -> retrieveRatingWithCache(product).map(product::withRating))

          .doOnNext(p -> log.info("Product on main flow"));
   }

   private static Mono<ProductRatingResponse> retrieveRatingWithCache(Product product) {
      return ExternalCacheClient.lookupInCache(product.getId())
          .switchIfEmpty(ExternalAPIs.fetchProductRating(product.getId())
              .doOnNext(rating -> ExternalCacheClient.putInCache(product.getId(), rating).subscribe()));
   }


   //   public static final ConnectableFlux<Long> timer = Flux.interval(Duration.ofMillis(1000)).publish();
//   static {
//      timer.connect();
//   }
   @SneakyThrows
   public static Flux<Product> fetchSingleProductDetails(List<Long> productIds) {
      return WebClient.create()
          .post()
          .uri("http://localhost:9999/api/product/many")
          .bodyValue(productIds)
          .retrieve()
          .bodyToFlux(ProductDetailsResponse.class)
          .subscribeOn(Schedulers.boundedElastic())
          .publishOn(Schedulers.boundedElastic()).map(ProductDetailsResponse::toEntity);
   }


   @SneakyThrows
   public static Mono<Void> auditProduct(Product product) {
      if (product.isResealed()) {
         return WebClient.create().get().uri("http://localhost:9999/api/audit-resealed/" + product)
             .retrieve()
             .toBodilessEntity()
             .then()
             .subscribeOn(Schedulers.boundedElastic());
      } else {
         return Mono.empty();
      }
   }

//
//      return Flux.defer(() -> {
//         log.info("Calling Get Product Details REST");
//         RestTemplate rest = new RestTemplate();
//         ProductDetailsResponse[] dtos = rest.postForObject("http://localhost:9999/api/product/many", productIds, ProductDetailsResponse[].class);
//         System.out.println(Arrays.toString(dtos));
//
//         return Flux.fromArray(dtos).map(ProductDetailsResponse::toEntity);
//      })
//          .subscribeOn(FR_GOV)
//          .publishOn(Schedulers.boundedElastic())
//          ;


//   }
}

