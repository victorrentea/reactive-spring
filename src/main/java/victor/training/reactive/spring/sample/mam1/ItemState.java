package victor.training.reactive.spring.sample.mam1;

import reactor.core.publisher.Flux;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public enum ItemState {
      /**
       * The <code>predicate</code> is used to determine the state of an
       * individual item.
       * The <code>sender</code> determines what to send where.
       */
      NEW(ItemState::isNew, ItemStateHandler::whenNew),
      UPDATE(ItemState::isUpdate, ItemStateHandler::whenUpdated),
      REJECTED(ItemState::isRejected, ItemStateHandler::whenRejected),
      INVALID_DATA(ItemState::hasInvalidContent, ItemStateHandler::whenError),
      ERROR(ItemState::isError, ItemStateHandler::whenError),
      // MONITOR items are sent to the deduplicator
      MONITOR(ItemState::isMonitor, ItemStateHandler::whenMonitor);

      final BiPredicate<MasterItem, AnalyserItem> predicate;
      final BiFunction<ItemStateHandler, Flux<ItemWithState>, Flux<UUID>> sender;

      ItemState(BiPredicate<MasterItem, AnalyserItem> predicate,
                BiFunction<ItemStateHandler, Flux<ItemWithState>, Flux<UUID>> sender) {
         this.predicate = predicate;
         this.sender = sender;
      }

      /**
       * The ERROR state is set, if the GTIN is missing.
       * The NEW state is automatically set if cassandra does not return an
       *   AnalyserItem or if the relation provider did not acknowledge having
       *   sent the relation to the key provider.
       * The UPDATE and REJECTED states depend on the new GTIN being unchanged.
       * The MONITOR state is set, if the MasterItem is a monitor item.
       */
      static ItemState determineFor(MasterItem masterItem, AnalyserItem analyserItem) {
         return Stream.of(INVALID_DATA, NEW, UPDATE, REJECTED)
             .filter(state -> state.predicate.test(masterItem, analyserItem))
             .findAny()
             .orElse(ERROR); // just in case
      }

      static boolean isNew(MasterItem masterItem, AnalyserItem analyserItem) {
         return analyserItem == null || !analyserItem.isRelationProvided() && isUpdate(masterItem, analyserItem);
      }

      static boolean isUpdate(MasterItem masterItem, AnalyserItem analyserItem) {
         return masterItem.hasGtin() && Objects.equals(masterItem.getGtin(), analyserItem.getGtin());
      }

      static boolean isRejected(MasterItem masterItem, AnalyserItem analyserItem) {
         return masterItem.hasGtin() && !Objects.equals(masterItem.getGtin(), analyserItem.getGtin());
      }

      private static boolean hasInvalidContent(MasterItem masterItem, AnalyserItem analyserItem) {
         return Analyser.hasInvalidContent(masterItem, false);
      }

      static boolean isError(MasterItem masterItem, AnalyserItem analyserItem) {
         return !masterItem.hasValidGtin();
      }

      static boolean isMonitor(MasterItem masterItem, AnalyserItem analyserItem) {
         return masterItem.isMonitorItem();
      }
   }