package victor.training.reactive.reactor.exercises.customeraudit;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Slf4j
@Data
@Service
public class CustomerAuditService {

   private final EmailSender emailSender;
   private final CustomerReactiveRepo repo;
   @Autowired
   private String baseUrl;

   public void processOrderingCustomer(Flux<Integer> customerIdFlux) {
      customerIdFlux
          .flatMap(id -> repo.findById(id))
          .groupBy(c -> c.isExternal())
          .flatMap(group -> {
             if (group.key()) {
                // external
                return group.buffer(2).flatMap(buffer -> {
                   String ids = buffer.stream().map(Customer::getId).map(Objects::toString).collect(joining(","));
                   log.info("GET REST " + ids);
                   return WebClient.create().get().uri(baseUrl + "/external-customer/" + ids)
                           .retrieve().bodyToFlux(Customer.class);
                    }
                );
             } else {
                return group;
             }
          })
          .flatMap(c -> send(c))
          .subscribe();
   }

   private Mono<Void> send(Customer c) {
      log.info("SEND customer " + c);
      if (c.isActive()) return WebClient.create().get().uri(baseUrl + "/customer-audit/{id}",c.getId()).retrieve().toBodilessEntity().log().then();
      return emailSender.sendInactiveCustomerEmail(c);
   }


}
