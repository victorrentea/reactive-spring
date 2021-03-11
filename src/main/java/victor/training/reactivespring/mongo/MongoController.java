package victor.training.reactivespring.mongo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequiredArgsConstructor
@RequestMapping("mongo")
public class MongoController {
   private final EventReactiveRepo rxRepo;
   private final EventBlockingRepo blockingRepo;

   @GetMapping("create")
   public Mono<Event> sendMessage() {
      Event event = new Event();
      event.setValue("Aloha " + LocalDateTime.now());
      return rxRepo.save(event);
   }


   @GetMapping(value = "flux-live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public Flux<ServerSentEvent<String>> messageStream() {
      return rxRepo.findAllByValueNotNull()
          .map(Event::getValue)
          .map(b -> ServerSentEvent.builder(b).build());
   }

   @GetMapping(value = "flux")
   public Flux<Event> flux() {
      return rxRepo.findAllByValueNotNull();
   }

   @GetMapping(value = "list-flux")
   public Mono<List<Event>> mono() {
      return rxRepo.findAll().collectList();
   }

   @GetMapping(value = "list-blocking")
   public Mono<List<Event>> list() {
      return Mono.defer(() -> Mono.just(blockingRepo.findAll()))
          .subscribeOn(Schedulers.boundedElastic())
          .map(this::toList);
   }

   private List<Event> toList(Iterable<Event> iterable) {
      return StreamSupport.stream(iterable.spliterator(), false)
          .collect(Collectors.toList());
   }
}

