package victor.training.reactor.buffersend;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static java.time.Duration.ofMillis;

@RestController
@SpringBootApplication
@Slf4j
public class BufferSend {
   private Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();
   private static final Flux<Long> timeFlux = Flux.interval(ofMillis(100));

   @PostConstruct
   public void init() {
      sink.asFlux()
          .buffer(timeFlux.doOnNext(t -> log.debug("Tick " + t)))
          .doOnNext(page -> log.debug("Buffer " + page))
          .log()
          .flatMap(page -> sendPage(page)
//            .onErrorResume(throwable -> Mono.empty())
          )

          // spooky rau !
          .onErrorContinue(t -> t instanceof IllegalArgumentException, (t, e) -> log.error("Exceptie pe {} : {}",e,t))
          .subscribe()
          ;
      log.info("gata post contruct");
   }

   private Mono<Void> sendPage(List<String> page) {
      return Mono.fromRunnable(() -> {
         if (page.contains("Elem 0")) {
            log.error("BUM!");
            throw new IllegalArgumentException();
         }
         log.debug("Sending " + page);
      });
   }

   @GetMapping
   public void get(@RequestParam int i) {
      sink.tryEmitNext("Elem " + i);
   }

   public static void main(String[] args) {
      SpringApplication.run(BufferSend.class, args);
   }
}
