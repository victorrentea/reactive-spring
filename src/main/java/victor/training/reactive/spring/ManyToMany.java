package victor.training.reactive.spring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import victor.training.reactive.intro.ThreadUtils;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static java.time.Duration.ofMillis;

public class ManyToMany {


   public static void main(String[] args) {
      Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

      Flux.interval(ofMillis(10)).subscribe(t -> sink.tryEmitNext("1"));
      Flux.interval(ofMillis(10)).subscribe(t -> sink.tryEmitNext("a"));

      Pattern pattern = Pattern.compile("\\d+");
      sink.asFlux()
          .groupBy(s -> pattern.matcher(s).matches())
          .flatMap(many -> many.key() ?
              many.buffer(10).flatMap(ManyToMany::sendNumberData) :
               many.buffer(10).flatMap(ManyToMany::sendAlfaData)
              )
          .subscribe();


      ThreadUtils.sleep(5000);
   }

   private static Mono<Void> sendAlfaData(List<String> buffer) {
      System.out.println("Alfa: " + buffer);
      return Mono.empty();
   }

   private static  Mono<Void> sendNumberData(List<String> buffer) {
      System.out.println("Number: " + buffer);
      return Mono.empty();
   }

   private static Mono<Void> sendData(List<String> many) {
      return Mono.empty();
   }

}
