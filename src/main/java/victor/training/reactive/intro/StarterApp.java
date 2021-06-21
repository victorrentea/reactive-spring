package victor.training.reactive.intro;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@EnableAsync
@SpringBootApplication
@RequiredArgsConstructor
@RestController
public class StarterApp  {

   public static void main(String[] args) {
      SpringApplication.run(StarterApp.class, args);
   }

   @Bean
   public ThreadPoolTaskExecutor vodkaPool( @Value("${vodka.count:1}")int count) {
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
   public ThreadPoolTaskExecutor beerPool( @Value("${beer.count:2}")int count) {
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(count);
      executor.setMaxPoolSize(count);
      executor.setQueueCapacity(2);
      executor.setThreadNamePrefix("beer-");
      executor.initialize();
      executor.setWaitForTasksToCompleteOnShutdown(true);
      return executor;
   }

   private final Barman barman;


   @GetMapping("drink")
   public CompletableFuture<DillyDilly> run() throws ExecutionException, InterruptedException {
      try {
         log.info("Sending orders calls to the barman : " + barman.getClass());
         long t0 = System.currentTimeMillis();

         // TODO make this guy drink earlier

         CompletableFuture<Beer> futureBeer = barman.pourBeer();
         CompletableFuture<Vodka> futureVodka =barman.pourVodka(); // ONLY ALLOWED IF I ONLY DO CPU work !!!
         // NOT REST, DB, FILES, logging ?

         CompletableFuture<DillyDilly> futureDilly = futureBeer.thenCombine(futureVodka, (b, v) -> new DillyDilly(b, v));

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
   @Async("beerPool")
//   public void pourBeer() { // Mono<Void>
//   public CompletableFuture<Beer> pourBeer() { // Mono<Beer>
   public CompletableFuture<Beer> pourBeer() {
//      return CompletableFuture.supplyAsync(() -> {
         log.info("Start pour beer");
         ThreadUtils.sleep(1000); // blocking REST call
         log.info("end pour beer");
      return CompletableFuture.completedFuture(new Beer());
//      });
   }

   @Async("vodkaPool")
   public CompletableFuture<Vodka> pourVodka() {
      log.info("Start pour vodka");
      ThreadUtils.sleep(1000);  // blocking DB call
      log.info("end pour vodka");
      return CompletableFuture.completedFuture(new Vodka());
   }
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

