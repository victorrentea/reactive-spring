package victor.training.reactive.reactor.complex;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toList;

@Slf4j
public class ComplexFlow {

   public static void main(String[] args) {
      List<Long> productIds = LongStream.range(1, 10).boxed().collect(toList());
//      mainFlow(productIds).flatMap(cassandraRepo::save);
      Mono<List<Product>> listMono = mainFlow(productIds).collectList();

      List<Product> products = listMono.block(); // unusual, only here to stop main thread from exiting
      log.info("Done. Got {} products: {}", products.size(), products);
   }


   private static Flux<Product> mainFlow(List<Long> productIds) {
      return Flux.fromIterable(productIds) // FLux<id>
          .buffer(2)// Flux<List<id>>   = Flux<page of ids>
          .flatMap(ComplexFlow::getProductById, 2)
          .flatMap(list -> Flux.fromIterable(list));
   }

   @SneakyThrows
   public static Mono<List<Product>> getProductById(List<Long> productIds) {
      log.info("Calling Get Product Details REST");
      return (Mono<List<Product>>) WebClient.create()
          .post()
          .uri("http://localhost:9999/api/product/many")
          .body(Mono.just(productIds.toString()), String.class)
          .retrieve()
          .bodyToMono(new ParameterizedTypeReference<List<ProductDetailsResponse>>() {})
          .map(listDto -> listDto.stream().map(ProductDetailsResponse::toEntity).collect(toList()));
      // TODO 2 is it ok to call them N times over network ?
      //....
   }

}

