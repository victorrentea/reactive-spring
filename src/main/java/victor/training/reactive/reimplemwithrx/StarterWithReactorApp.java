package victor.training.reactive.reimplemwithrx;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.intro.ThreadUtils;

import java.util.concurrent.ExecutionException;

@Slf4j
@EnableAsync
@SpringBootApplication
@RequiredArgsConstructor
@RestController
public class StarterWithReactorApp /*implements CommandLineRunner */{

   public static void main(String[] args) {
      SpringApplication.run(StarterWithReactorApp.class, args);
   }

   private final Barman barman;

   @GetMapping("dilly")
   public Mono<DillyDilly> run() throws ExecutionException, InterruptedException {
//   public void run(String... args) throws Exception {


      log.info("Sending orders calls to the barman : " + barman.getClass());
      long t0 = System.currentTimeMillis();

      Mono<Beer> beerMono = barman.pourBlondBeer()
          .subscribeOn(Schedulers.boundedElastic());
      Mono<Vodka> vodkaMono = barman.pourVodka()
          .subscribeOn(Schedulers.boundedElastic());

      Mono<DillyDilly> dillyDillyMono = beerMono
          .publishOn(Schedulers.parallel())
          .zipWith(vodkaMono, (beer, vodka) -> new DillyDilly(beer, vodka));

//      dillyDillyMono.subscribe( dilly->{
//         log.info("Digesting : " + dilly);
//      });

      long t1 = System.currentTimeMillis();
      log.debug("Time= " + (t1 - t0));
      return dillyDillyMono;

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
   public Mono<Beer> pourBlondBeer() {
      return Mono.defer(() ->  {
         log.info("Start pour beer");
         ThreadUtils.sleep(1000); // blocking REST call
         log.info("end pour beer");
         return Mono.just(new Beer()); // WRONG!!!! because every caller of this method still blocks
      });
   }
   public Mono<Vodka> pourVodka() {
      return Mono.defer(()  -> {
         log.info("Start pour vodka beer");
         ThreadUtils.sleep(1000); // blocking REST call
         log.info("end pour vodka");
         return Mono.just(new Vodka());
      });
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

