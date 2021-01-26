package victor.training.reactivespring.sample.mam2;

import org.reactivestreams.Publisher;
import reactor.core.publisher.GroupedFlux;

public class BaseItemSender<T, T1> {
   public Publisher<?> sendAll(GroupedFlux<? extends BaseItemSender<DeduplicatorItem, ?>, T> flux) {
      return null;
   }
}
