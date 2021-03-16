package victor.training.reactor.complex;

import lombok.Data;

@Data
public class Product {
   private Long id;
   private String name;
   private boolean active;
   private boolean resealed;
   private ProductRating rating;
}
