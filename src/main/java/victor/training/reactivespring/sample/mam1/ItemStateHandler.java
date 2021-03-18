package victor.training.reactivespring.sample.mam1;

import reactor.core.publisher.Flux;

import java.util.UUID;

public class ItemStateHandler {

   public static Flux<UUID> whenNew(ItemStateHandler itemStateHandler, Flux<ItemWithState> itemWithStateFlux) {
      return null;
   }

   public static Flux<UUID> whenUpdated(ItemStateHandler itemStateHandler, Flux<ItemWithState> itemWithStateFlux) {
      return Flux.empty();
   }

   public static Flux<UUID> whenRejected(ItemStateHandler itemStateHandler, Flux<ItemWithState> itemWithStateFlux) {
      return null;
   }

   public static Flux<UUID> whenError(ItemStateHandler itemStateHandler, Flux<ItemWithState> itemWithStateFlux) {
      return null;
   }

   public static Flux<UUID> whenMonitor(ItemStateHandler itemStateHandler, Flux<ItemWithState> itemWithStateFlux) {
      return null;
   }
}
