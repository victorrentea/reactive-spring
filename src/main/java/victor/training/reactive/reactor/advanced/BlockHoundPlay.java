package victor.training.reactive.reactor.advanced;

import reactor.blockhound.BlockHound;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class BlockHoundPlay {
   public static void main(String[] args) {

      BlockHound.install();

      Mono.delay(Duration.ofSeconds(1))
          .doOnNext(it -> {
             try {
                Thread.sleep(10);
             }
             catch (InterruptedException e) {
                throw new RuntimeException(e);
             }
          })
          .block();
   }

}
