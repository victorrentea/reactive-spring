package victor.training.reactive.reactor.complex;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.intro.ThreadUtils;

import java.io.FilterInputStream;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.zip.GZIPInputStream;

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

//          .flatMap(product -> auditResealedProduct(product).thenReturn(product)) // acceptable<< T
            .delayUntil(ComplexFlow::auditResealedProduct)

          .doOnNext(p -> log.info("Product on main flow"));
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
   public static Mono<Void> auditResealedProduct(Product product) {
      // TODO only audit resealed products !
      return WebClient.create().get().uri("http://localhost:9999/api/auditoups!!-resealed/" + product)
          .retrieve()
          .toBodilessEntity()
          .then();
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

