package victor.training.reactivespring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@RestController
public class FloodController {

   @GetMapping("flood/generate")
   public Flux<String> flood() {
      return Flux.<String>generate(sink ->
          sink.next(new DataItem("Hello World!").toString() + " \n"))
          .take(100_000_000);
   }
   @GetMapping("flood/download")
   public Flux<String> download() throws IOException {


      Writer fileWriter = new FileWriter("download.dat");
      return WebClient.create().get().uri("http://localhost:8080/flood/generate")
          .retrieve()
          .bodyToFlux(String.class)
//          .bodyToFlux(DataItem.class)// doesnt work with huge data
//          .map(DataItem::toString)
          .doOnNext(s -> {
             try {
                fileWriter.write(s);
             } catch (IOException e) {
                throw new RuntimeException(e);
             }
          })
          .doFinally(signalType -> {
             try {
                fileWriter.close();
             } catch (IOException e) {
                e.printStackTrace();
             }
          })
          .scan(0, (sum, x) -> sum + 1)
          .sample(Duration.ofMillis(500))
          .map(i -> i + "<br>");
   }
}
@Data
@AllArgsConstructor
@NoArgsConstructor
class DataItem {
   private String value;
}
