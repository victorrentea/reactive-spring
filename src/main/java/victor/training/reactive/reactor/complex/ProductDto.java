package victor.training.reactive.reactor.complex;

import lombok.Data;

@Data
public class ProductDto {
   private Long id;
   private String name;
   private boolean active;
   private boolean resealed;


   public Product toEntity() {
      Product product = new Product(id, name, active, resealed);
      return product;
   }
}
