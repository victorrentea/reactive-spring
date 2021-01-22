package victor.training.reactivespring.sampleOPP3;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;

public class PickingTaskService {
   public Mono<List<PickingTask>> createSortedPickingTasks(List<Tuple2<PickList, String>> pickListsAndPrintMarks, PickingJobId pickingJobId, CountryConfig countryConfiguration) {
      return null;
   }

   public <R> Mono<Void> savePickingTasks(List<PickingTask> picklingTasks) {
      return null;
   }
}
