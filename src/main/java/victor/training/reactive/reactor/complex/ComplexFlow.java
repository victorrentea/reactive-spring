package victor.training.reactive.reactor.complex;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

@Slf4j
public class ComplexFlow {

   public static final Scheduler FR_GOV = Schedulers.newBoundedElastic(2, 100, "fr.gov");

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
          .buffer(2)
//          .publishOn(Schedulers.boundedElastic())
          .flatMap(idPage -> fetchSingleProductDetails(idPage), 4)
          .doOnNext(p -> log.info("Product on main flow"));
   }

   @SneakyThrows
   public static Flux<Product> fetchSingleProductDetails(List<Long> productIds) {

      return Flux.defer(() -> {
         log.info("Calling Get Product Details REST");
         RestTemplate rest = new RestTemplate();
         ProductDetailsResponse[] dtos = rest.postForObject("http://localhost:9999/api/product/many", productIds, ProductDetailsResponse[].class);
         System.out.println(Arrays.toString(dtos));

         return Flux.fromArray(dtos).map(ProductDetailsResponse::toEntity);
      })
          .subscribeOn(FR_GOV)
          .publishOn(Schedulers.boundedElastic())
          ;


   }



}

