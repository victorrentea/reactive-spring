package victor.training.reactivespring.r2dbc;

import com.datastax.oss.driver.shaded.guava.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.xml.ws.ServiceMode;
import java.util.Random;
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
      return otherService.m(name)
         .then(userRepository.save(new User(null)));
   }
private final OtherService otherService;


}
@Service
@RequiredArgsConstructor
class OtherService {
   private final UserRepository userRepository;
   public Mono<User> m(String name) {
      return userRepository.save(new User(name));
   }
}


