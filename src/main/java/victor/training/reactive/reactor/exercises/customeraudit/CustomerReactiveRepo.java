package victor.training.reactive.reactor.exercises.customeraudit;

import reactor.core.publisher.Mono;

public interface CustomerReactiveRepo {
   Mono<Customer> findById(Integer id);
}
