package victor.training.reactor.hotcold;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
public class HotCold {
   public static void main(String[] args) {


      Flux<Long> interval = Flux.interval(Duration.ofMillis(100));
      ConnectableFlux<Long> publish = interval.publish();



      System.out.println(interval.take(3).collectList().block());
      System.out.println(interval.take(3).collectList().block());
   }
}
