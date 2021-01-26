package victor.training.reactivespring.sample.mam2;

import reactor.core.publisher.Flux;
import victor.training.reactivespring.sample.mam1.MasterItem;
import victor.training.reactivespring.sample.mam1.ReceiverRecord;

import java.util.Optional;
import java.util.UUID;

public class MasterItemReceiver {
   public Flux<ReceiverRecord<UUID, MasterItem>> receive() {
      return null;
   }
}
