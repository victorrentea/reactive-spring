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
import reactor.core.publisher.Mono;

import java.util.concurrent.*;

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
   public ThreadPoolTaskExecutor beerPool(@Value("${beer.count:1}")int corePoolSize) {
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(corePoolSize);
      executor.setMaxPoolSize(corePoolSize);
      executor.setQueueCapacity(500);
      executor.setThreadNamePrefix("beer-");
      executor.initialize();
      executor.setWaitForTasksToCompleteOnShutdown(true);
      return executor;
   }

   @Bean
   public ThreadPoolTaskExecutor vodkaPool(@Value("${vodka.count:4}")int corePoolSize) {
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(corePoolSize);
      executor.setMaxPoolSize(corePoolSize);
      executor.setQueueCapacity(500);
//      executor.setRejectedExecutionHandler(new CallerRunsPolicy());
      executor.setThreadNamePrefix("vodka-");
      executor.initialize();
      executor.setWaitForTasksToCompleteOnShutdown(true);
      return executor;
   }

   private final Barman barman;

   @Autowired
   ThreadPoolTaskExecutor beerPool;
   @Autowired
   ThreadPoolTaskExecutor vodkaPool;


   @GetMapping("dilly")
   public CompletableFuture<DillyDilly> run() throws ExecutionException, InterruptedException {

      log.info("Sending orders calls to the barman : " + barman.getClass());
      long t0 = System.currentTimeMillis();

      // TODO CF plain Java
      // TODO controlling pool sizes
      // TODO Exceptions

      CompletableFuture<Beer> futureBeer = CompletableFuture
          .supplyAsync(() -> barman.pourBlondBeer(), beerPool)
          .exceptionally(e -> {
             if (e.getCause() instanceof IllegalStateException) {
                return barman.pourDarkBeer();
             }
             throw new RuntimeException(e);
          });

      CompletableFuture<Vodka> futureVodka = CompletableFuture.supplyAsync(() -> barman.pourVodka(), vodkaPool);
      CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(futureBeer, futureVodka);

      log.debug("Now, two workers are pouring drinks for me in ||");

      barman.curse("&!^@&#^@!&$^!*@$**%(!");


      CompletableFuture<DillyDilly> futureDilly = futureBeer.thenCombineAsync(futureVodka, DillyDilly::new);
      long t1 = System.currentTimeMillis();
      log.debug("Time= " + (t1 - t0));
      return futureDilly;
   }

   // later in spring
//   {
//      CompletableFuture<DillyDilly> dillyCF;
//
//      dillyCF.thenAccept(dilly -> {
//
//      })
//   }
}

@Slf4j
@Service
@RequiredArgsConstructor
class Barman {
//   @Async("beerPool")
   public Beer pourBlondBeer() {
      log.info("Start pour beer");
      if (true) {
         throw new IllegalStateException("Out of beer");
      }
      ThreadUtils.sleep(1000); // blocking REST call
      log.info("end pour beer");
      return new Beer();
   }
   public Beer pourDarkBeer() {
      log.info("Start pour dark beer");
      ThreadUtils.sleep(1000); // blocking REST call
      log.info("end pour beer");
      return new Beer();
   }

//   @Async("vodkaPool")
   public Vodka pourVodka() {
      log.info("Start pour vodka");
      ThreadUtils.sleep(1000);  // blocking DB call
      log.info("end pour vodka");
      return new Vodka();
   }

   @Async
   public void curse(String curse) {
      if (curse != null) {
         throw new RuntimeException("Bring the boys");
      }

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

