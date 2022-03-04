package victor.training.reactive.reactor.complex;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
class ExternalAPIs {

   @SneakyThrows
   public static Product fetchSingleProductDetails(Long productId) {
      log.info("Calling Get Product Details REST");
      RestTemplate rest = new RestTemplate();
      ProductDetailsResponse dto = rest.getForObject("http://localhost:9999/api/product/", ProductDetailsResponse.class, productId);
      return dto.toEntity();
   }

   @SneakyThrows
   // because the method took time to complete> return MOno
   // because it does not give me anything back > returns a Mono<Void>
   public static Mono<Void> auditResealedProduct(Product product) {
      return WebClient.create().get()
          .uri("http://localhost:9999/apiXX/audit-resealed/" + product)
          .retrieve()
          .toBodilessEntity()
          .then()
      ;
//          .doOnSubscribe(s -> log.info("Calling Audit REST"))
//          .then();
   }















   @SneakyThrows
   public static Mono<ProductRatingResponse> fetchProductRating(long productId) {
      return WebClient.create().get().uri("http://localhost:9999/api/rating/{}", productId)
          .retrieve()
          .bodyToMono(ProductRatingResponse.class)
          .doOnSubscribe(s -> log.info("Calling Rating REST"))
          ;
   }


}
