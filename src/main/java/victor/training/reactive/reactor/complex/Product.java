package victor.training.reactive.reactor.complex;

import lombok.Data;
import lombok.Value;
import lombok.With;

@Value
public class Product {
   Long id;
   String name;
   boolean active;
   boolean resealed;
   @With
   ProductRatingResponse rating;
}


@Value
class ProductWithRating {
   Product product;
   ProductRatingResponse rating;
}