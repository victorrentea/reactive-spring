package victor.training.async;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import victor.training.reactivespring.start.ThreadUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

//      new Thread(()-> {}).start(); // NEVER
//      ExecutorService pool = Executors.newFixedThreadPool(6);


      // here there is just main
      CompletableFuture<Beer> futureBeer = CompletableFuture.supplyAsync(barman::pourBeer);
      CompletableFuture<Vodka> futureVodka = CompletableFuture.supplyAsync(barman::pourVodka);

      Vodka vodka = futureVodka.get(); // blocking - main thread is wasted for 1 sec.
      Beer beer = futureBeer.get(); // takes 0 seconds because the work is already done.

//      Beer beer = barman.pourBeer();
//
//      Vodka vodka = barman.pourVodka();

      long t1 = System.currentTimeMillis();
      System.out.println("My drinks: " + vodka + " " + beer);
      System.out.println("Got my drinks " + (t1-t0));
   }
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
   private final String type="blond";
}
@Data
class Vodka {
   private final String type="deadly";

}