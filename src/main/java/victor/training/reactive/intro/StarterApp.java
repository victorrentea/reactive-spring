package victor.training.reactive.intro;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockHound.Builder;
import reactor.blockhound.integration.BlockHoundIntegration;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.ServiceLoader;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static victor.training.reactive.intro.Utils.installBlockHound;
import static victor.training.reactive.intro.Utils.sleep;

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

   //@EventListener(ApplicationStartedEvent.class) // TODO uncomment
   public void setupBlockingDetection() {
      installBlockHound(List.of(Tuples.of("io.netty.resolver.HostsFileParser", "parse")));
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

