package victor.training.reactor.complex;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactivespring.start.ThreadUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
public class ComplexFlow {

   public static void main(String[] args) {

      log.info("Start");
// Mono si Flux sunt Publisheri

      List<Long> productIds = Arrays.asList(1L,2L);

      Mono<List<Product>> listMono = mainFlow(productIds);

      listMono
          .doOnNext(list -> log.info("Peeking : " + list))
          .subscribe(list -> log.info("Result: " + list))
      ;
      log.info("Gata");
      ThreadUtils.sleep(4000);
   }

   private static Mono<List<Product>> mainFlow(List<Long> productIds) {
      return Flux.fromIterable(productIds)

          .flatMap(productId -> convertBlockingToReactive(productId))
         .collectList()
         ;
   }
   public static Mono<Product> convertBlockingToReactive(Long productId) {
      return Mono
          .defer(() ->Mono.just(ExternalAPI.getProductDetails(productId)))
          .subscribeOn(Schedulers.boundedElastic())
          ;
   }

}
@Slf4j
class ExternalAPI {
   @SneakyThrows
   public static Product getProductDetails(Long productId) {
      // ne inchipuim apel REST / WS
      log.info("Calling REST");

      RestTemplate rest = new RestTemplate();
      ProductDto dto = rest.getForObject("http://localhost:9999/api/product/1", ProductDto.class);

      return new Product(dto.getName(), dto.isActive());
   }
}

@Data
@AllArgsConstructor
class Product {
   private String name;
   private boolean active;

}

@Data
class ProductDto {
   private String name;
   private boolean active;
}