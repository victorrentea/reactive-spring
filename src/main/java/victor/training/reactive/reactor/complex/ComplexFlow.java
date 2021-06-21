package victor.training.reactive.reactor.complex;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
public class ComplexFlow {

   public static void main(String[] args) {
      List<Long> productIds = LongStream.range(1, 10).boxed().collect(Collectors.toList());
//      mainFlow(productIds).flatMap(cassandraRepo::save);
      Mono<List<Product>> listMono = mainFlow(productIds).collectList();

      List<Product> products = listMono.block(); // unusual, only here to stop main thread from exiting
      log.info("Done. Got {} products: {}", products.size(), products);
   }


   private static Flux<Product> mainFlow(List<Long> productIds) {
     return Flux.fromIterable(productIds)
          .flatMap(ComplexFlow::getProductById, 100);
   }

   @SneakyThrows
   public static Mono<Product> getProductById(Long productId) {
      log.info("Calling Get Product Details REST");
      return WebClient.create()
          .get()
          .uri("http://localhost:9999/api/product/{id}", productId)
          .retrieve()
          .bodyToMono(ProductDetailsResponse.class)
          .map(ProductDetailsResponse::toEntity);
      // TODO 2 is it ok to call them N times over network ?
      //....
   }

}

