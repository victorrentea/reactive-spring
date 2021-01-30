package victor.training.reactivespring.sample.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/locations/{locationId}/retur")
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

      // TODO victor .defer( () ->  ce avea?
      return Mono.just(returnDetailsMapper.toReturnDetails(returnRequest, locationId))
          .flatMap(returnDetails -> returnsRepository.findByKeyLocationIdAndKeyId(locationId, returnDetails.getKey().getId()).next()
              .flatMap(this::logReturnAlreadyExists)
              // TODO victor:
              .switchIfEmpty(Mono.defer(() -> processReturnDetails(locationId, returnRequest, returnDetails, requestContext)))
              .then());
   }

   private <R> Mono<ReturnDetails> logReturnAlreadyExists(ReturnDetails aReturn) {
      return null;
   }

   private boolean isReturnNotAllowedForLocation(LocationId locationId) {
      return false;
   }


   private Mono<ReturnDetails> processReturnDetails(final LocationId locationId, final ReturnRequest returnRequest,
                                                    final ReturnDetails returnDetails, final BettyRequestContext requestContext) {
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

   public Flux<ReturnItemContext> createReturnItemContext(final LocationId locationId, final ReturnRequest returnRequest,
                                                          final Collection<ReturnRequestItem> returnRequestItems) {

      return stockService.getStockCorrectionType(locationId)
          .flatMapMany(stockCorrectionType -> getArticleInfoAndStorageGroupsTuple(locationId, returnRequestItems)
              .flatMapMany(articleInfosAndStorageGroupsTuple -> getSsccsRequestItemsTuple(locationId, returnRequestItems)
                  .flatMap(ssccRequestItemTuple -> buildReturnItemContext(locationId, returnRequest, stockCorrectionType,
                      articleInfosAndStorageGroupsTuple, ssccRequestItemTuple)
                  )
              ));
   }


   private Mono<Tuple2<Map<String, ArticleInformation>, Map<String, StorageGroup>>> getArticleInfoAndStorageGroupsTuple(
       final LocationId locationId, final Iterable<ReturnRequestItem> returnRequestItems) {

      return Flux.fromIterable(returnRequestItems)
          .map(ReturnRequestItem::getBettyBundleId)
          .collect(Collectors.toSet())
          .flatMap(bettyBundleIds ->
              articleSearchServiceClient.getBettyBundleIdArticleInformationMappings(locationId, bettyBundleIds))
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

   public Mono<Void> uploadStockCorrection(final StockCorrectionDetails stockCorrectionDetails) {
      return requestContextService.obtainBettyRequestContext()
          .flatMap(requestContext -> getUserId(stockCorrectionDetails, requestContext))
          .flatMap(userId -> toStockCorrectionContentHelper(stockCorrectionDetails, userId)
              .filter(StockCorrectionContentHelper::hasValidCorrectionQuantity)
              .flatMap(stockCorrectionContentHelper ->
                  uploadStockCorrectionToMMS(stockCorrectionDetails, stockCorrectionContentHelper)
                      .then(Mono.defer(
                          () -> sendStockCorrectionToAnalytics(stockCorrectionDetails, stockCorrectionContentHelper, userId))
                      )
              )
          );
   }


   public Mono<StockDetails> createStock(final ReturnItemContext returnItemContext) {
      return Mono.just(returnItemContext.getStockCorrectionDetails().getStockUpdateDetails())
          .map(StockUpdateDetails::getWmsMode)
          .flatMap(wmsMode -> WmsMode.ADVANCED_MODE.equals(wmsMode) ?
              advStockLocationServiceClient.createStock(returnItemContext.getLocationId(),
                  returnItemContext.getCreateStockTransactionRequest()) :
              Mono.empty())
          .thenReturn(stockRequestConverter.toStockDetails(returnItemContext));
   }

   public Mono<Object> getStockCorrectionType(LocationId locationId) {
      return null;
   }
}

class JobsService {

   public Mono<ReturnJob> createReturnJob(final LocationId locationId, final List<StockDetails> stockDetails,
                                          final ReturnRequest returnRequest) {

      return configurationsService.isQuantityJobEnabled(locationId).flatMap(
          isEnabled -> isEnabled ?
              createReturnQuantityJob(locationId, stockDetails, returnRequest) :
              createQAJob(locationId, stockDetails, returnRequest));
   }

}
