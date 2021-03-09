package victor.training.reactivespring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.*;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class TickerController {
   private ConnectableFlux<Long> hotFlux;

   @PostConstruct
   public void initHotFlux() {
      hotFlux = Flux.interval(Duration.ofSeconds(1)).publish();
      hotFlux.connect();
   }

   @GetMapping(value = "tick", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public Flux<String> tick() {
      return hotFlux.map(Objects::toString);
   }
}
