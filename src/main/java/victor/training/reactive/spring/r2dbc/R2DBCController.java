package victor.training.reactive.spring.r2dbc;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;

@RestController
@RequestMapping("sql")
@RequiredArgsConstructor
public class R2DBCController {
   private final UserRepository userRepository;

   @GetMapping(value = "flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public Flux<String> flux() {
      return userRepository.findAll().map(User::getName);
   }

   @GetMapping("create")
   public Mono<User> create() {
      String id = UUID.randomUUID().toString();
      String name = "User " + now().format(ofPattern("mm:ss"));
      return userRepository.save(new User(name));
   }

   @Autowired
   ConnectionFactory connectionFactory;

   @PostConstruct
    public void method() {

//      ConnectionFactory connectionFactory = MySqlConnectionFactory.from(configuration);

//      Mono<Connection> connectionMono = Mono.from(connectionFactory.create());
//      Disposable subscription = connectionMono.flatMap(connection -> ((Mono<Void>) connection.beginTransaction())
//          .then(Mono.from(connection.createStatement("INSERT INTO person (first_name, last_name) VALUES ('who', 'how')").execute()))
//          .flatMap(result -> (Mono<Integer>) result.getRowsUpdated())
//          .thenMany(connection.createStatement("INSERT INTO person (birth, nickname, show_name) VALUES (?, ?name, ?name)")
//              .bind(0, LocalDateTime.of(2019, 6, 25, 12, 12, 12))
//              .bind("name", "Some one")
//              .add()
//              .bind(0, LocalDateTime.of(2009, 6, 25, 12, 12, 12))
//              .bind(1, "My Nickname")
//              .bind(2, "Naming show")
//              .returnGeneratedValues("generated_id")
//              .execute())
//          .flatMap(Result::getRowsUpdated)
//          .then((Mono<Void>) connection.commitTransaction())
//      ).subscribe();

//      subscription.dispose();
   }

}


