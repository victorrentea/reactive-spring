package victor.training.reactive.reactor.complex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.size;
import static victor.training.reactive.intro.Utils.*;

@RestController
@Slf4j
//@SpringBootApplication
public class ComplexFlowApp implements CommandLineRunner {
   public static void main(String[] args) {
      SpringApplication.run(ComplexFlowApp.class, "--server.port=8081");
   }

   @EventListener(ApplicationStartedEvent.class)
   public void setupBlockingDetection() {
      installBlockHound(List.of(
          Tuples.of("io.netty.resolver.HostsFileParser", "parse"),
          Tuples.of("victor.training.reactive.reactor.complex.ComplexFlowMain", "executeAsNonBlocking")
      ));
   }

   @Override
   public void run(String... args) throws Exception {
      log.info("Calling myself automatically once");
      WebClient.create().get().uri("http://localhost:8081/complex").retrieve().bodyToMono(String.class)
          .subscribe(
              data -> log.info("COMPLETED with: "+data),
              error -> log.error("FAILED! See above why" )
          );
      startRefreshWireMockStubsFromJsonContinuously();
   }

   @GetMapping("complex")
   public static Mono<String> executeAsNonBlocking() {
      List<Long> productIds = LongStream.rangeClosed(1, 1000).boxed().collect(toList());

      return mainFlow(productIds)
          .map(list -> "Done. Got " + size(list) + " products: " + list);
   }

   // ================================== work below ====================================

   public static Mono<List<Product>> mainFlow(List<Long> productIds) {
//      return Flux.fromIterable(productIds)
//          .flatMap(productId ->
//              WebClient.create().get().uri("http://localhost:9999/api/product/" + productId)
//                  .retrieve()
//                  .bodyToMono(ProductDetailsResponse.class)
//                  .map(e -> e.toEntity())
//          ).collectList();

//      Mono<Void> ack = Mono.just(new Void());
      return Flux.fromIterable(productIds)
          // Flux<List<Long>> grouping them in pages of 2
          .buffer(2)
//          .flatMap(productIdList -> retrieveMultipleProducts(productIdList)) // infinite requests in parallel
//          .flatMap(productIdList -> retrieveMultipleProducts(productIdList), 4) // max 4 calls in parallel.
//          .concatMap(productIdList -> retrieveMultipleProducts(productIdList)) // one request at a time
          .flatMapSequential(productIdList -> retrieveMultipleProducts(productIdList)) // one request at a time

//          .flatMap(product -> auditProduct(product).thenReturn(product))
          .delayUntil(product -> auditProduct(product))

          .collectList();

   }

   private static Mono<Void> auditProduct(Product product) {
      if (!product.isResealed()) {
         return Mono.empty();
      }
      return ExternalAPIs.auditResealedProduct(product)
          .doOnError(e -> log.error("OMG i'm afraid", e))
          .onErrorResume(e -> Mono.empty()) // converts an ERROR signal into a COMPLETION signal
          ;
   }

   private static Flux<Product> retrieveMultipleProducts(List<Long> productIdList) {
      return WebClient.create()
          .post()
          .uri("http://localhost:9999/api/product/many")
          .body(productIdList, Long.class)
          .retrieve()
          .bodyToFlux(ProductDetailsResponse.class)
          .map(ProductDetailsResponse::toEntity);
   }

//   private static Mono<Product> retrieveProduct(Long productId) {
//      return WebClient.create()
//          .get()
//          .uri("http://localhost:9999/api/product/" + productId)
//          .retrieve()
//          .bodyToMono(ProductDetailsResponse.class)
//          .map(ProductDetailsResponse::toEntity);
//   }

}

