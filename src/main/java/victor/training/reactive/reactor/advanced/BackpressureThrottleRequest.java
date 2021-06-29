package victor.training.reactive.reactor.advanced;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class BackpressureThrottleRequest {
   public static void main(String[] args) {

      List<Integer> collect = Flux.range(1, 30)
          .log("UP")
          .limitRate(10)
          .log("DOWN")
          .collect(Collectors.toList())
          .block();


   }
}
