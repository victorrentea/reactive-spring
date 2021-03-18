package victor.training.reactor.complex;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.With;

@Getter
@ToString
@AllArgsConstructor
public class Product {
   private final Long id;
   private final String name;
   private final boolean active;
   private final boolean resealed;
   @With
   private final ProductRating rating;

   public Product(Long id, String name, boolean active, boolean resealed) {
      this.id = id;
      this.name = name;
      this.active = active;
      this.resealed = resealed;
      this.rating = null;
   }
}

//class PRoductWithLargeRating {
//   private final Product product;
//   private final rati1, rat2
//}