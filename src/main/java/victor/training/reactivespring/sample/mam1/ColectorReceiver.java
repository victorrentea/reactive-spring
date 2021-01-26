package victor.training.reactivespring.sample.mam1;

import reactor.core.publisher.Flux;

import java.util.UUID;

public class ColectorReceiver {

   public Flux<ReceiverRecord<UUID, MasterItem>> receive() {
      return null;// KafkaReceiver.create(receiverOptions)    .receive();
   }
}
