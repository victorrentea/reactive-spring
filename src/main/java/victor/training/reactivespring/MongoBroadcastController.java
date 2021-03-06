package victor.training.reactivespring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactivespring.MongoBroadcastController.LogMessage;
import victor.training.reactivespring.MongoBroadcastController.LogMessage.Level;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class MongoBroadcastController {
   private final LogMessageRepo rxRepo;
   private final LogMessageRepoCrud repo;
   private final MongoOperations db;

//   @PostConstruct
   public void setupDb() {
      db.createCollection(LogMessage.class, CollectionOptions.empty()
          .capped()
          .size(1024)
          .maxDocuments(5));
      System.out.println("CREATED");
   }

   @GetMapping(value = "log/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public Flux<ServerSentEvent<String>> messageStream() {
      return rxRepo.findAllByIdNotNull()
          .map(LogMessage::getValue)
          .map(b -> ServerSentEvent.builder(b).build());
   }

   @GetMapping(value = "list/list")
   public Flux<String> messageList() {
      return rxRepo.findAll().map(Objects::toString);
   }
   @GetMapping(value = "log/list2")
   public Mono<String> messageList2() {
      return rxRepo.findAll().collectList().map(Objects::toString);
   }

   @GetMapping(value = "log/list-imper")
   public Mono<String> messageListImperative() {
      List<LogMessage> result = new ArrayList<>();
      repo.findAll().forEach(result::add);
      return Mono.just(result.toString());
   }

   @GetMapping("log/send")
   public Mono<LogMessage> sendMessage(/*@PathVariable Level level*/) {
      LogMessage logMessage = new LogMessage();
//      logMessage.setLevel(level);
      logMessage.setValue("Aloha " + LocalDateTime.now());
      return rxRepo.save(logMessage);
   }

   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   @Document
   public static class LogMessage {
      enum Level {
         INFO, WARN, DEBUG
      }
      @Id
      private String id;
      private String value;
      private Level level;
   }

}
interface LogMessageRepoCrud extends CrudRepository<LogMessage, Long> {
//   @Tailable
//   @Query("SELECT * From Bid")
//   List<Bid> findAllTailable();
}
interface LogMessageRepo extends ReactiveCrudRepository<LogMessage, Long> {
   @Tailable
   Flux<LogMessage> findAllByIdNotNull();
//   @Tailable
//   Flux<LogMessage> findByLevel(Level level);

//   @Tailable
//   @Override
//   Flux<Bid> findAll();
}
