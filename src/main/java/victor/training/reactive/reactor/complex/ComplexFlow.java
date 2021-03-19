package victor.training.reactive.reactor.complex;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

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
      return Mono.empty();
   }

}

