package victor.training.reactive.reactor.complex;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
public class ComplexFlow {

   public static void main(String[] args) {
      List<Long> productIds = LongStream.range(1, 10).boxed().collect(Collectors.toList());

      Mono<List<Product>> listMono = mainFlow(productIds);

      List<Product> products = listMono.block(); // unusual, only here to stop main thread from exiting
      log.info("Done. Got {} products: {}", products.size(), products);
   }


   private static Mono<List<Product>> mainFlow(List<Long> productIds) {
      List<Product> result = new ArrayList<>();
      for (Long productId : productIds) {
         Product product = getProductById(productId);
         result.add(product);
      }

      return Mono.just(result);
   }

   @SneakyThrows
   public static Product getProductById(Long productId) {
      log.info("Calling Get Product Details REST");
      RestTemplate rest = new RestTemplate();
      ProductDetailsResponse dto = rest.getForObject("http://localhost:9999/api/product/{id}", ProductDetailsResponse.class, productId);
      return dto.toEntity();
      // TODO 1 use a non-blockin REST client
      // TODO 2 is it ok to call them N times over network ?
      //....
   }

}

