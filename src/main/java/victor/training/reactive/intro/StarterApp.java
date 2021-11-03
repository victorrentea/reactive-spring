package victor.training.reactive.intro;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.reactor.complex.Product;

import java.net.ServerSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static reactor.core.scheduler.Schedulers.boundedElastic;

@Slf4j
@EnableAsync
@SpringBootApplication
@RestController
@RequiredArgsConstructor
@EnableCaching
public class StarterApp implements CommandLineRunner {

   public static void main(String[] args) {
      SpringApplication.run(StarterApp.class, args);
   }

   @EventListener(ApplicationStartedEvent.class) // TODO uncomment
   public void installBlockHound() {
      log.info("--- App Started ---");
      log.warn("Installing BlockHound to detect I/O in non-blocking threads");
      BlockHound.install();
   }

   @Bean
   public ThreadPoolTaskExecutor pool() {
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(2);
      executor.setMaxPoolSize(2);
      executor.setQueueCapacity(500);
      executor.setThreadNamePrefix("bar-");
      executor.initialize();
      executor.setWaitForTasksToCompleteOnShutdown(true);
      return executor;
   }

   private final Barman barman;


   @Override
   public void run(String... args) throws Exception {
   }

   @GetMapping("drink")
   public CompletableFuture<DillyDilly> drink() throws ExecutionException, InterruptedException {
      log.info("Sending orders calls to the barman : " + barman.getClass());
      long t0 = System.currentTimeMillis();

      // !!! Important AICI se incepe executia (unlike a Mono/Flux care asteapta subscribe)
      Mono<Beer> beerMono = Mono.fromSupplier(barman::pourBeer)
          .subscribeOn(boundedElastic());
      Mono<Vodka> vodkaMono = Mono.fromSupplier(barman::pourVodka)
          .subscribeOn(boundedElastic());


      Mono<DillyDilly> monoDilly = beerMono.zipWith(vodkaMono, (beer, vodka) -> new DillyDilly(beer, vodka));

      CompletableFuture<DillyDilly> futureDilly = monoDilly.toFuture(); // porneste efectiv munca in paralel aici


      log.info("Thradul tomcatului iese aici dupa {}", System.currentTimeMillis()- t0);
      return futureDilly;
   }
}

@Slf4j
@Service
@RequiredArgsConstructor
class Barman {
   public Beer pourBeer() {
      log.info("Start pour beer");


      RestTemplate rest = new RestTemplate(); // blocant !!
      Product product = rest.getForObject("http://localhost:9999/api/product/13", Product.class);

      log.info("end pour beer");
      return new Beer(product.getName());
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
   private final String type;
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
      ThreadUtils.sleep(500);
   }
}

