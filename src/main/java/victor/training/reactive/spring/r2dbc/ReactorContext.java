package victor.training.reactive.spring.r2dbc;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Files;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class ReactorContext {


   private static Scheduler dbElasticScheduler = Schedulers.newBoundedElastic(100, 500, "DB");

   public static void main(String[] args) {


//      Flux.fromIterable(Files.lines(path))
//         .publishOn(Schedulers.parallel())
//         .map(cpuWork());
//         .flatMap(httpRetrieveMono())
//         .publishOn(Schedulers.sixSizedPool())
//          .buffer(500)
//          .flatMap(persitToDBBlockingJDBC) //500 inserts batched together
//          .then(sendEmailMono())
//         .publish();

      String tenantId = "US";
      doStuff()
          // subscriber does:
          .contextWrite(context -> context.put("tenantId", tenantId))
          .block();
   }

   private static Mono<Object> doStuff() {
      return deeper()
//          .flatMap(callSomeAsync, 100)
          .contextWrite(c -> c.put("tenantId", "EU"));
   }

   private static Mono<Object> deeper() {
      return Mono.deferContextual(context -> {
         String tenantId = context.get("tenantId");
         log.debug("INSERT CREATED_BY=" + tenantId);
         return Mono.empty();
      }).subscribeOn(dbElasticScheduler);
   }

   public Mono<String> reactiveAccessToBlockingCode() {
      return Mono.fromSupplier(() -> blockingTransactionalMethod())
          .subscribeOn(Schedulers.boundedElastic());
   }
   
   @Transactional//(transactionManager = "rxTx")
   public String blockingTransactionalMethod() {
      //
      // NO REACTOR inside
      // Hibernate, JDbcTemplate, MyBatis
//      new RestTemplate().getForObject()
      return "from DB";
   }
}
