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

   // TODO make tests happy
   public void processOrderingCustomer(Flux<Integer> customerIdFlux) {
   }

}
