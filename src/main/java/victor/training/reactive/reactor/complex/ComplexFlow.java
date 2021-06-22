package victor.training.reactive.reactor.complex;

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
      List<Long> productIds = LongStream.rangeClosed(1, 10).boxed().collect(Collectors.toList());

      Mono<List<Product>> listMono = mainFlow(productIds);

      List<Product> products = listMono.block(); // unusual, only here to stop main thread from exiting
      log.info("Done. Got {} products: {}", products.size(), products);
   }


   private static Mono<List<Product>> mainFlow(List<Long> productIds) {
      List<Product> products = new ArrayList<>();
      for (Long productId : productIds) {
         RestTemplate rest = new RestTemplate();
         ProductDetailsResponse dto = rest.getForObject("http://localhost:9999/api/product/", ProductDetailsResponse.class, productId);
         products.add(dto.toEntity());
      }
      return Mono.empty();
   }

}

