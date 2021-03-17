package victor.training.async;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import victor.training.reactivespring.start.ThreadUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@EnableAsync
@SpringBootApplication
@RequiredArgsConstructor
public class StarterApp implements CommandLineRunner {

   private ExecutorService pool = Executors.newFixedThreadPool(2);

   public static void main(String[] args) {
      SpringApplication.run(StarterApp.class, args);
   }
   @Bean
      public ThreadPoolTaskExecutor beerPool(@org.springframework.beans.factory.annotation.Value("${beer.thread.count:1}") int threadCount) {
         ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
         executor.setCorePoolSize(threadCount);
         executor.setMaxPoolSize(threadCount);
         executor.setQueueCapacity(500);
         executor.setThreadNamePrefix("beer-");
         executor.initialize();
         executor.setWaitForTasksToCompleteOnShutdown(true);
         return executor;
   }
   @Bean
      public ThreadPoolTaskExecutor vodkaPool(@org.springframework.beans.factory.annotation.Value("${vodka.thread.count:4}") int threadCount) {
         ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
         executor.setCorePoolSize(threadCount);
         executor.setMaxPoolSize(threadCount);
         executor.setQueueCapacity(500);
         executor.setThreadNamePrefix("vodka-");
         executor.initialize();
         executor.setWaitForTasksToCompleteOnShutdown(true);
         return executor;
   }

   private final Barman barman;


   @Override
   public void run(String... args) throws Exception {
      log.info("Sending method calls to the barman : " + barman.getClass());
      long t0 = System.currentTimeMillis();
      //
      // here there is just main
      CompletableFuture<Beer> futureBeer = barman.pourBeer();
      CompletableFuture<Vodka> futureVodka = barman.pourVodka();

      // Schedulers.parallel(); // only use for CPU work: bitcoin minign, asymm encryption, generating PDF (binary formats), parsing/marhsalling JSON because it allocates memory
      // IO work : Archive/compress,


      CompletableFuture<DillyDilly> futureDilly = futureBeer
          .thenCombine(futureVodka, DillyDilly::new);

      futureDilly.thenAccept(dilly -> {
         log.debug("My drinks: " + dilly);
      });
      long t1 = System.currentTimeMillis();
      log.debug("Got my drinks " + (t1 - t0));
   }
}

// Vinul dupa Bere e Placere
// Bere dupa Vin  e un Chin
@Value
class DillyDilly {
   Beer beer;
   Vodka vodka;
}


@Slf4j
@Service
@RequiredArgsConstructor
class Barman {
   @Async("beerPool")
   public CompletableFuture<Beer> pourBeer() {
      log.info("Start pour beer");
      ThreadUtils.sleep(1000); // blocking REST call
      log.info("end pour beer");
      return CompletableFuture.completedFuture(new Beer());
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