package victor.training.reactive.intro;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@EnableAsync
@SpringBootApplication
@RequiredArgsConstructor
@RestController
public class StarterApp {

   private final Barman barman;

   public static void main(String[] args) {
      SpringApplication.run(StarterApp.class, args);
   }

   @Bean
   public ThreadPoolTaskExecutor vodkaPool(@Value("${vodka.count:1}") int count) {
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      // WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING
      // WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING
      // WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING
      // WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING
      executor.setCorePoolSize(count);
      executor.setMaxPoolSize(count);
      executor.setQueueCapacity(2);
      executor.setThreadNamePrefix("vodka-");
      executor.initialize();
      executor.setWaitForTasksToCompleteOnShutdown(true);
      return executor;
   }

   @Bean
   public ThreadPoolTaskExecutor beerPool(@Value("${beer.count:2}") int count) {
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(count);
      executor.setMaxPoolSize(count);
      executor.setQueueCapacity(2);
      executor.setThreadNamePrefix("beer-");
      executor.initialize();
      executor.setWaitForTasksToCompleteOnShutdown(true);
      return executor;
   }

   @GetMapping("drink")
   public Mono<DillyDilly> run() throws ExecutionException, InterruptedException {
//      HttpServletRequest.startAsync();
      try {
         log.info("Sending orders calls to the barman : " + barman.getClass());
         long t0 = System.currentTimeMillis();

         // TODO make this guy drink earlier


//         CompletableFuture<Void>

         Mono<Beer> futureBeer = barman.pourBeer();
         Mono<Vodka> futureVodka = barman.pourVodka(); // ONLY ALLOWED IF I ONLY DO CPU work !!!
         // NOT REST, DB, FILES, logging ?

         Mono<Tuple3<Beer, Beer, Vodka>> zip = Mono.zip(futureBeer, futureBeer, futureVodka);
//         Tuple2<String, Map<Long, Tuple3<String, LocalDateTime, String>>>

         Mono<DillyDilly> futureDilly = futureBeer.zipWith(futureVodka, (b, v) -> new DillyDilly(b, v))
//             .subscribeOn(Schedulers.single())
             ;

         return futureDilly;
      } finally {
         log.debug("http worker thread returns to the pool");
      }
   }
}

// await / async

@Slf4j
@Service
@RequiredArgsConstructor
class Barman {
   public Mono<Beer> pourBeer() {// IF a function declares to return Mono or Flux, it must NEVER BLOCK and it should never THROW\
      return Mono.fromSupplier(() -> {
         log.info("Start pour beer");
         ThreadUtils.sleep(1000); // blocking REST call
         log.info("end pour beer");
         return new Beer();
      })
          .subscribeOn(Schedulers.boundedElastic()); //~ pool.submit()
   }

   public Mono<Vodka> pourVodka() {
      return Mono.fromSupplier(() -> {
         log.info("Start pour vodka");
         ThreadUtils.sleep(1000);  // blocking DB call

         log.info("end pour vodka");
         return new Vodka();
      }).subscribeOn(Schedulers.boundedElastic()); //~ pool.submit()
   }

//   private Mono<Void> someMethod() {
////      WebClient
////      Mono<Data>
//      ThreadUtils.sleep(1000);  // blocking DB call
//   }
}


@Data
class Beer {
   private final String type = "blond";
}

@Data
class Vodka {
   private final String type = "deadly";

}

// Vinul dupa Bere e Placere
// Berea dupa Vin  e un Chin
@Slf4j
@Data
class DillyDilly {
   private final Beer beer;
   private final Vodka vodka;

   public DillyDilly(Beer beer, Vodka vodka) {
      this.beer = beer;
      this.vodka = vodka;
      log.debug("Mixing {} with {}...", beer, vodka);
      ThreadUtils.sleep(1000);
   }
}

