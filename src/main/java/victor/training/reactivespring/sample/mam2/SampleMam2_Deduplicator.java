package victor.training.reactivespring.sample.mam2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import victor.training.reactivespring.sample.mam1.MasterItem;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class SampleMam2_Deduplicator
{
   private static final Logger LOGGER = LoggerFactory.getLogger(SampleMam2_Deduplicator.class);
   public BaseItemSender<DeduplicatorItem, ?> rejectedMasterItemSender;
   public BaseItemSender<DeduplicatorItem, ?> masterItemSender;
   private int keyProviderConcurrency;
   private KafkaReceiver<UUID, MasterItem> masterItemReceiver;
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
      LOGGER.info("initialize synchronize integrated master items (KP concurrency: {}, prefetch: {})", keyProviderConcurrency, keyProviderPrefetch);

      subscription = masterItemReceiver.receive()
          .map(this::extractMasterItem)
          .flatMap(m -> bobControllerClient.readAssortmentState(m), keyProviderConcurrency, keyProviderPrefetch)
          // discard error items
          .filter(item -> item.state != ItemState.ERROR)
          // create a flat flux of items with respective sender
          .flatMap(item -> item.state.sender.apply(this).map(sender -> Tuples.of(sender, item)))
          // VICTOR unele item-uri sunt trimise pe doi senderi
          // TODO Victor in loc sa folosim enum-ul pentru a tine logica, nu o putem pune in DeduplicatorItem.get
          .groupBy(Tuple2::getT1, Tuple2::getT2)
          // Grouping key is a BaseItemSender

          .flatMap(flux -> flux.key().sendAll(flux))
          .subscribe(this::success, SampleMam2_Deduplicator::logError);
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
      MasterItem masterItem = record.value();
      LOGGER.info("{}, {}: received master item, total count: {}",
          masterItem.getMasterKey(), masterItem.getAttempt(), totalRecordCount.incrementAndGet());
      return masterItem;
   }


   private Object urlParams(Object gtin) {
      return null;
   }


}
