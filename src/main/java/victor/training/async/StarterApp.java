package victor.training.async;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import victor.training.reactivespring.start.ThreadUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@EnableAsync
@SpringBootApplication
@RequiredArgsConstructor
@RestController
public class StarterApp  {

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


   // under the hood, in a Tomcat-based app (servlet 3.0), Spring does:
//   HttpServletRequest request;
//   AsyncContext asyncContext = request.startAsync();
//		return from worker threa d
//   // in a different thread
//		asyncContext.getResponse().getWriter().println("");


   // in netty it gets even worse

   @GetMapping("dilly")
   public CompletableFuture<DillyDilly> getDilly() throws ExecutionException, InterruptedException {

      log.info("Sending method calls to the barman : " + barman.getClass());
      long t0 = System.currentTimeMillis();

      CompletableFuture<Beer> futureBeer = barman.pourBeer();
      CompletableFuture<Vodka> futureVodka = barman.pourVodka();

      CompletableFuture<DillyDilly> futureDilly = futureBeer
          .thenCombine(futureVodka, DillyDilly::new);

      long t1 = System.currentTimeMillis();
      log.debug("Got my drinks " + (t1 - t0));
      return futureDilly;
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
      ThreadUtils.sleep(20000); // blocking REST call
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