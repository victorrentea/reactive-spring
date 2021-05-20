package victor.training.reactive.spring.mongo;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Configuration
@EnableMongoRepositories
@EnableReactiveMongoRepositories
public class MongoConfig {

   private final MongoOperations db;

      @PostConstruct
   public void setupDb() {
      db.createCollection(Event.class, CollectionOptions.empty()
          .capped()
          .size(1024)
          .maxDocuments(5));
      System.out.println("CREATED");
   }
}
