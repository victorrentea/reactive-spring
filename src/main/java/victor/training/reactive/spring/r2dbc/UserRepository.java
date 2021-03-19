
package victor.training.reactive.spring.r2dbc;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserRepository extends ReactiveCrudRepository<User, String> {

}
