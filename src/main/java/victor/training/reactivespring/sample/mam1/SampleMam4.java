package victor.training.reactivespring.sample.mam1;

import lombok.Value;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SampleMam4 {


   public static void main(String[] args) {
      MasterItem receivedMasterItem = null;
      Mono<ItemWithState2> defaultMono = Mono.just(new ItemWithState2(receivedMasterItem, ItemState2.DEFAULT));
      Mono<ItemWithState2> warningMono = Mono.empty();
      Mono<ItemWithState2> invalidAmount = Mono.just(new ItemWithState2(receivedMasterItem, ItemState2.INVALID_AMOUNT_ITEM_CHANGE));

//      System.out.println(lastNonEmpty.block());
   }


   public Mono<ItemWithState2> determineState(MasterItem receivedMasterItem) {

      Mono<ItemWithState2> a = Mono.just(new ItemWithState2(receivedMasterItem, ItemState2.DEFAULT));
      Mono<ItemWithState2> b = testWarning(receivedMasterItem);
      Mono<ItemWithState2> c = Mono.empty();
      Mono<ItemWithState2> d = Mono.just(new ItemWithState2(receivedMasterItem, ItemState2.INVALID_AMOUNT_ITEM_CHANGE));

      Mono<ItemWithState2> winner = Flux.concat(Flux.just(a, b, c, d)).next();

//      WARN

      return Mono.just(new ItemWithState2(receivedMasterItem, ItemState2.DEFAULT))
          //WARN
          .filter(this::isNotWarning) //false
          // empty mono
          .defaultIfEmpty(new ItemWithState2(receivedMasterItem, ItemState2.WARNING))
          // Mono<WARNING>
          .filter(this::isNotVariantGroupCodeRequiredRejection)
          .defaultIfEmpty(new ItemWithState2(receivedMasterItem, ItemState2.VARIANT_GROUP_CODE_REQUIRED))
          .filterWhen(this::isNotInvalidAmountItemChangeRejection)
          .defaultIfEmpty(new ItemWithState2(receivedMasterItem, ItemState2.INVALID_AMOUNT_ITEM_CHANGE));
   }

   private Mono<ItemWithState2> testWarning(MasterItem receivedMasterItem) {
      return Mono.<ItemWithState2>fromSupplier(() -> {
         //sleep call
//         if (true) {
//            return Mono.just(new ItemWithState2(receivedMasterItem, ItemState2.WARNING));
//         } else {
//            return Mono.empty();
//         }
         return null;
      });
   }

   private Publisher<Boolean> isNotInvalidAmountItemChangeRejection(ItemWithState2 itemWithState2) {
      return null;
   }

   private boolean isNotVariantGroupCodeRequiredRejection(ItemWithState2 itemWithState2) {
      return false;
   }

   private boolean isNotWarning(ItemWithState2 itemWithState2) {
      return false;
   }
}
@Value
class ItemWithState2 {
   MasterItem masterItem;
   ItemState2 state;
}
enum ItemState2 {
   DEFAULT,
   WARNING,
   VARIANT_GROUP_CODE_REQUIRED,
   INVALID_AMOUNT_ITEM_CHANGE,

}