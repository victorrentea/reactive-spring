package victor.training.reactor.lite;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class ZipBarrier {
   public static void main(String[] args) {

      Flux<Integer > s  = Flux.just(1);

      Mono<String> mono1 = requestString(1);
      Mono<String> mono2 = requestString(2);

      Mono.zip(mono1, mono2, (s1,s2) -> {
         return s1 + s2;
      })
          .doOnNext(x -> log.info("Combned " + x))
      .block()
      ;
   }

   private static Mono<String> requestString(int i ) {
      return Mono.fromCallable(() -> {
         Thread.sleep(12);

         return "" + i;
      })

//          .subscribeOn(Schedulers.boundedElastic())
          .doOnNext(s -> log.info("Fetching " + s));
   }
}
