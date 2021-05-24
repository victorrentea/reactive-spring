package victor.training.reactive.reactor.exercises.customeraudit;

import reactor.core.publisher.Mono;

public interface EmailSender {
   Mono<Void> sendInactiveCustomerEmail(Customer customer);
}
