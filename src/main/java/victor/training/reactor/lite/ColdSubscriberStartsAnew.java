package victor.training.reactor.lite;

import reactor.core.publisher.Flux;
import victor.training.reactivespring.start.ThreadUtils;

import java.time.Duration;

public class ColdSubscriberStartsAnew {
   public static void main(String[] args) {

      Flux<Long> flux = Flux.interval(Duration.ofMillis(500));

      flux.subscribe(x -> System.out.println("A" + x));

      ThreadUtils.sleep(2000);
      flux.subscribe(x -> System.out.println("B" + x));


      ThreadUtils.sleep(5000);
   }

}
