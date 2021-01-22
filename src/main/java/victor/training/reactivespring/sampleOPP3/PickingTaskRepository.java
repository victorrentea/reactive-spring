package victor.training.reactivespring.sampleOPP3;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PickingTaskRepository {
   public Publisher<PickingTask> findAllByLocationIdAndPickingJobId(LocationId locationId, PickingJobId pickingJobId) {
      return null;
   }

   public Mono<Long> insert(PickingTask pickingTask) {
      return null;
   }
}
