package victor.training.reactor.dynamic;

import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.KafkaSender;

import java.util.function.BiFunction;
import java.util.function.Function;

public class SelectingFlow {
   public static void main(String[] args) {
      // you get the category of the item via #fetchItemCategory
      // then, based on the category, execute different flows for each item
      Flux.range(1, 10)
          .flatMap(id -> fetchItemCategory(id).map(category -> new ItemWithCategory(id, category)))
          .flatMap(idCat -> new SelectingFlow().process(idCat))

         .subscribe()

      ;
   }

   @Autowired // prented
   KafkaSender<String, String> kafkaSender;
   public Mono<Void> callA(Integer id) {
      System.out.println("calling A " + id);
      return Mono.empty();
   }

   public Mono<Void> callB(Integer id) {
      System.out.println("calling B " + id);
      return Mono.empty();
   }
   static Mono<FlowCategory> fetchItemCategory(Integer id) {
      return Mono.just(FlowCategory.values()[id%FlowCategory.values().length]);
   }

   public Mono<Void> process(ItemWithCategory idCat) {
      return idCat.getCategory().flowSupplier.apply(this, idCat.getId());
   }

   public Mono<Void> callAThenB(Integer id) {
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
   NOOP((x, id) -> Mono.empty()),
   CALL_A(SelectingFlow::callA),
   CALL_B(SelectingFlow::callB),
   CALL_A_THEN_B(SelectingFlow::callAThenB);
   public final BiFunction<SelectingFlow, Integer, Mono<Void>> flowSupplier;

   FlowCategory(BiFunction<SelectingFlow, Integer, Mono<Void>> flowSupplier) {
      this.flowSupplier = flowSupplier;
   }

}