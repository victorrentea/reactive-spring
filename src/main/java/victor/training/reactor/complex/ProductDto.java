package victor.training.reactor.complex;

import lombok.Data;

@Data
public class ProductDto {
   private Long id;
   private String name;
   private boolean active;
   private boolean resealed;


   public Product toEntity() {
      Product product = new Product();
      product.setId(id);
      product.setName(name);
      product.setActive(active);
      product.setResealed(resealed);
      return product;
   }
}
