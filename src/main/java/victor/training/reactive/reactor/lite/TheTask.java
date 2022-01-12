package victor.training.reactive.reactor.lite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.One;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

class ResponseOnQueue {

   Integer requestId;
}
interface RabbitReceiver {
   Flux<ResponseOnQueue> consumeAutoAck(String queue);
}
interface WaitingRepliesRxRepo {
   Mono<OrderRequest> findByRequestId(Integer requestId);
}
class OrderRequest {
   Integer requestId;

}
@RestController
public class TheTask {@Autowired
   RabbitReceiver rabbitReceiver;

//   @Autowired
//   WaitingRepliesRxRepo waitingRepliesRxRepo;



   @PostConstruct
   public void startListeningOnRabbit() {
      rabbitReceiver
          .consumeAutoAck("reply-queue")
//          .flatMap(message -> {
//             return waitingRepliesRxRepo.findByRequestId(message.requestId)
//          })
          .doOnNext(request -> {
             pendingConnections.get(request.requestId).tryEmitValue(request);
          })
          .doOnNext(message -> rabbitFlux.tryEmitNext(message))
          .subscribe(); // TODO error reporting
   }

   private Sinks.Many<ResponseOnQueue> rabbitFlux = Sinks.many().multicast().onBackpressureBuffer();


   Map<Integer, One<ResponseOnQueue>> pendingConnections = new HashMap<>(); // careful with memory. You need to remove from the map

   // a watchdog that take OLD elements from map and emits an erorr

   @PostMapping
   Mono<ResponseOnQueue> createOrder(String data) {
//      pendingConnections.get(1).emi

      Integer requestId = 1;
//      One<ResponseOnQueue> oneShotPublisher = Sinks.one();
//      pendingConnections.put(1, oneShotPublisher);


      Mono<Void> webRequestMono = WebClient.create().post().uri().retrieve().bodyToMono().then();

      Flux<ResponseOnQueue> sharedRabbitFlux = rabbitFlux.asFlux();
      return webRequestMono
          .then(sharedRabbitFlux.filter(message -> message.requestId == requestId).next());

//      webRequestMono.zip

//      return oneShotPublisher.asMono();
   }

   private Mono<ResponseOnQueue> waitForMessageOnQueue() {

   }
}
