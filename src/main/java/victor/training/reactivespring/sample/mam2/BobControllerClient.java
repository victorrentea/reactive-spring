package victor.training.reactivespring.sample.mam2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import victor.training.reactivespring.sample.mam1.MasterItem;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class BobControllerClient {
   private static final Logger LOGGER = LoggerFactory.getLogger(BobControllerClient.class);
   private String keyProviderUrl;
   private String keyProviderCountry;
   private AtomicInteger requestCount;
   private String keyProviderUser;
   private String keyProviderPass;
   private Retry retry;


   //3. se verifica existenta item-ului intr-un proiect extern
   public Mono<DeduplicatorItem> readAssortmentState(MasterItem masterItem) {
      return createWebClient(keyProviderUrl, keyProviderUser, keyProviderPass)
          .get()
          .uri("BOB_RESOURCE", urlParams(masterItem.getGtin()))
          .accept(MediaType.APPLICATION_JSON)
          .exchangeToMono(response -> mapGetResponse(masterItem, response))
          .retryWhen(retry)
          .onErrorResume(ex -> ex instanceof RetryExhaustedException, ex -> {
             return Mono.just(DeduplicatorItem.MONITOR);
          })
          .onErrorResume(ex -> {
             return Mono.empty(); // discard error items
          });
//      catch (RetryExhaustedException e) {
//         return MONITOR;
//      } catch (Exception e) {
//         return null;
//      }
   }

   private Mono<DeduplicatorItem> mapGetResponse(MasterItem masterItem, ClientResponse response) {
      return null;
   }

   private WebClient createWebClient(String keyProviderUrl, String keyProviderUser, String keyProviderPass) {
      return null;
   }

   private Object urlParams(Object gtin) {
      return null;
   }
}
