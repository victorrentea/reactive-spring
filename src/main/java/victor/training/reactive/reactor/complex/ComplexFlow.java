package victor.training.reactive.reactor.complex;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.ContextView;
import victor.training.reactive.intro.ThreadUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
public class ComplexFlow {

   public static final Scheduler FR_GOV = Schedulers.newBoundedElastic(2, 100, "fr.gov");

   public static void main(String[] args) {
      List<Long> productIds = LongStream.range(0, 100).boxed().collect(Collectors.toList());

      log.info("START");
      Flux<Product> listMono = mainFlow(productIds);
      log.info("I got the mono");
      List<Product> products = listMono.collectList().block();
      log.info("Done. Got {} products: {}", products.size(), products);
      ThreadUtils.sleep(7000);
   }

   // in a reactive app, when you see a func return a Publisher, you expect to block ONLY when you subscribe
   private static Flux<Product> mainFlow(List<Long> productIds) {


      return Flux.fromIterable(productIds)
          .buffer(2)
          .flatMap(ComplexFlow::fetchProductDetailsInPages)
          .flatMap(ComplexFlow::getRatingAndAuditInParallel)
          .doOnNext(p -> log.info("Product on main flow"));
   }

   private static Mono<Product> getRatingAndAuditInParallel(Product product) {
      Mono<String> auditMono = auditProduct(product)
          .subscribeOn(Schedulers.single())
          .thenReturn("useless"); // > zipWith wanted data signal in order to proceed

//      Mono.deferContextual(context -> {
//         runWithMDC(context, () -> callLegacyCode());
////         context.get("usermetda");
////
////             ThreadLocal.set("mdc", user);
////             try {
////                call legacy code
////                    return just("results");
////                return empty();
////             }finally {
////                ThreadLocal.remove();
////             }
//      })

      Mono<Product> productWithRatingMono = retrieveRatingWithCache(product)
          .map(product::withRating)
//          .doOnEach(signal -> {
//             signal.getContextView().get("usermetda");
//             ThreadLocal.set("mdc", user);
//             try {
//                call legacy code
//             }finally {
//                ThreadLocal.remove();
//             }
//          })
          .subscribeOn(Schedulers.single())
          ;

      return productWithRatingMono.zipWith(auditMono, (p, v) -> p);
   }

   private static Mono<ProductRatingResponse> retrieveRatingWithCache(Product product) {
      return ExternalCacheClient.lookupInCache(product.getId())


//            .doOnSubscribe(s -> {
//               System.out.println("For the initial and every retry");
//            })

          .doOnEach(signal -> {
             if (signal.isOnSubscribe()) {
                ContextView context = signal.getContextView();
                log.info("AAAAAAA");
               String uuid = context.get("requestUUID");
               log.info("REQUESTUUID: >" + uuid + "<");
             }
          })
            .retry(100)
//            .retry(Retry.)
          .contextWrite(context -> context.put("requestUUID", UUID.randomUUID().toString()))

          .onErrorResume(t -> Mono.empty())

          .switchIfEmpty(ExternalAPIs.fetchProductRating(product.getId())
              .doOnNext(rating ->
                  ExternalCacheClient.putInCache(product.getId(), rating)
                      .subscribe()));
   }


   //   public static final ConnectableFlux<Long> timer = Flux.interval(Duration.ofMillis(1000)).publish();
//   static {
//      timer.connect();
//   }
   @SneakyThrows
   public static Flux<Product> fetchProductDetailsInPages(List<Long> productIds) {
      return WebClient.create()
          .post()
          .uri("http://localhost:9999/api/product/many")
          .bodyValue(productIds)
          .retrieve()
          .bodyToFlux(ProductDetailsResponse.class)
          .subscribeOn(Schedulers.boundedElastic())
          .publishOn(Schedulers.boundedElastic())
          .map(ProductDetailsResponse::toEntity);
   }


   @SneakyThrows
   public static Mono<Void> auditProduct(Product product) {
      if (product.isResealed()) {
         return WebClient.create().get().uri("http://localhost:9999/api/audit-resealed/" + product)
             .retrieve()
             .toBodilessEntity()
             .then()
             .doOnSubscribe(s -> log.info("Calling AUDIT for " + product.getId()))
             .doOnTerminate(() -> log.info("Call DONE AUDIT for " + product.getId()))
//             .subscribeOn(Schedulers.boundedElastic())
             ;
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

