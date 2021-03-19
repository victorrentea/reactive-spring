package victor.training.reactive.spring.files;

import org.apache.logging.log4j.util.Strings;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ReadingPom {
   public static void main(String[] args) throws IOException, InterruptedException {

      Path path = new File("pom.xml").toPath();
//      try (Stream<String> lines = Files.lines(path)) {
//         lines.filter(Strings::isNotBlank)
//             .map(String::toUpperCase)
//             .forEach(System.out::println);
//      }

      // CITIRE din FLUX, posibil dintr-un fisier gigantic

      Flux<String> stringFlux = Flux.using(
          () -> Files.lines(path),  // cum obtin resursa
          Flux::fromStream, // cum convertesc resursa la flux
          Stream::close // cum inchid resursa
      )
          .filter(Strings::isNotBlank)
          .map(String::toUpperCase);



      File out = new File("pom-upper.xml");
      PrintWriter fileWriter = new PrintWriter(out);
      stringFlux
          .publishOn(Schedulers.boundedElastic())
          .log()
          .subscribe(
             line -> fileWriter.println(line),
             e -> e.printStackTrace(),
              fileWriter::close
      );

      Thread.sleep(1000);
   }
}
