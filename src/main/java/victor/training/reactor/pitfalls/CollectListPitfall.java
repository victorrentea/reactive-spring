package victor.training.reactor.pitfalls;

import reactor.core.publisher.Flux;

public class CollectListPitfall {

   public static void main(String[] args) {

      Flux<Integer> intFlux = Flux.create(sink -> {
         sink.next(1);
         sink.next(2);
         sink.complete();
      });

      System.out.println(intFlux.collectList().block());


   }
}
