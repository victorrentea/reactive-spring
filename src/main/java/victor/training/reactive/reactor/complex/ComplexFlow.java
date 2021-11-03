package victor.training.reactive.reactor.complex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
public class ComplexFlow {

   public static void main(String[] args) {
      List<Long> productIds = LongStream.rangeClosed(1, 10).boxed().collect(Collectors.toList());

      BlockHound.install();
      Mono<List<Product>> listMono = mainFlow(productIds)
          .flatMap(lista -> {
             return Mono.fromSupplier(() -> {
               System.out.println("Fac ceva side effects (network call): anunt ca am terminat jobul");
               return lista;
             });
          });

      List<Product> products = listMono.block(); // unusual, only here to stop main thread from exiting
//      log.info("Done. Got {} products: {}", products.size(), products);
      log.info("Done. Got products: {}", products);
   }

   private static Mono<List<Product>> mainFlow(List<Long> productIds) {
      List<Product> products = new ArrayList<>();
      for (Long productId : productIds) {
         ProductDetailsResponse response = WebClient.create()
             .get()
             .uri("http://localhost:9999/api/product/"+productId)
             .retrieve()
             .bodyToMono(ProductDetailsResponse.class)
             .block();

         products.add(response.toEntity());
      }
      if (true) return Mono.empty();
      return Mono.just(products);
   }

}

