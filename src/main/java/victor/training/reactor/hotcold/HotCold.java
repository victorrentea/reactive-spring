package victor.training.reactor.hotcold;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactivespring.start.ThreadUtils;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
public class HotCold {
   public static void main(String[] args) {

      Mono.just("Hello")
          .flatMap(s-> Mono.deferContextual(
              context -> Mono.just(s + " " + context.get("username"))))
          .contextWrite(context -> context.put("username","John"));
   }
}
