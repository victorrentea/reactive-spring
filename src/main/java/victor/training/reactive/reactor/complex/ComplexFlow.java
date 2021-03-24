package victor.training.reactive.reactor.complex;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.intro.ThreadUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
public class ComplexFlow {

   public static void main(String[] args) {
      List<Long> productIds = LongStream.range(1, 11).boxed().collect(Collectors.toList());

      Mono<List<Product>> listMono = mainFlow(productIds);

      listMono.subscribe(list -> log.info("Done: " + list));
      log.info("GATA, threadul  d http se intoarce in pool");
      ThreadUtils.sleep(20000);
   }


   private static Mono<List<Product>> mainFlow(List<Long> productIds) {


      Flux<Long> idFlux = Flux.fromIterable(productIds);

      Flux<Product> productFlux = idFlux
          .buffer(2)
          .flatMap(id -> getMultipleProductDetails(id), 4)
          .subscribeOn(Schedulers.single())// ma dau mare... pute a NodeJS
           ;

//      Stream<Stream<Integer>> ---> .flatMap

      return productFlux.collectList();
   }



   @SneakyThrows
   public static Mono<Product> getSingleProductDetails(Long productId) {
      log.info("Calling REST");

      return WebClient.create().get().uri("http://localhost:9999/api/product/1")
          .retrieve()
          .bodyToMono(ProductDto.class)
          .map(dto -> dto.toEntity());
   }
   @SneakyThrows
   public static Flux<Product> getMultipleProductDetails(List<Long> productIdPage) {
      log.info("Calling REST");

      return WebClient.create().post().uri("http://localhost:9999/api/product/many")
          .bodyValue(productIdPage)
          .retrieve()
          .bodyToFlux(ProductDto.class)
          .map(dto -> dto.toEntity());
   }



}

