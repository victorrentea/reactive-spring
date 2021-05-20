package victor.training.reactive.reactor.complex;

import lombok.Data;
import lombok.NonNull;
import lombok.With;

@Data
public class Product {
   @NonNull
   private final Long id;
   @NonNull
   private final String name;
   private final boolean active;
   private final boolean resealed;
   @With
   @NonNull
   private final ProductRatingResponse rating;

}

// 3 options to augment data
// NEVER SETTERS
// 1) immutable obj with null fields / NULL-OBJECT-PATTERN
// 2) ProductWithRating
// 3) Tuple2<Product,Integer>  << AVOID === lack of abstration. Find more concepts. Create more clases
