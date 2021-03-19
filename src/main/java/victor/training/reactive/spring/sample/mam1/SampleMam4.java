package victor.training.reactive.spring.sample.mam1;

import lombok.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SampleMam4 {


   public static void main(String[] args) {
      MasterItem receivedMasterItem = null;
      Mono<ItemWithState2> defaultMono = Mono.just(new ItemWithState2(receivedMasterItem, ItemState2.DEFAULT));
      Mono<ItemWithState2> warningMono = Mono.empty();
      Mono<ItemWithState2> invalidAmount = Mono.just(new ItemWithState2(receivedMasterItem, ItemState2.INVALID_AMOUNT_ITEM_CHANGE));

      Mono<ItemWithState2> lastNonEmpty = Flux.concat(Flux.just(defaultMono, warningMono, invalidAmount)).reduce((x, y) -> y);
      System.out.println(lastNonEmpty.block());
   }


//   public Mono<ItemWithState2> determineState(MasterItem receivedMasterItem) {
//
//      Mono<ItemWithState2> a = Mono.just(new ItemWithState2(receivedMasterItem, ItemState2.DEFAULT));
//      Mono<ItemWithState2> b = Mono.empty();
//      Mono<ItemWithState2> c = Mono.just(new ItemWithState2(receivedMasterItem, ItemState2.INVALID_AMOUNT_ITEM_CHANGE));
//
//      Flux.concat(Flux.just(a,b,c)).reduce((x,y) -> y);
//
//      return Mono.just(new ItemWithState2(receivedMasterItem, ItemState2.DEFAULT))
//          .filter(this::isNotWarning)
//          .defaultIfEmpty(new ItemWithState2(receivedMasterItem, ItemState2.WARNING))
//          .filter(this::isNotVariantGroupCodeRequiredRejection)
//          .defaultIfEmpty(new ItemWithState2(receivedMasterItem, ItemState2.VARIANT_GROUP_CODE_REQUIRED))
//          .filterWhen(this::isNotInvalidAmountItemChangeRejection)
//          .defaultIfEmpty(new ItemWithState2(receivedMasterItem, ItemState2.INVALID_AMOUNT_ITEM_CHANGE));
//   }
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