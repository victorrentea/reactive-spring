package victor.training.reactor.complex;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
@Slf4j
public class ComplexFlow {

   public static void main(String[] args) {

// Mono si Flux sunt Publisheri

      List<Long> productIds = Arrays.asList(1L,2L);

      Mono<List<Product>> listMono = mainFlow(productIds);

      listMono
          .doOnNext(list -> log.info("Peeking : " + list))
          .subscribe(list -> log.info("Result: " + list))
      ;
   }

   private static Mono<List<Product>> mainFlow(List<Long> productIds) {
      return Flux.fromIterable(productIds)
         .flatMap(ComplexFlow::convertBlockingToReactive)
         .collectList()
         ;
   }

   public static Mono<Product> convertBlockingToReactive(Long productId) {
      return Mono.just(ExternalAPI.getProductDetails(productId));
   }

}
@Slf4j
class ExternalAPI {
   @SneakyThrows
   public static Product getProductDetails(Long productId) {
      // ne inchipuim apel REST / WS
      log.info("Calling REST");
      Thread.sleep(1000);
      return new Product();
   }
}

@Data
class Product {

}