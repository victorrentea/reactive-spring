package victor.training.reactor.dynamic;

import lombok.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Function;

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

   public static Mono<Void> callA(Integer id) {
      System.out.println("calling A " + id);
      return Mono.empty();
   }

   public static Mono<Void> callB(Integer id) {
      System.out.println("calling B " + id);
      return Mono.empty();
   }
   static Mono<FlowCategory> fetchItemCategory(Integer id) {
      return Mono.just(FlowCategory.values()[id%FlowCategory.values().length]);
   }

   private static Mono<Void> process(ItemWithCategory idCat) {
      return idCat.getCategory().flowSupplier.apply(idCat.getId());
   }

   public static Mono<Void> callAThenB(Integer id) {
      Mono<Void> monoA = callA(id).subscribeOn(Schedulers.boundedElastic());
      Mono<Void> monoB = callB(id).subscribeOn(Schedulers.boundedElastic());
      return Mono.when(monoA, monoB);
   }
}

@Value
class ItemWithCategory {
   Integer id;
   FlowCategory category;
}

enum FlowCategory {
   NOOP(id -> Mono.empty()),
   CALL_A(SelectingFlow::callA),
   CALL_B(SelectingFlow::callB),
   CALL_A_THEN_B(SelectingFlow::callAThenB);
   public final Function<Integer, Mono<Void>> flowSupplier;

   FlowCategory(Function<Integer, Mono<Void>> flowSupplier) {
      this.flowSupplier = flowSupplier;
   }
}
}