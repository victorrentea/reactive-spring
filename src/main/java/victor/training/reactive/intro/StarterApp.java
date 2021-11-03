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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockHound.Builder;
import reactor.blockhound.integration.BlockHoundIntegration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactive.reactor.complex.Product;

import java.sql.Connection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static reactor.core.scheduler.Schedulers.boundedElastic;
import static reactor.core.scheduler.Schedulers.parallel;

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

//      WebClient.create().get().uri("http://localhost/").retrieve().bodyToMono(String.class).block();
//      BlockHound.install();


      Builder builder = BlockHound.builder();
      ServiceLoader<BlockHoundIntegration> serviceLoader = ServiceLoader.load(BlockHoundIntegration.class);
      Stream
          .concat(StreamSupport.stream(serviceLoader.spliterator(), false), Stream.of())
          .sorted()
          .forEach(builder::with);

       builder.allowBlockingCallsInside("io.netty.resolver.HostsFileParser", "parse")
          .install();
   }


   @Data
   static class UnDtoCuOListaBabana {
      List<String> babana = new ArrayList<>();
   }


   @GetMapping("flood")
   public Flux<String> daiFrate() {
      return Flux.range(1, 1_000_000_000).map(n -> "y".repeat(1000));
   }
   @GetMapping("unu-da-mare")
   public Mono<UnDtoCuOListaBabana> unuDaMare() {
      UnDtoCuOListaBabana dto = new UnDtoCuOListaBabana();
      dto.getBabana().addAll(IntStream.range(1, 15_000_000).mapToObj(i -> "x").collect(Collectors.toList()));
      return Mono.just(dto);
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


//      HttServReq

//      Connection conn; // din conn pool
//      conn.setAutoCommit(false); // ~= START TRANSACTION ==== @Transactional asta face
//      conn.prepareStatement().execute()
//       conn.commit();; // o face tot aspectul @Transacatipn
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
      Mono<Beer> beerMono = barman.pourBeer();
      Mono<Vodka> vodkaMono = Mono.fromSupplier(barman::pourVodka)
          .subscribeOn(parallel())
//          .subscribeOn(boundedElastic())
          ;

      Mono<DillyDilly> monoDilly = beerMono.zipWith(vodkaMono, (beer, vodka) -> new DillyDilly(beer, vodka));

      CompletableFuture<DillyDilly> futureDilly = monoDilly.toFuture(); // porneste efectiv munca in paralel aici

      log.info("Thradul tomcatului iese aici dupa {}", System.currentTimeMillis() - t0);
      return futureDilly;
   }
}

@Slf4j
@Service
@RequiredArgsConstructor
class Barman {
   public Mono<Beer> pourBeer() {

      List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

//      numbers.stream()
//          .filter(n -> n%2 == 1)
//          .map(n -> n * 2)
//          .collect(Collectors.toList());

//      log.info("Start pour beer");
//      return WebClient.create()
//          .get()
//          .uri("http://localhost:9999/api/product/13")
//          // aka gata retrieve
//          .retrieve()
//
//          .bodyToMono(Product.class)
//          .map(p -> new Beer(p.getName()))
//
//          .doOnNext(b -> log.info("end pour beer: " + b))
//
//          ;




      return Mono.just(new Beer("bruna"));
//
//
//
//      RestTemplate rest = new RestTemplate(); // blocant !!
//      Product product = rest.getForObject("http://localhost:9999/api/product/13", Product.class);
//
//      log.info("end pour beer");
//      return new Beer(product.getName());
   }

   public Vodka pourVodka() {
      log.info("Start pour vodka");
      ThreadUtils.sleep(1000);  // blocking DB call
      log.info("end pour vodka");
      return new Vodka();
   }

//   {
//      Mono<> m("a")
//   }
//   // aspect:
//   return redisMono.defaultIfEmpty(::m)
//   @Cacheable
//   fun m(String s): Mono<String>
//   fun m(String s): String

}

// caching pe :
// rezultate ale fct pure => memoization > poate fi si in ConcurrentHashMap
// data mutable externe ==> caching ttl expiration si alte dureri





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

