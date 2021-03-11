package victor.training.reactor.complex;

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

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Slf4j
public class ComplexFlow {

   public static void main(String[] args) {

      log.info("Start");
// Mono si Flux sunt Publisheri

//      List<Long> productIds = Arrays.asList(1L,2L);
      List<Long> productIds = LongStream.range(1, 10).boxed().collect(Collectors.toList());

      Mono<List<Product>> listMono = mainFlow(productIds);

      listMono
          .doOnNext(list -> log.info("Peeking : " + list.size()))
          .subscribe(list -> log.info("Result: " + list))
      ;


      log.info("Gata");
      ThreadUtils.sleep(30000);
   }


   // De ex: daca vii cu 10K de id-uri, faci total 50 de requesturi HTTP fiecare carand dus-intors 200 id-uri/date ;
   // in plus, niciodata nu vei avea mai mult de 10 requesturi simultane deschise cu API-ul lor, datorita  #productApiCallScheduler


   private static Mono<List<Product>> mainFlow(List<Long> productIds) {

      return
          Flux.fromIterable(productIds)
              .buffer(2)
              .flatMap(ComplexFlow::convertBlockingToReactive)

              .flatMap(product -> {
                 Mono<Void> auditMono = ExternalAPIs.auditResealedProduct(product);
                 return auditMono.thenReturn(product);
              })

//           .flatMap(product -> ExternalAPIs.getRating(product.getId())
//                                           .map(rating -> product.withRating(rating)))
              .flatMap(product -> ExternalCacheClient.lookupInCache(product.getId())  // se comporta ca un Optional asincron
                  .switchIfEmpty(ExternalAPIs.getRating(product.getId())) // ne inchipuim e mai expensive x 3 ori
                  .map(rating -> product.withRating(rating)))

              .doOnNext(product -> {
                 log.debug("Put" + product.getId());
                 Mono.defer(() -> ExternalCacheClient.putInCache(product.getId(), product.getRating()))
                     .subscribeOn(Schedulers.boundedElastic())
                     .subscribe();
//                     .subscribe(v->{}, e -> {
//                        e.printStackTrace();
//                     });
              })

              .collectList()
          ;


//      Flux.interval(Duration.ofMillis(100))
//          .back
   }

   public static Flux<Product> convertBlockingToReactive(List<Long> productIds) {
      // TODO cum ii dau cu 10 threaduri nu cu 120 = 10 x #CPU
      return Flux.defer(() -> ExternalAPIs.getManyProductDetails(productIds))
          .subscribeOn(productApiCallScheduler)
          ;
   }

   public static final Scheduler productApiCallScheduler = Schedulers.newBoundedElastic(10, 10_000, "product-api-call");

}

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
      // ne inchipuim apel REST / WS
      log.info("Calling REST");

//      RestTemplate rest = new RestTemplate();
//      ProductDto dto = rest.getForObject("http://localhost:9999/api/product/1", ProductDto.class);
//      return Mono.just(new Product(dto.getName(), dto.isActive()));
//
      return WebClient.create().get().uri("http://localhost:9999/api/product/1")
          .retrieve()
          .bodyToMono(ProductDto.class)
          .map(dto -> dto.toEntity())
          ;
   }

   @SneakyThrows
   public static Flux<Product> getManyProductDetails(List<Long> productIds) {
      log.info("Calling REST");
      return WebClient.create().post().uri("http://localhost:9999/api/product/many").body(Mono.just(productIds), new ParameterizedTypeReference<List<Long>>() {
      })
          .retrieve()
          .bodyToFlux(ProductDto.class)
          .map(dto -> dto.toEntity())
          ;
   }


   @SneakyThrows
   public static Mono<ProductRating> getRating(long productId) {
      log.info("Calling get Rating Service REST");
      return WebClient.create().get().uri("http://localhost:9999/api/rating/" + productId)
          .retrieve()
          .bodyToMono(ProductRating.class);

   }

}

@Data
class Product {
   private Long id;
   private String name;
   private boolean active;
   private boolean resealed;
   private ProductRating rating;

   public Product(Long id, String name, boolean active, boolean resealed) {
      this.id = id;
      this.name = name;
      this.active = active;
      this.resealed = resealed;
   }

   public Product(Long id, String name, boolean active, boolean resealed, ProductRating rating) {
      this.id = id;
      this.name = name;
      this.active = active;
      this.resealed = resealed;
      this.rating = rating;
   }

   public Product withRating(ProductRating rating) {
      return new Product(id, name, active, resealed, rating);
   }
}

@Data
class ProductDto {
   private Long id;
   private String name;
   private boolean active;
   private boolean resealed;


   public Product toEntity() {
      return new Product(this.id, getName(), isActive(), isResealed());
   }
}