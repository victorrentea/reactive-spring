package victor.training.reactivespring.r2dbc;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
@Data
public class User {
   @Id
   private Integer id;
   private String name;

   public User(String name) {
      this.name = name;
   }
}