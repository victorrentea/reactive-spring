package victor.training.reactive.intro;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

@Slf4j
@EnableAsync
@SpringBootApplication
@RequiredArgsConstructor
public class StarterApp implements CommandLineRunner {

   public static void main(String[] args) {
      SpringApplication.run(StarterApp.class, args);
   }

   @Bean
   public ThreadPoolTaskExecutor pool( @Value("${barman.count}")int barmanCount) {
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      // WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING
      // WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING
      // WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING
      // WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING WARNING
      executor.setCorePoolSize(barmanCount);
      executor.setMaxPoolSize(barmanCount);
      executor.setQueueCapacity(2);
      executor.setThreadNamePrefix("bar-");
      executor.initialize();
      executor.setWaitForTasksToCompleteOnShutdown(true);
      return executor;
   }

   private final Barman barman;

   @Autowired
   ThreadPoolTaskExecutor pool;

   @Override
   public void run(String... args) throws Exception {
      log.info("Sending orders calls to the barman : " + barman.getClass());
      long t0 = System.currentTimeMillis();

      // TODO make this guy drink earlier

//      CompletableFuture<Void> payFuture = CompletableFuture.runAsync(() -> {
//         log.debug("PAY");
//      });

      CompletableFuture<Beer> futureBeer = CompletableFuture.supplyAsync( barman::pourBeer);
      CompletableFuture<Vodka> futureVodka = CompletableFuture.supplyAsync(barman::pourVodka); // ONLY ALLOWED IF I ONLY DO CPU work !!!
      // NOT REST, DB, FILES, logging ?


//      CompletableFuture<Void> whenBothAreDoneFuture = CompletableFuture.allOf(futureBeer, futureVodka);
//      whenBothAreDoneFuture.thenRun(() -> {
//         log.debug("HALO!");
//      });

      CompletableFuture<DillyDilly> futureDilly = futureBeer.thenCombine(futureVodka, (b, v) -> new DillyDilly(b, v));


      futureDilly.thenAccept(dilly ->  {
         log.debug("Drinking: " + dilly);
      });
//      DillyDilly dilly = new DillyDilly(beer, vodka);
//

      long t1 = System.currentTimeMillis();
      log.debug("Time= " + (t1 - t0));
   }
}

// await / async

@Slf4j
@Service
@RequiredArgsConstructor
class Barman {
   public Beer pourBeer() {
      log.info("Start pour beer");
      ThreadUtils.sleep(1000); // blocking REST call
      log.info("end pour beer");
      return new Beer();
   }

   public Vodka pourVodka() {
      log.info("Start pour vodka");
      ThreadUtils.sleep(1000);  // blocking DB call
      log.info("end pour vodka");
      return new Vodka();
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

