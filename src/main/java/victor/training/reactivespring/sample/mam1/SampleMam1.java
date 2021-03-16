package victor.training.reactivespring.sample.mam1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

import javax.annotation.PostConstruct;
import java.util.UUID;

import static victor.training.reactivespring.sample.mam1.Analyser.hasInvalidContent;

public class SampleMam1 {
   private static final Logger LOGGER = LoggerFactory.getLogger(SampleMam1.class);
   private KafkaReceiver<UUID, MasterItem> collectorReceiver;
   private ItemStateHandler itemStateHandler;
   private Disposable disposable;
   private AnalyzerItemRepository repository;

//   1. Flux-ul in microserviciul Analyser
   @PostConstruct
   public void setUpFlux() {
      disposable = collectorReceiver.receive()
          .flatMap(this::determineState)
          .filterWhen(this::saveItem)
          .groupBy(item -> item.state)
          .flatMap(flux -> flux.key().sender.apply(itemStateHandler, flux))
          .subscribe(this::success, Analyser::logError);// TODO victor daca apare eroare fluxul moare - un retry() ?
   }

   private void success(UUID uuid) {

   }


//2. determineState se foloseste pe un record primit pe topicul de kafka. Se fac o parte din verificari.
//   In toate cazurile, se creaza un ItemWIthState.

   Mono<ItemWithState> determineState(ReceiverRecord<UUID, MasterItem> receiverRecord) {
      LOGGER.info("{}: received from collector", receiverRecord.key());
      receiverRecord.receiverOffset().acknowledge();

      MasterItem kafkaItem = receiverRecord.value();
      if(hasInvalidContent(kafkaItem, true)) {
         return Mono.just(new ItemWithState(kafkaItem, ItemState.INVALID_DATA));
      }
      // if there are no valid GTINs, numberOfGtins() returns 0.
      return kafkaItem.numberOfGtins() == 1 ? // TODO victor if()
          repository.findById(kafkaItem.getMasterKey())
              .map(analyserItem -> new ItemWithState(kafkaItem, analyserItem))
              // no response means NEW or MONITOR TODO victor explain in code?
              .switchIfEmpty(Mono.fromSupplier(() -> new ItemWithState(kafkaItem))) // TODO victor: .just
              .onErrorResume(ex -> { // todo victor a dmai devreme
                 int attempt = kafkaItem.getAttempt();
                 LOGGER.error("{}, {}: error finding item.", receiverRecord.key(), attempt, ex);
//                 MAMEvent.error(attempt, ex);
                 return Mono.empty();
              }) :
          // no (valid) GTIN or more than one distinct GTIN means ERROR
          Mono.just(new ItemWithState(kafkaItem, ItemState.ERROR)); // TODO victor factory method ?
   }

//2.1. Obiectul ItemWithState folosit mai sus, in determineState.




//2.2. Pentru cazul in care avem un item vechi, caruia ii facem update, vom folosi determineFor(KafkaItem, AnalyserItem) pentru a incerca sa iisetam statusul
//   Tot aici, in functie de predicatele pentru fiecare status, se executa o anumita metoda (in principiu trimiteri pe topicuri de kafka ca tre alte microservicii)

   /**
    * If the item is already known, it is in the state UPDATE, otherwise it is in
    * the NEW state.  If the GTIN has changed, it is rejected and its state is
    * REJECTED.  If the GTIN is missing, it is rejected and its state is ERROR.
    */


   //3. Salvarea in baza de date
   Mono<Boolean>  saveItem(ItemWithState item) {
      // here we exactly have one gtin
      MasterItem masterItem = item.kafkaItem;
      UUID masterKey = masterItem.getMasterKey();
      int attempt = masterItem.getAttempt();
      LOGGER.info("{}, {}: gtin = {} saving state = {}", masterKey, attempt, masterItem.getGtin(), item.state);
      return item.state == ItemState.NEW ? // monitor items are not saved, either.
          repository.save(item.persistentItem)
              .map(result -> Boolean.TRUE) // TODO victor de ce nu Mono<Void> - discutie. filterWhen le face secvential - aici se consuma secv mesajele
              .onErrorResume(ex -> {
                 LOGGER.error("{}, {}: error saving item.", masterKey, attempt, ex);
//                 MAMEvent.error(attempt, ex);
                 return Mono.empty(); // TODO victor parca mai corect suna just(false)
              }) :
          Mono.just(Boolean.TRUE);
   }

}
