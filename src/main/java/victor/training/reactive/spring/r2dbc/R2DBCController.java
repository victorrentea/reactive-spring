package victor.training.reactive.spring.r2dbc;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
   public Mono<String> create() {
      String name = "User " + now().format(ofPattern("mm:ss"));
      Mono<User> save1 = userRepository.save(new User(name));
      Mono<User> save2 = Mono.defer(() ->userRepository.save(null));
//      return save1.zipWith(save2, (u1, u2) -> u1.getId() + "," + u2.getId());
      return save1.doOnNext(u1 -> System.out.println("Persisted : "  + u1.getId()))
          .flatMap(u1 -> save2)
          .map(user -> user.getId() + "");
   }

}


