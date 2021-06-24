package victor.training.reactive.spring.r2dbc;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
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
   @Transactional
   public Mono<User> create() {
      String id = UUID.randomUUID().toString();
      String name = "User " + now().format(ofPattern("mm:ss"));
      return  userRepository.save(new User(name))
          .subscribeOn(Schedulers.parallel())
//          .delayElement(Duration.ofDays(1))
          .then(userRepository.save(new User("x".repeat(1000))))
          .subscribeOn(Schedulers.boundedElastic());
   }

}


