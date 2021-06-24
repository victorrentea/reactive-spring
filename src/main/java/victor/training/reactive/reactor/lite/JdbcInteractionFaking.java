package victor.training.reactive.reactor.lite;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.intro.ThreadUtils;
import victor.training.reactive.reactor.lite.Part07Errors.Order;

import java.util.Random;

@Slf4j
public class JdbcInteractionFaking {



   @Transactional
   public Mono<Void> retrieveOrder(int id) {

      return Mono.fromRunnable(() -> {
         log.info("INSERT INTO... " + id);
         ThreadUtils.sleep(10 + new Random().nextInt(100));// blocking call you can't really make unblokcing. (reactive) ~ JDBC, Spring Remoting, CORBA, EJB, RMI,
      })
          .subscribeOn(Schedulers.boundedElastic())
          .then();
   }

//   EntityManager em;
//   private void met(int id) {
//      em.findById()
//       em.save()
//   }
}
