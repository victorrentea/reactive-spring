package victor.training.reactive.spring.sample.mam2;

import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import victor.training.reactive.spring.sample.mam1.MasterItem;

import java.util.function.Function;
import java.util.stream.Stream;

public enum ItemState {
      IN_METRO_ASSORTMENT(ItemState::isInMetroAssortment,
          deduplicator -> Flux.just(deduplicator.rejectedMasterItemSender)),
      NOT_IN_METRO_ASSORTMENT(ItemState::isNotInMetroAssortment,
          deduplicator -> Flux.just(deduplicator.masterItemSender)), // TODO victor Flux de functii care intor Fluxuri... excelent!
      ERROR,
      MONITOR(ItemState::isMonitorItem,
          deduplicator -> Flux.just(deduplicator.masterItemSender, deduplicator.rejectedMasterItemSender));

      @FunctionalInterface
      interface Matcher {
         boolean match(MasterItem m, BobResponse b, HttpStatus h);
      }

      final Matcher matcher;
      final Function<SampleMam2_Deduplicator, Flux<BaseItemSender<DeduplicatorItem, ?>>> sender;

      ItemState() { // TODO victor delete -> inline in ERROR()
         this((item, bobResponse, state) -> true, deduplicator -> Flux.empty()); // errors are not sent anywhere
      }

      ItemState(Matcher matcher, Function<SampleMam2_Deduplicator, Flux<BaseItemSender<DeduplicatorItem, ?>>> sender) {
         this.matcher = matcher;
         this.sender = sender;
      }

      static ItemState determineFor(MasterItem masterItem, BobResponse bobResponse, HttpStatus status) {
         return Stream.of(MONITOR, IN_METRO_ASSORTMENT, NOT_IN_METRO_ASSORTMENT)
             .filter(state -> state.matcher.match(masterItem, bobResponse, status))
             .findAny()
             .orElse(ERROR);
      }

      static boolean isInMetroAssortment(MasterItem masterItem, BobResponse response, HttpStatus status) {
         // the articleNo is the one and only must
         return !masterItem.isMonitorItem() &&
                status == HttpStatus.OK && response != null && response.getArticleNo() != null;
      }

      static boolean isNotInMetroAssortment(MasterItem masterItem, BobResponse response, HttpStatus status) {
         // also articles without articleNo
         return !masterItem.isMonitorItem() && (
             status == HttpStatus.NO_CONTENT ||
             status == HttpStatus.OK && response != null && response.getArticleNo() == null);
      }

      static boolean isMonitorItem(MasterItem masterItem, BobResponse response, HttpStatus status) {
         return masterItem.isMonitorItem();
      }
   }