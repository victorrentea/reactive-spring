package victor.training.reactivespring.sample.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.stream.Collectors;

import static victor.training.reactivespring.sample.jobs.WmsMode.ADVANCED_MODE;

//@RestController
//@RequestMapping("/locations/{locationId}/retur")
@Slf4j
@RequiredArgsConstructor
class ReturnsResource {

   private static final String LOCATION_ID = "locationId";

   private final ReturnsService returnsService;

   @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
   public Mono<ResponseEntity<Void>> processReturn(
       @PathVariable(LOCATION_ID) final LocationId locationId,
       @RequestBody final ReturnRequest returnRequest,
       final BettyRequestContext bettyRequestContext) {
      return Mono.defer(() -> {

         return returnsService.processReturn(locationId, returnRequest, bettyRequestContext)
             .doOnError(error ->
                 log.error("Process return failed for location {} and returnId {}",
                     locationId,
                     returnRequest.getId(),
                     error
                 )
             )
             .map(ResponseEntity::ok);
      });
   }
}

//   SERVICE
@Slf4j
class ReturnsService {

   private ReturnItemService returnItemService;
   private ReturnDetailsMapper returnDetailsMapper;
   private ReturnsRepository returnsRepository;
   private StockService stockService;

   public Mono<Void> processReturn(final LocationId locationId, final ReturnRequest returnRequest, final BettyRequestContext requestContext) {
      if (isReturnNotAllowedForLocation(locationId)) {
         return Mono.empty();
      }

      ReturnDetails returnDetails = returnDetailsMapper.toReturnDetails(returnRequest, locationId);

      return returnsRepository.findByKeyLocationIdAndKeyId(locationId, returnDetails.getKey().getId()).next()
              .flatMap(this::logReturnAlreadyExists)
              .switchIfEmpty(processReturnDetails(locationId, returnRequest, returnDetails, requestContext))
              .then();
   }

   private <R> Mono<ReturnDetails> logReturnAlreadyExists(ReturnDetails aReturn) {
      return null;
   }

   private boolean isReturnNotAllowedForLocation(LocationId locationId) {
      return false;
   }

   JobsService jobsService;

   private Mono<ReturnDetails> processReturnDetails(final LocationId locationId, final ReturnRequest returnRequest,
                                                    final ReturnDetails returnDetails, final BettyRequestContext requestContext) {
//      DO STUFF NOW ON METHOD CALL
      return returnsRepository.save(returnDetails)
          .doOnSuccess(success -> log.info("Saved return details for locationId={} and returnId={}", locationId, returnRequest.getId()))
          .then(groupReturnRequestItems(returnRequest.getReturnItems()))
          .flatMapMany(returnRequestItems -> returnItemService.createReturnItemContext(locationId, returnRequest, returnRequestItems))
          .flatMap(returnItemContext -> stockService.uploadCorrectionDetails(returnItemContext.getStockCorrectionDetails())
              .then(stockService.createStock(returnItemContext)))
          .collectList()
          .flatMap(stockDetailsList -> jobsService.createReturnJob(locationId, stockDetailsList, returnRequest))
          .thenReturn(returnDetails);
   }


   private Mono<List<ReturnRequestItem>> groupReturnRequestItems(final List<ReturnRequestItem> returnItems) {
      return Flux.fromIterable(returnItems)
          .groupBy(ReturnRequestItem::getBettyBundleId)
          .flatMap(groupedReturnRequestItem -> groupedReturnRequestItem.reduce(this::sumQuantity))
          .collectList();
   }

   private ReturnRequestItem sumQuantity(ReturnRequestItem returnRequestItem, ReturnRequestItem returnRequestItem1) {
      return new ReturnRequestItem();
   }
}

class ReturnItemService {
   private StockService stockService;
   private ArticleSearchServiceClient articleSearchServiceClient;

   public Flux<ReturnItemContext> createReturnItemContext(final LocationId locationId, final ReturnRequest returnRequest,
                                                          final Collection<ReturnRequestItem> returnRequestItems) {

//
//      // Tuple solution
//      Mono<Tuple3<Map<String, ArticleInformation>, Map<String, StorageGroup>, StockCorrectionType>> tuple3Mono =
//          getArticleInfo(locationId, returnRequestItems)
//          .flatMap(articleInfo -> getStorageGroups(locationId, returnRequestItems, articleInfo).map(storage -> Tuples.of(articleInfo, storage)))
//          .flatMap(tuple2 -> stockService.getStockCorrectionType(locationId).map(stock -> Tuples.of(tuple2.getT1(), tuple2.getT2(), stock)));
//
//
//      tuple3Mono.flatMapMany(tuple3 -> getSsccsRequestItemsTuple(locationId, returnRequestItems)
//                  .flatMap(ssccRequestItemTuple -> buildReturnItemContext(locationId, returnRequest, tuple3.getT3(),
//                      tuple3.getT1(), tuple3.getT2(), ssccRequestItemTuple)
//              ));

      Mono<StockCorrectionType> stockMono = stockService.getStockCorrectionType(locationId);

      // Tuple solution
      Mono<Map<String, ArticleInformation>> articleInfoMono = getArticleInfo(locationId, returnRequestItems);

      Mono<Map<String, StorageGroup>> storageMono = articleInfoMono.flatMap(articleInfo -> getStorageGroups(locationId, returnRequestItems, articleInfo));

      return articleInfoMono.zipWhen(articleInfo -> getStorageGroups(locationId, returnRequestItems, articleInfo))
          .flatMap(TupleUtils.function((articleInfo, storage) -> stockService.getStockCorrectionType(locationId)
              .map(stock -> Tuples.of(articleInfo, storage, stock))))

          .flatMapMany(TupleUtils.function((article, storage, stock) ->
              getSsccsRequestItemsTuple(locationId, returnRequestItems)
                  .flatMap(ssccRequestItemTuple -> buildReturnItemContext(locationId, returnRequest, stock, article, storage, ssccRequestItemTuple))));


//      return getSsccsRequestItemsTuple(locationId, returnRequestItems)
//          .flatMap(ssccRequestItemTuple ->
//              stockMono.flatMap(stock ->
//                  articleInfoMono.flatMap(articleInfo ->
//                      storageMono.flatMap(storage ->
//                          buildReturnItemContext(locationId, returnRequest, stock,
//                          articleInfo, storage, ssccRequestItemTuple)
//                      )
//                  )
//              )
//          );


//      return stockService.getStockCorrectionType(locationId)
//          .flatMapMany(stockCorrectionType -> getArticleInfoAndStorageGroupsTuple(locationId, returnRequestItems)
//              .flatMapMany(articleInfosAndStorageGroupsTuple -> getSsccsRequestItemsTuple(locationId, returnRequestItems)
//                  .flatMap(ssccRequestItemTuple -> buildReturnItemContext(locationId, returnRequest, stockCorrectionType,
//                      articleInfosAndStorageGroupsTuple, ssccRequestItemTuple)
//                  )
//              ));

   }


   private Mono<ReturnItemContext> buildReturnItemContext(LocationId locationId, ReturnRequest returnRequest, Object stockCorrectionType,
                                                          Map<String, ArticleInformation> articleInformation, Map<String, StorageGroup> storageGroupsTuple, Tuple2<String, ReturnRequestItem> ssccRequestItemTuple) {
      return null;
   }


   private Mono<Map<String, ArticleInformation>> getArticleInfo(final LocationId locationId, final Collection<ReturnRequestItem> returnRequestItems) {
      Set<Long> idSet = returnRequestItems.stream().map(ReturnRequestItem::getBettyBundleId).collect(Collectors.toSet());
      return articleSearchServiceClient.getBettyBundleIdArticleInformationMappings(locationId, idSet);
   }

   private Mono<Map<String, StorageGroup>> getStorageGroups(final LocationId locationId,
                                                            final Collection<ReturnRequestItem> returnRequestItems,
                                                            Map<String, ArticleInformation> articleInfoMappings) {

      return Flux.fromIterable(articleInfoMappings.values())
          .map(ArticleInformation::getMgbBundleId)
          .collect(Collectors.toSet())
          .flatMap(mgbBundleIds -> stockService.getStorageGroupMappingsFor(locationId, mgbBundleIds))
          ;
   }

   private Mono<Tuple2<Map<String, ArticleInformation>, Map<String, StorageGroup>>> getArticleInfoAndStorageGroupsTuple(
       final LocationId locationId, final Collection<ReturnRequestItem> returnRequestItems) {

      Set<Long> idSet = returnRequestItems.stream().map(ReturnRequestItem::getBettyBundleId).collect(Collectors.toSet());

      return articleSearchServiceClient.getBettyBundleIdArticleInformationMappings(locationId, idSet)
//          .flatMap(articleInfoMappings -> Flux.fromIterable(articleInfoMappings.values())
//              .map(ArticleInformation::getMgbBundleId)
//              .collect(Collectors.toSet())
//              .flatMap(mgbBundleIds -> stockService.getStorageGroupMappingsFor(locationId, mgbBundleIds))
//              .map(x-> Tuples.of(articleInfoMappings,x))
//          );
          .zipWhen(articleInfoMappings -> Flux.fromIterable(articleInfoMappings.values())
              .map(ArticleInformation::getMgbBundleId)
              .collect(Collectors.toSet())
              .flatMap(mgbBundleIds -> stockService.getStorageGroupMappingsFor(locationId, mgbBundleIds))
          );
   }

   private Flux<Tuple2<String, ReturnRequestItem>> getSsccsRequestItemsTuple(final LocationId locationId,
                                                                             final Collection<ReturnRequestItem> returnRequestItems) {
      return stockService.generateMultipleSsccs(locationId, returnRequestItems.size())
          .zipWith(Flux.fromIterable(returnRequestItems));
   }

}

class StockService {

   RequestContextService requestContextService;
   private AdvStockLocationServiceClient advStockLocationServiceClient;
   private StockRequestConverter stockRequestConverter;

   public Mono<Void> uploadStockCorrection(final StockCorrectionDetails stockCorrectionDetails) {
      return requestContextService.obtainBettyRequestContext()
          .flatMap(requestContext -> getUserId(stockCorrectionDetails, requestContext))
          .flatMap(userId -> toStockCorrectionContentHelper(stockCorrectionDetails, userId)
              .filter(StockCorrectionContentHelper::hasValidCorrectionQuantity)
              .delayUntil(helper -> uploadStockCorrectionToMMS(stockCorrectionDetails, helper))
              .flatMap(helper -> sendStockCorrectionToAnalytics(stockCorrectionDetails, helper, userId)
              )
          );

   }

   private Mono<Void> sendStockCorrectionToAnalytics(StockCorrectionDetails stockCorrectionDetails, Object stockCorrectionContentHelper, Object userId) {
      return Mono.fromRunnable(() -> {
         // block
         //      sleep RestTemplate.getForo

      });
   }

   private Mono<Void> uploadStockCorrectionToMMS(StockCorrectionDetails stockCorrectionDetails, StockCorrectionContentHelper stockCorrectionContentHelper) {
      return Mono.empty();
   }

   private Mono<StockCorrectionContentHelper> toStockCorrectionContentHelper(StockCorrectionDetails stockCorrectionDetails, String userId) {
      return null;
   }

   private Mono<String> getUserId(StockCorrectionDetails stockCorrectionDetails, Object requestContext) {
      return null;
   }


   public Mono<StockDetails> createStock(final ReturnItemContext returnItemContext) {

      if (returnItemContext.getStockCorrectionDetails().getStockUpdateDetails().getWmsMode() != ADVANCED_MODE) {
         return Mono.empty();
      }
      return advStockLocationServiceClient.createStock(returnItemContext.getLocationId(), returnItemContext.getCreateStockTransactionRequest())
          .thenReturn(stockRequestConverter.toStockDetails(returnItemContext));
   }

   public Mono<StockCorrectionType> getStockCorrectionType(LocationId locationId) {
      return null;
   }

   public Mono<Void> uploadCorrectionDetails(StockCorrectionDetails stockCorrectionDetails) {
      return null;
   }

   public Mono<Map<String, StorageGroup>> getStorageGroupMappingsFor(LocationId locationId, Set<String> mgbBundleIds) {
      return null;
   }

   public Flux<String> generateMultipleSsccs(LocationId locationId, int size) {
      return null;
   }
}

class JobsService {

   private ConfigurationsService configurationsService;

   public Mono<ReturnJob> createReturnJob(final LocationId locationId, final List<StockDetails> stockDetails,
                                          final ReturnRequest returnRequest) {

      return configurationsService.isQuantityJobEnabled(locationId)
          .flatMap(isEnabled -> isEnabled ?
              createReturnQuantityJob(locationId, stockDetails, returnRequest) :
              createQAJob(locationId, stockDetails, returnRequest));
   }

   private Mono<ReturnJob> createQAJob(LocationId locationId, List<StockDetails> stockDetails, ReturnRequest returnRequest) {
      return null;
   }

   private Mono<ReturnJob> createReturnQuantityJob(LocationId locationId, List<StockDetails> stockDetails, ReturnRequest returnRequest) {
      return null;
   }

}
