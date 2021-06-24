package victor.training.reactive.reactor.exercises.customeraudit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Customer {
   private Integer id;
   private boolean active;
   private boolean external;

   public Customer(Integer id, boolean active) {
      this.id = id;
      this.active = active;
   }
}
