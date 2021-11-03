package victor.training.reactive.reactor.pitfalls;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import victor.training.reactive.reactor.exercises.customeraudit.Customer;

@Slf4j
public class MissingSignals {

   class MySqlRepo {
      public Mono<Customer> findCustomer(int customerId) {
         log.info("Searching customer for id " + customerId);
         if (customerId > 0) {
            return Mono.just(new Customer());
         } else {
            // but 1 day, the ID not found in DB...
            return Mono.empty();
         }
      }
   }
   private MySqlRepo mySqlRepo = new MySqlRepo();

   class CassandraRepo {
      public Mono<Customer> save(Customer customer) {
         return Mono.just(customer); // success
      }
   }
   private CassandraRepo cassandraRepo = new CassandraRepo();

   public static class Redis {
      public Mono<Void> increaseApiUsageCounter(String currentUser) {
         log.info("Increase counters for " + currentUser);
         return Mono.empty();
      }
   }
   private Redis redis = new Redis();

   public Mono<Void> losingSignals(int customerId) {
      String currentUser = "gigi";// from Reactor context
      System.out.println(redis.getClass());
      return mySqlRepo.findCustomer(customerId)
          .flatMap(c -> cassandraRepo.save(c))
          .flatMap(c -> redis.increaseApiUsageCounter(currentUser))
          // also consider doOnNext
          ;
   }


}
