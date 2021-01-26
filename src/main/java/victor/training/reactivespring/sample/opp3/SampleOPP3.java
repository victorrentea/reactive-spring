package victor.training.reactivespring.sample.opp3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class SampleOPP3 {
   private static final Logger LOGGER = LoggerFactory.getLogger(SampleOPP3.class);
   private static final String DEFAULT_SSCC_PRINT_MARK = "S";
   private PickingTaskService pickingTaskService;
   private CancelPickingJobProducer cancelPickingJobProducer;
   private PickingTaskRepository pickingTaskRepository;

   public static void main(String[] args) {

      List<Integer> l = Flux.just("a", "b", "a")
          .flatMap(s -> {
             if (s == "a") {
                return Mono.just(1);
             } else {
                return Mono.error(new RuntimeException());
             }
          })
          .collectList()
          .block();
      System.out.println(l);
   }


   private Mono<List<PickingTask>> createPickingTasks(final LocationId locationId, final Collection<PickList> pickLists, final PickingJob pickingJob) {
      Function<CountryConfig, Mono<List<PickingTask>>> f = countryConfiguration -> {
         final PickListFilter pickListFilter = newPickListFilter(locationId);
         final PickingJobId pickingJobId = pickingJob.getId();

         return Flux.fromIterable(pickLists)
             .flatMap(pickList -> filterPickList(locationId, pickingJob, pickList, pickListFilter))
             // Mono in error might terminate the entire flow
             .map(filteredPickList -> mapToPickListAndPrintMarkTuple(filteredPickList, pickingJob))
             .collectList()
             // TODO victor collect varible here
             .filter(pickListsAndPrintMarks -> !pickListsAndPrintMarks.isEmpty())
             .flatMap(pickListsAndPrintMarks -> pickingTaskService.createSortedPickingTasks(pickListsAndPrintMarks, pickingJobId, countryConfiguration))
             .flatMap(pickingTaskService::savePickingTasks)

               // TODO victor collect here. Nu ar fi fost suficient ca repo sa returneze inapoi lista data sa fie persistenta? sau din DB poate veni o alta lista ?
             .thenMany(pickingTaskRepository.findAllByLocationIdAndPickingJobId(locationId, pickingJobId))
             .doOnNext(pt -> LOGGER.debug("Retrieved picking task {} for picking job {} in location {}", pt.getId(), pickingJobId, locationId))
             .switchIfEmpty(Mono.defer(() -> { // TODO switch if empty aici e necesar ca esti in fucntia de flow centrala, nu intr-un asincrona
                // if no picking task has been created then the job must be cancelled
                return cancelPickingJobProducer.publishCancelPickingJob(pickingJob, PickingJobCancelReason.NO_PICKINGTASK_CREATED_ON_JOB_START)
                    .doOnError(e -> LOGGER.error(e.getMessage(), e))
                    .onErrorResume(e -> Mono.empty())
                    .then(Mono.error(() ->
                        new JobStatusUpdateException(ProcessResult.UNRECOVERABLE_INCOMPLETE_DATA,
                            "Unable to create picking tasks for picking job {0} in location {1}: {2}",
                            pickingJob.getId(), locationId, PickingJobCancelReason.NO_PICKINGTASK_CREATED_ON_JOB_START.getDescription())));
             }))
             .collectList();
      };
      return findCountryConfigurationForLocation(locationId, pickingJob.getId())
          .flatMap(f);
   }

   private Mono<CountryConfig> findCountryConfigurationForLocation(LocationId locationId, PickingJobId id) {
      return null;
   }

   private PickListFilter newPickListFilter(LocationId locationId) {
      return null;
   }


   private Mono<PickList> filterPickList(final LocationId locationId, final PickingJob pickingJob, PickList pickList, PickListFilter pickListFilter) {
      return Mono.just(pickListFilter.filterPickList(pickList))
          .flatMap(( pickListAndItemsAlreadyPicked) -> {
             final Optional<PickList> filteredPickList = pickListAndItemsAlreadyPicked.getT1();
             final Boolean itemsAlreadyPicked = pickListAndItemsAlreadyPicked.getT2();

             return Mono.justOrEmpty(filteredPickList)
                 .switchIfEmpty(Mono.defer(() -> { // TODO victor if(isPresent) ce avea?
                    // if one of the job's pick list is filtered out, but no item is already picked, then the whole job must be cancelled
                    return publishCancelPickingJobIfNoItemsAlreadyPicked(itemsAlreadyPicked, pickingJob, PickingJobCancelReason.PICKLIST_FILTERED_OUT_ON_JOB_START)
                        .doOnError(e -> LOGGER.error(e.getMessage(), e))
                        .onErrorResume(e -> Mono.empty())
                        .then(Mono.error(() ->
                            new JobStatusUpdateException(ProcessResult.UNRECOVERABLE_INCOMPLETE_DATA,
                                "Unable to create picking tasks for picking job {0} based on picklist {1} in location {2}: {3}",
                                pickingJob.getId(), pickList.getId(), locationId, PickingJobCancelReason.PICKLIST_FILTERED_OUT_ON_JOB_START.getDescription())));
                 }));
          })
          .doOnError(e -> LOGGER.error(e.getMessage(), e));
   }
   private Mono<PickingJob> publishCancelPickingJobIfNoItemsAlreadyPicked(
       final Boolean itemsAlreadyPicked,
       final PickingJob pickingJob,
       final PickingJobCancelReason reason) {
      return Boolean.FALSE.equals(itemsAlreadyPicked)
          ? cancelPickingJobProducer.publishCancelPickingJob(pickingJob, reason)
          : Mono.empty();
   }

   //publish message to kafka
   public Mono<PickingJob> publishCancelPickingJob(final PickingJob pickingJob, final PickingJobCancelReason reason) {
      return publish(Tuples.of(pickingJob, reason)).thenReturn(pickingJob);
   }

   private <T> Mono<Void> publish(Tuple2<PickingJob, PickingJobCancelReason> of) {
      return null; // TODO kafka
   }

   private Tuple2<PickList, String> mapToPickListAndPrintMarkTuple(final PickList pickList, final PickingJob pickingJob) {
      final Optional<PickListSummary> pickListSummary = pickingJob.getData().retrievePickListSummaryByPickListId(pickList.getId());
      final String ssccPrintMark = pickListSummary.flatMap(PickListSummary::getSsccPrintMark).orElse(DEFAULT_SSCC_PRINT_MARK);
      return Tuples.of(pickList, ssccPrintMark);
   }

   public Mono<Void> savePickingTasks(final List<PickingTask> pickingTasks) {
      return Mono.defer(() ->
          Flux.fromIterable(pickingTasks)
              .index((index, pickingTask) -> Tuples.of(index + 1, pickingTask))
              .flatMap(indexedPickingTask -> {
                 final int index = indexedPickingTask.getT1().intValue();
                 final PickingTask pickingTask = indexedPickingTask.getT2();
                 pickingTask.getData().updatePickingOrderIndex(index);
                 LOGGER.info("saving picking task {} for picking job {}", pickingTask.getId(), pickingTask.getPickingJobId());
                 return pickingTaskRepository.insert(pickingTask)
                     .doOnError(error -> LOGGER.error("Inserting in Cassandra failed (location {}, picking job {}, picking task {})",
                         pickingTask.getLocationId(),
                         pickingTask.getPickingJobId(),
                         pickingTask.getId()/*,
                         error*/));
              })
              .then());
   }



}
