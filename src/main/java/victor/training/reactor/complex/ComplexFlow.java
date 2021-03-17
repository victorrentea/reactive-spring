package victor.training.reactor.complex;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import victor.training.reactivespring.start.ThreadUtils;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
public class ComplexFlow {
   public static void main(String[] args) {
      List<Long> productIds = LongStream.range(1, 10).boxed().collect(Collectors.toList());
      Mono<List<Product>> listMono = mainFlow(productIds);
      List<Product> products = listMono.block(); // unusual, only here to stop main thread from exiting
      log.info("Done: " + products);
   }


   private static Mono<List<Product>> mainFlow(List<Long> productIds) {
      return Flux.fromIterable(productIds)
          .log()
//          .publishOn(Schedulers.boundedElastic())
          .flatMap(id -> getSingleProductDetails(id))
          .collectList();
   }


   @SneakyThrows
   public static Mono<Product> getSingleProductDetails(Long productId) {
      return Mono.fromSupplier(() -> {
         log.info("Calling REST");
         RestTemplate rest = new RestTemplate();
         ProductDto dto = rest.getForObject("http://localhost:9999/api/product/1", ProductDto.class);
         return dto.toEntity();
      }).subscribeOn(Schedulers.boundedElastic());
   }
}


