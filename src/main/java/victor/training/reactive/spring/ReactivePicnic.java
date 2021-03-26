//package victor.training.reactive.spring;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.mapping.Document;
//import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
//import org.springframework.data.repository.reactive.ReactiveCrudRepository;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.core.scheduler.Schedulers;
//import victor.training.reactive.reactor.complex.Product;
//import victor.training.reactive.reactor.complex.ProductDetailsResponse;
//
//import java.util.List;
//
//
//@Data
//@Document
//class ProductDescription {
//   @Id
//   private Long productId;
//   private String description;
//}
//interface ProductDescriptionRepo extends ReactiveMongoRepository<ProductDescription, Long> {
//
//}
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//class ProductRelational {
//   @Id
//   private Long id;
//   private String name;
//   private String description;
//}
//
//interface ProductRelationalRepo extends ReactiveCrudRepository<ProductRelational, Long> {
//
//}
//
//
//@RestController
//public class ReactivePicnic {
//
//   private final ProductDescriptionRepo productDescriptionRepo;
//   private final ProductRelationalRepo productRelationalRepo;
//
//   private static final Logger log = LoggerFactory.getLogger(ReactivePicnic.class);
//
//   public ReactivePicnic(ProductDescriptionRepo productDescriptionRepo, ProductRelationalRepo productRelationalRepo) {
//      this.productDescriptionRepo = productDescriptionRepo;
//      this.productRelationalRepo = productRelationalRepo;
//   }
//   // in: List<Long>. For each, call the getProductDetails, load from mongo more data, then store in postgres
//
//   @GetMapping("picnic")
//   public Mono<Void> method(@RequestParam List<Long> ids) {
//      return Flux.fromIterable(ids)
//          .flatMap(id -> fetchSingleProductDetails(id), 10)
//          .flatMap(product -> productDescriptionRepo.findById(product.getId())
//              .map(description -> product.withDescription(description.getDescription())))
//          .map(product -> new ProductRelational(product.getId(), product.getName(), product.getDescription()))
//          .flatMap(r -> productRelationalRepo.save(r))
//          .then();
//   }
//
//   public static Mono<Product> fetchSingleProductDetails(Long productId) {
//      log.info("Calling Get Product Details REST");
//      return WebClient.create().get().uri("http://localhost:9999/api/product/{}", productId)
//          .retrieve()
//          .bodyToMono(ProductDetailsResponse.class)
//          .map(ProductDetailsResponse::toEntity)
//          .subscribeOn(Schedulers.boundedElastic());
//   }
//
//}
