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

import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.in;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static victor.training.reactive.intro.ThreadUtils.sleep;

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

      Builder builder = BlockHound.builder();
      ServiceLoader.load(BlockHoundIntegration.class).forEach(integration -> integration.applyTo(builder));
      builder
          .allowBlockingCallsInside("io.netty.resolver.HostsFileParser", "parse")
          .install();
   }

   private final Barman barman;

   @Override
   public void run(String... args) throws Exception {
      log.info("Talking to proxied barman: " + barman.getClass());

      long t0 = currentTimeMillis();

      Beer beer = barman.pourBeer();
      Vodka vodka = barman.pourVodka();

      log.info("Drinks ready: {}", List.of(beer, vodka));
      log.debug("Time= " + (currentTimeMillis() - t0));
   }
}

@Slf4j
@Service
@RequiredArgsConstructor
class Barman {
   public Beer pourBeer() {
      log.info("Start pour beer");
      sleep(1000); // imagine blocking REST call
      log.info("End pour beer");
      return new Beer();
   }

   public Vodka pourVodka() {
      log.info("Start pour vodka");
      sleep(1000);  // imagine blocking DB call
      log.info("End pour vodka");
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
      log.debug("Mixing {} with {} (takes time) ...", beer, vodka);
      sleep(500);
   }
}

