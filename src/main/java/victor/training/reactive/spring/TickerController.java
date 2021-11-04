package victor.training.reactive.spring;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.*;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Objects;

@RestController
public class TickerController {
   private ConnectableFlux<Long> hotFlux;
   private Flux<Long> coldFlux;

   @PostConstruct
   public void initHotFlux() {
      hotFlux = Flux.interval(Duration.ofSeconds(1)).publish();
      hotFlux.connect();

      coldFlux = Flux.interval(Duration.ofSeconds(1));
   }

   @GetMapping(value = "tick"/*, produces = MediaType.TEXT_EVENT_STREAM_VALUE*/)
   public Flux<String> tick() {
      return hotFlux.map(Objects::toString);
   }

   @GetMapping(value = "tick-cold", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public Flux<String> tickCold() {
      return coldFlux.map(Objects::toString);
   }
}
