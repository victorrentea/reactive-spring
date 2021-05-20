package victor.training.reactive.reactor.complex;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SlowFastItems {
   public static void main(String[] args) {
      Random r = new Random();
      List<Integer> numbers = Stream.generate(()->r.nextInt(10)*100).limit(10).collect(Collectors.toList());

      Stream.of(1,2,3)
          .sequential()
          .map(n -> /*DB SELECT */n * 2)
          .parallel()
          .map(n -> /*CPU*/n * 2)

      ;

      Flux<String> input = Flux.fromIterable(numbers)
//          .parallel()
//          .sequential()
          .flatMap(n -> fireDataAfterTime(n + "", n));



//          .toStream()
//          .forEach(System.out::println);

   }

   public static Mono<String> fireDataAfterTime(String data, int millis) {
      return Mono.just(data).delayUntil(d -> Mono.delay(Duration.ofMillis(millis)));
   }
}



