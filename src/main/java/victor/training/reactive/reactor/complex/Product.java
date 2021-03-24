package victor.training.reactive.reactor.complex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;

@Getter
@AllArgsConstructor
public class Product {
   private final Long id;
   private final String name;
   private final boolean active;
   private final boolean resealed;
   @With
   private ProductRatingDto rating;


   public Product(Long id, String name, boolean active, boolean resealed) {
      this.id = id;
      this.name = name;
      this.active = active;
      this.resealed = resealed;
   }
}
