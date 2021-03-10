package victor.training.reactor.complex;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import victor.training.reactivespring.start.ThreadUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
public class ComplexFlow {

   public static void main(String[] args) {

      log.info("Start");
// Mono si Flux sunt Publisheri

//      List<Long> productIds = Arrays.asList(1L,2L);
      List<Long> productIds = LongStream.range(1,10).boxed().collect(Collectors.toList());

      Mono<List<Product>> listMono = mainFlow(productIds);

      listMono
          .doOnNext(list -> log.info("Peeking : " + list))
          .subscribe(list -> log.info("Result: " + list))
      ;


      log.info("Gata");
      ThreadUtils.sleep(30000);
   }

   private static Mono<List<Product>> mainFlow(List<Long> productIds) {
      return Flux.fromIterable(productIds)

          .buffer(2)
          .flatMap(productId -> convertBlockingToReactive(productId))
         .collectList()
         ;
   }
   public static Flux<Product> convertBlockingToReactive(List<Long> productIds) {
      // TODO cum ii dau cu 10 threaduri nu cu 120 = 10 x #CPU
      return Flux.defer(() -> ExternalAPI.getManyProductDetails(productIds))
          .subscribeOn(productApiCallScheduler)
          ;
   }

   public static final Scheduler productApiCallScheduler = Schedulers.newBoundedElastic(10, 10_000, "product-api-call");

}
@Slf4j
class ExternalAPI {
   @SneakyThrows
   public static Mono<Product> getProductDetails(Long productId) {
      // ne inchipuim apel REST / WS
      log.info("Calling REST");

//      RestTemplate rest = new RestTemplate();
//      ProductDto dto = rest.getForObject("http://localhost:9999/api/product/1", ProductDto.class);
//      return Mono.just(new Product(dto.getName(), dto.isActive()));
//
      return WebClient.create().get().uri("http://localhost:9999/api/product/1")
          .retrieve()
          .bodyToMono(ProductDto.class)
         .map(dto -> new Product(dto.getName(), dto.isActive()))
      ;
   }
   @SneakyThrows
   public static Flux<Product> getManyProductDetails(List<Long> productIds) {
      log.info("Calling REST");
      return WebClient.create().post().uri("http://localhost:9999/api/product/many").body(Mono.just(productIds), new ParameterizedTypeReference<List<Long>>() {})
          .retrieve()
          .bodyToFlux(ProductDto.class)
            .map(dto -> new Product(dto.getName(), dto.isActive()))
      ;
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