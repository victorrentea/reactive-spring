package victor.training.reactive.reactor.complex;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class CollectListPitfall {
   public static void main(String[] args) {
      Flux<Integer> objectFlux = Flux.create(sink -> {
         sink.next(1);
         sink.next(2);
      });
      Mono<List<Integer>> listMono = objectFlux.collectList();

      List<Integer> block = listMono.block();
      System.out.println(block);
   }
}
