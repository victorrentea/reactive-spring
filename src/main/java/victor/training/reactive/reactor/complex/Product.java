package victor.training.reactive.reactor.complex;

import lombok.Data;

@Data
public class Product {
   private Long id;
   private String name;
   private boolean active;
   private boolean resealed;
   private ProductRatingResponse rating;
   private String description;

   public Product withDescription(String description) {
      this.description = description;
      return this;
   }
}
