package victor.training.asyncrx;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactivespring.start.ThreadUtils;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
@RestController
public class StarterRxApp {

   public static void main(String[] args) {
      SpringApplication.run(StarterRxApp.class, args);
   }


   private final Barman barman;

   @GetMapping("dilly")
   public Mono<DillyDilly> getDilly() throws ExecutionException, InterruptedException {

      log.info("Sending method calls to the barman : " + barman.getClass());
      long t0 = System.currentTimeMillis();

      Mono<Beer> beerMono = Mono.defer(() -> barman.pourBeer())
          .subscribeOn(Schedulers.boundedElastic())
          ;
      Mono<Vodka> vodkaMono = Mono.defer(() ->barman.pourVodka())
          .subscribeOn(Schedulers.boundedElastic()) // this runs each mono in parallel. submitting twice to boundedElastic
          ;

      Mono<DillyDilly> dillyMono = beerMono.zipWith(vodkaMono, DillyDilly::new);
//         ; // running both monos above on the SAME thread (no parallelism)

      long t1 = System.currentTimeMillis();
      log.debug("Got my drinks " + (t1 - t0));
      return dillyMono;
   }
}


@Slf4j
@Service
@RequiredArgsConstructor
class Barman {
   public Mono<Beer> pourBeer() {
      log.info("Start pour beer");
      ThreadUtils.sleep(1000); // blocking REST call
      log.info("end pour beer");
      return Mono.just(new Beer());
   }
   public Mono<Vodka> pourVodka() {
      log.info("Start pour vodka");
      ThreadUtils.sleep(1000);  // blocking DB call
      log.info("end pour vodka");
      return Mono.just(new Vodka());
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
// Bere dupa Vin  e un Chin
@Value
@Slf4j
class DillyDilly {
   Beer beer;
   Vodka vodka;

   public DillyDilly(Beer beer, Vodka vodka) {
      log.info("Where am I ? ");
      this.beer = beer;
      this.vodka = vodka;
   }
}
