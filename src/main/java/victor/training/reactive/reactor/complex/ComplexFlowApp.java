package victor.training.reactive.reactor.complex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.size;
import static victor.training.reactive.intro.Utils.*;

@RestController
@Slf4j
@SpringBootApplication
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


   public static Mono<List<Product>> mainFlow(List<Long> productIds) {

      return Flux.fromIterable(productIds)
//          .doOnNext(id -> log.debug("ID is fired" + id))
          .buffer(2)
          .concatMap(idList -> loadProductDto(idList), 10) // CONS: can finish all data SLOWER
//          .doOnNext(dto -> log.info("Got product response " + dto))
          .map(dto -> dto.toEntity())

//          .flatMap(p -> horrorMethod(p).thenReturn(p) ) // smart but proves how important is to know operators
          .delayUntil(p -> horrorMethod(p)) // smart but proves how important is to know operators


          .collectList();
   }

   private static Mono<Void> horrorMethod(Product p) {
      if (p.isResealed()) {
         return ExternalAPIs.auditResealedProduct(p);
      }
      return Mono.empty();
   }

   private static Flux<ProductDetailsResponse> loadProductDto(List<Long> productId) {
      return WebClient.create()
          .post()
          .uri("http://localhost:9999/api/product/many")
          .bodyValue(productId)
           .retrieve()
          .bodyToFlux(ProductDetailsResponse.class);
   }


   // 1: do you want me to go a) slower b) same c) faster.

   // 2: do you want: x) code y) theory

}

