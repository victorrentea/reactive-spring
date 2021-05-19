package victor.training.reactive.reactor.complex;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@Slf4j
public class ComplexFlow {

   public static void main(String[] args) {
      List<Long> productIds = LongStream.range(0, 100).boxed().collect(Collectors.toList());

      log.info("START");
      Flux<Product> listMono = mainFlow(productIds);
      log.info("I got the mono");
      List<Product> products = listMono.collectList().block();
      log.info("Done. Got {} products: {}", products.size(), products);
   }


   // in a reactive app, when you see a func return a Publisher, you expect to block ONLY when you subscribe
   private static Flux<Product> mainFlow(List<Long> productIds) {
      return Flux.fromIterable(productIds)
          .flatMap(ComplexFlow::fetchSingleProductDetails, 4);
   }

   @SneakyThrows
   public static Mono<Product> fetchSingleProductDetails(Long productId) {
      return Mono.defer(() -> {
         log.info("Calling Get Product Details REST");
         RestTemplate rest = new RestTemplate();
         ProductDetailsResponse dto = rest.getForObject("http://localhost:9999/api/product/", ProductDetailsResponse.class, productId);
         return Mono.just(dto.toEntity());
      })
          .subscribeOn(Schedulers.boundedElastic());
   }

}

