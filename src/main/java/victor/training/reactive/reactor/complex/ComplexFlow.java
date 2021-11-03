package victor.training.reactive.reactor.complex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
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
          .flatMap(ComplexFlow::retrieveProductById, 10)
          .collectList();
   }

   private static Mono<Product> retrieveProductById(Long productId) {
      return WebClient.create()
          .get()
          .uri("http://localhost:9999/api/product/" + productId)
          .retrieve()
          .bodyToMono(ProductDetailsResponse.class)
          .map(ProductDetailsResponse::toEntity)
          ;
   }
}

