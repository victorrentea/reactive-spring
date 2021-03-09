package victor.training.reactor;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public class Batch {

   public static void main(String[] args) {
      Path ipPath = Paths.get("/some/path/large-input-file.txt");

      // TODO: read file
//      Flux<String> stringFlux = Flux.using(open as Stream, Stream->Flux, close Stream);

      // TODO: write file
//      flux.subscribe(line, error, complete)

      Flux<Integer> f = Flux.fromStream(IntStream.range(1, 105).boxed());
      f.buffer(10)
          .zipWith(Flux.fromStream(Stream.iterate(1, i -> i + 1)))
          .flatMap(pageWithIndex -> Batch.save(pageWithIndex.getT1())
              .onErrorResume(t -> {
                 log.error("Error at page " + pageWithIndex.getT2());
                 return Mono.empty();
              }))
          .subscribe();

      // TODO: write 3 file at one filtering lines
      // read in 3 threads
   }

   private static Mono<Void> save(List<Integer> page) {
      return Mono.fromRunnable(() -> {
         System.out.println("Save page " + page);
         if (Math.random() < 0.3) {
            throw new IllegalArgumentException();
         }
      });
   }

}
// https://www.vinsguru.com/reactor-flux-file-reading/