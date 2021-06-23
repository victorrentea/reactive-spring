package victor.training.reactive.intro;

import reactor.core.publisher.Flux;

import java.time.Duration;

public class CollectListPitfall {
   public static void main(String[] args) {

      Flux<String> flux = Flux.create(sink ->
      {
         sink.next("a");
//         sink.complete();
      });


//      flux.collectList().subscribe(System.out::println);
//
//      ThreadUtils.sleep(1000);
      System.out.println(flux.collectList().timeout(Duration.ofMillis(2000)).block());


   }
}
