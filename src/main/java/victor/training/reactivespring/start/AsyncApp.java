package victor.training.reactivespring.start;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

@EnableAsync
@SpringBootApplication
public class AsyncApp {
   public static void main(String[] args) {
      SpringApplication.run(AsyncApp.class, args); // Note: .close added to stop executors after CLRunner finishes
   }

   @Bean
   public ThreadPoolTaskExecutor beerExecutor() {
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(1);
      executor.setMaxPoolSize(1);
      executor.setQueueCapacity(500);
      executor.setThreadNamePrefix("beer-");
      executor.initialize();
      executor.setWaitForTasksToCompleteOnShutdown(true);
      return executor;
   }

   @Bean
   public ThreadPoolTaskExecutor vodkaExecutor() {
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(2);
      executor.setMaxPoolSize(4);
      executor.setQueueCapacity(500); // foarte mare ==> OOM + waiting time indecent : Felicitari! esti a 150 persoana la casa! Craciun in Romania.
      executor.setThreadNamePrefix("vodka-");
      executor.setKeepAliveSeconds(3);
      executor.setRejectedExecutionHandler(new CallerRunsPolicy());
      executor.initialize();
      executor.setWaitForTasksToCompleteOnShutdown(true);

      // identic cu: pe java SE
      new ThreadPoolExecutor(2, 4, 3, TimeUnit.SECONDS, new ArrayBlockingQueue<>(500), new CallerRunsPolicy());

      // e BINE sa le pui core < max daca vrei sa lasi spatiu pentru alte lucruri pe acea masina. 2000' style.
      // Astazi (in contextul docker) nu prea mai ai in Prod alte app pe aceeasi masina.

      // E periculos sa ai max = 2 x min: pt ca in conditii de stres e posibil sa pui mai multa presiune pe un sistem extern si asa sufocat deja.
      // altfel spus; nu stii pe ce apesi la stres.
      return executor;
   }

}

@Slf4j
@RestController
class Drinker {
   ExecutorService pool = Executors.newFixedThreadPool(2);
   @Autowired
   private Barman barman;

   @GetMapping("drink")
   public CompletableFuture<DillyDilly> getDrink() throws ExecutionException, InterruptedException {
      log.info("Submitting my order " + barman.getClass());

      // *********************************
      //        NU IROSIM THREADURI
      // *********************************

      CompletableFuture<Beer> futureBeer = CompletableFuture.supplyAsync(barman::getOneBeer, pool);

      CompletableFuture<Vodka> futureVodka = CompletableFuture.supplyAsync(barman::getOneVodka);


      //	Executors workflow DONE
      // CompletableFuture Fara SPring DONE
      // TODO Ex handling
      // TODO Full Ex non-blocking

      log.info("Fata pleaca cu comanda");

      CompletableFuture<DillyDilly> futureDilly = futureBeer.thenCombine(futureVodka, (b, v) -> new DillyDilly(b, v));

      log.info("Ma intorc in thread pool");
      return futureDilly;
   }
}

// Vinul dupa Bere e placere,
// Berea dupa vin e un Chin

@Slf4j
@Data
class DillyDilly {
   private final Beer beer;
   private final Vodka vodka;

   DillyDilly(Beer beer, Vodka vodka) {
      log.info("Amestec bauturi ");
      ThreadUtils.sleep(1000);


      this.beer = beer;
      this.vodka = vodka;
   }
}

@Slf4j
@Service
class Barman {
   public Beer getOneBeer() {
      log.info("Pouring Beer...");

      // REST call blocant: threadul din care chemi getForObject e blocat pana vine raspunsul HTTP 10ms-2s
//      RestTemplate rest = new RestTemplate();
//      String contentAsString = rest.getForObject("http://bla", String.class);

      // REST call non-blocant: nici un thread din OS-ul nu sta blocat in asteptarea acestui raspuns
//      Mono<String> stringMono = WebClient.create().get().uri("http://bla").retrieve().bodyToMono(String.class);


      ThreadUtils.sleep(1000); // network call
      return new Beer();
   }

   public Vodka getOneVodka() {
      log.info("Pouring Vodka...");
      ThreadUtils.sleep(1000); // DB call, file read
      return new Vodka();
   }
}

@Data
class Beer {
   public final String type = "blond";
}

@Data
class Vodka {
   public final String type = "deadly";
}
