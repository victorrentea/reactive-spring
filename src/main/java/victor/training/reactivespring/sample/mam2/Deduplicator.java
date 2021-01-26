package victor.training.reactivespring.sample.mam2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import victor.training.reactivespring.sample.mam1.MasterItem;
import victor.training.reactivespring.sample.mam1.ReceiverRecord;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Deduplicator
{
   private static final Logger LOGGER = LoggerFactory.getLogger(Deduplicator.class);
   public BaseItemSender<DeduplicatorItem, ?> rejectedMasterItemSender;
   public BaseItemSender<DeduplicatorItem, ?> masterItemSender;
   private int keyProviderConcurrency;
   private MasterItemReceiver masterItemReceiver;
   private BobControllerClient bobControllerClient;
   private int keyProviderPrefetch;
   private Disposable subscription;
   private AtomicInteger totalRecordCount;

//1. flow-ul serviciului deduplicator

/**
 * describes the deduplication workflow on application ctx startup.
 */
   @PostConstruct
   public void deduplicateMasterItems() {
      LOGGER.info("initialize synchronize integrated master items (KP concurrency: {}, prefetch: {})",
          keyProviderConcurrency, keyProviderPrefetch);

      // listen
      subscription = masterItemReceiver.receive()
          .map(this::extractMasterItem)
          .flatMap(m -> bobControllerClient.readAssortmentState(m), keyProviderConcurrency, keyProviderPrefetch)
          // discard error items
          .filter(item -> item.state != ItemState.ERROR)
          // create a flat flux of items with respective sender
          .flatMap(item -> /*item.state */ItemState.ERROR.sender.apply(this).map(sender -> Tuples.of(sender, item)))
          .groupBy(Tuple2::getT1, Tuple2::getT2)
          .flatMap(flux -> flux.key().sendAll(flux))
          .subscribe(this::success, Deduplicator::logError);
   }

   private static void logError(Throwable throwable) {

   }

   private void success(Object o) {

   }

//2. extragem masterItem-ul din record-ul de kafka

   /**
    * directly ack the kafka item and convert from json.
    */
   MasterItem extractMasterItem(ReceiverRecord<UUID, MasterItem> record) {
      record.receiverOffset().acknowledge();
      var masterItem = record.value();
      LOGGER.info("{}, {}: received master item, total count: {}",
          masterItem.getMasterKey(), masterItem.getAttempt(), totalRecordCount.incrementAndGet());
      return masterItem;
   }


   private Object urlParams(Object gtin) {
      return null;
   }


}
