package victor.training.reactor.dynamic;

import lombok.Value;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SelectingFlow {
   public static void main(String[] args) {
      Flux.range(1,10)
          ;
   }

   private static Mono<Void> callA(Integer id) {
      System.out.println("calling A " + id);
      return Mono.empty();
   }
   private static Mono<Void> callB(Integer id) {
      System.out.println("calling B " + id);
      return Mono.empty();
   }

   static Mono<FlowCategory> fetchItemCategory(Integer id) {
      return Mono.just(FlowCategory.values()[id%FlowCategory.values().length]);
   }
}

@Value
class ItemWithCategory {
   Integer id;
   FlowCategory category;
}

enum FlowCategory {
   NOOP,
   CALL_A,
   CALL_B,
   CALL_AB
}