package victor.training.reactivespring.sample.mam3;

import org.apache.commons.collections4.MultiSet;
import org.apache.commons.collections4.multiset.HashMultiSet;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SampleMAM3 {
   private static final Logger LOGGER = LoggerFactory.getLogger(SampleMAM3.class);
   private CollectorClient collectorClient;
   private MiraklClient miraklClient;


   //   Primim o lista de id-uri pentru care dorim informatii din doua sisteme


   public static void main(String[] args) {

      MultiSet<String> miraklIds = new HashMultiSet<>();
      miraklIds.addAll(Arrays.asList("abb","b","abb","c","b","abb"));
      for (String miraklId : miraklIds) {
         System.out.println(miraklId);
      }
   }
   /**
    * <p>Detailed item info by mirakl id endpoint. Example: http://localhost:8290/swagger-ui.html#/item-info-controller/detailedItemInfoByMiraklIdUsingPOST</p>
    * <p>Being given a list of mirakl ids, the tracking ids will be extracted in order to make CM23 GET requests on MIRAKL.</p>
    * <p>The information obtained from MIRAKL will be compounded with the information from database.</p>
    */
//   @DetailedItemInfoByMiraklIdSwagger
   @PostMapping(value = "DETAILED_ITEM_INFO_BY_MIRAKL_ID_RESOURCE", consumes = MediaType.APPLICATION_JSON_VALUE)
   public final Mono<ResponseEntity<ItemInfoResponse>> detailedItemInfoByMiraklId(@RequestBody(required = false) List<String> miraklIdsList) {
      LOGGER.info("GET_INFO_MESSAGE" +  miraklIdsList); //
      HashSet<String> itemsOnError = new HashSet<String>();
      MultiSet<String> miraklIds = new HashMultiSet<>();// TODO victor similar to sorted ? or sorted by hashCode()? De ce ?
      miraklIds.addAll(miraklIdsList);


//    TODO victor  Stream.of(miraklIds).map(UUID::fromString).collect(Collectors.toList()); +
      //+ Mono.defer(() -> retrieveCollectorItems)

      // TODO victor fragment in variables
      return Flux.fromIterable(miraklIdsList)
          .map(UUID::fromString)
          .collectList()
          .flatMap(collectorClient::retrieveCollectorItems)
          .flatMapMany(Flux::fromIterable)
          .filter(collectorResponse -> trackingIdNotNull(itemsOnError, collectorResponse))
          .map(CollectorResponse::getTrackingId)
          // TODO victor break here
          .distinct()
          .flatMap(miraklClient::getMiraklSyncDetails)
          .flatMap(miraklSuccessResponse -> Flux.fromIterable(miraklSuccessResponse.getProcessedItems()))
          .filter(processedItem -> miraklIds.contains(processedItem.getMiraklId()))
          .flatMap(processedItem -> pairInfo(processedItem, miraklIds))
          .map(dataPair -> mapDetailedItemInfo(dataPair.getT1(), dataPair.getT2()))
          .collectList()
          .flatMap(itemsWithInfo -> generateResponse(itemsWithInfo, miraklIds, itemsOnError, null))
          // TODO victor why ResponseEntity?
          .onErrorContinue((ex, value) -> {
             LOGGER.error("ERROR_MESSAGE", ex.getMessage());
             itemsOnError.add(value.toString());
          });
   }

   private Mono<ResponseEntity<ItemInfoResponse>> generateResponse(List<DetailedItemInfo> itemsWithInfo, MultiSet<String> miraklIds, HashSet<String> itemsOnError, Object o) {
      return null;
   }

   private DetailedItemInfo mapDetailedItemInfo(Object t1, Object t2) {
      return null;
   }

   private Publisher<Tuple2<MiraklProcessedItem, MultiSet<String>>> pairInfo(MiraklProcessedItem processedItem, MultiSet<String> miraklIds) {
      return Mono.just(Tuples.of(processedItem, miraklIds));
   }

   private boolean trackingIdNotNull(HashSet<String> itemsOnError, CollectorResponse collectorResponse) {
      return false;
   }

}
