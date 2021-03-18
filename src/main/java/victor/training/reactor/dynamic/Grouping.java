package victor.training.reactor.dynamic;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public class Grouping {
   public static void main(String[] args) {
      // TODO odd numbers should be sent in pages of 10 items to sendOdd, while even numbers to sendEven, respectively


   }
   public static Mono<Void> sendEven(List<Integer> page) {
      return Mono.fromRunnable(() -> log.info("Sending even: {}", page));
   }
   public static Mono<Void> sendOdd(List<Integer> page) {
      return Mono.fromRunnable(() -> log.info("Sending odd: {}", page));
   }
}
