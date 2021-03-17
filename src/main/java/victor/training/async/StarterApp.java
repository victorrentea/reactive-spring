package victor.training.async;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import victor.training.reactivespring.start.ThreadUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class StarterApp implements CommandLineRunner {
   public static void main(String[] args) {
      SpringApplication.run(StarterApp.class, args);
   }

   private final Barman barman;

   @Override
   public void run(String... args) throws Exception {

      long t0 = System.currentTimeMillis();
      // here there is just main
      CompletableFuture<Beer> futureBeer = CompletableFuture.supplyAsync(barman::pourBeer);
      CompletableFuture<Vodka> futureVodka = CompletableFuture.supplyAsync(barman::pourVodka);


      CompletableFuture<DillyDilly> futureDilly = futureBeer.thenCombine(futureVodka, DillyDilly::new);

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
   public Beer pourBeer() {
      log.info("Start pour beer");
      ThreadUtils.sleep(1000);
      log.info("end pour beer");
      return new Beer();
   }

   public Vodka pourVodka() {
      log.info("Start pour vodka");
      ThreadUtils.sleep(1000);
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