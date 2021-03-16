package victor.training.reactor.complex;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
class ExternalAPIs {

   @SneakyThrows
   public static Mono<Void> auditResealedProduct(Product product) {
      if (!product.isResealed()) {
         return Mono.empty();
      }
      log.info("Calling Audit REST");
      return WebClient.create().get().uri("http://localhost:9999/api/audit-resealed/" + product)
          .retrieve()
          .toBodilessEntity()
          .then();

   }

   @SneakyThrows
   public static Mono<Product> getSingleProductDetails(Long productId) {
      log.info("Calling REST");
      RestTemplate rest = new RestTemplate();
      ProductDto dto = rest.getForObject("http://localhost:9999/api/product/1", ProductDto.class);
      return Mono.just(dto.toEntity());
   }


}
