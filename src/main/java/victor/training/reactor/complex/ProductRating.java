package victor.training.reactor.complex;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRating {
   public static final ProductRating NONE = new ProductRating();
   private int rating;
}

