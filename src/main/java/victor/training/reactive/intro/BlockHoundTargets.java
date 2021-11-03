package victor.training.reactive.intro;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileWriter;
import java.io.IOException;

@Slf4j
@RestController
public class BlockHoundTargets {

   // Block hound this:
   @GetMapping("cache")
   @Cacheable("cachex")
   public String cache() {
      log.info("In method");
      ThreadUtils.sleep(1000);
      return "A";
   }












   @GetMapping("log")
   public String log() throws IOException {
      log.info("Logging is safe (non-blocking)");
      return "logged OK";
   }
   @GetMapping("writeFile")
   public String writeFile() throws IOException {
      try (FileWriter writer = new FileWriter("a.txt")) {
         writer.write("HALO!");
      }
      return "wrote file OK";
   }


}
