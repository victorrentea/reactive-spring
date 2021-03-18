package victor.training.reactor.dynamic;

import lombok.Value;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class SelectingFlow {
   public static void main(String[] args) {
      // you get the category of the item via #fetchItemCategory
      // then, based on the category, execute different flows for each item
      Flux.range(1, 10)
          .flatMap(id -> fetchItemCategory(id).map(category -> new ItemWithCategory(id, category)))
          .flatMap(SelectingFlow::process)

         .subscribe()

      ;
   }

   private static Mono<Void> process(ItemWithCategory idCat) {
      switch (idCat.getCategory()) {
         case CALL_A:
            return callA(idCat.getId());
         case CALL_B:
            return callB(idCat.getId());
         case CALL_A_THEN_B:
            Mono<Void> monoA = callA(idCat.getId()).subscribeOn(Schedulers.boundedElastic());
            Mono<Void> monoB = callB(idCat.getId()).subscribeOn(Schedulers.boundedElastic());
            return Mono.when(monoA, monoB);
         default: // problems might be here...
            return Mono.empty();
      }
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
   CALL_A_THEN_B
}