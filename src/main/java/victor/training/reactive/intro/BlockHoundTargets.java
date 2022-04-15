package victor.training.reactive.intro;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static victor.training.reactive.intro.Utils.installBlockHound;

@Slf4j
@RestController
public class BlockHoundTargets {

   //@EventListener(ApplicationStartedEvent.class) // TODO uncomment
   public void setupBlockingDetection() {
      installBlockHound(List.of(Tuples.of("io.netty.resolver.HostsFileParser", "parse")));
   }

   // Block hound this:
   @GetMapping("cache")
   @Cacheable("cache")
   public String cache() {
      log.info("In method");
      return "A";
   }

   @GetMapping("RestTemplate")
   public String rest() throws IOException {
      return "Got" + new RestTemplate().getForObject("http://localhost:9999/api/product/1", String.class);
   }
   @GetMapping("WebClient")
   public Mono<String> restWebclient() {
      return WebClient.create().get().uri("http://localhost:9999/api/product/1").retrieve()
          .bodyToMono(String.class)
          .map(s -> "Got " + s);
   }

   @GetMapping("file")
   public String writeFile() throws IOException {
      try (FileWriter writer = new FileWriter("a.txt")) {
         writer.write("HALO!");
      }
      return "wrote file OK";
   }

   @GetMapping("log")
   public String log() throws IOException {
      log.info("Logging is safe (non-blocking)");
      return "logged OK";
   }

}

