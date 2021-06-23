package victor.training.reactive.spring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
public class InMemoryBroadcastController {
   private AtomicInteger integer = new AtomicInteger(0);
   private Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();
   Flux<String> flux = sink.asFlux();
   Flux<List<String>> pageflux = sink.asFlux().buffer(Duration.ofSeconds(3));


   @GetMapping(value = "message/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public Flux<ServerSentEvent<CustomerDto>> messageStream() {
      return flux.map(CustomerDto::new).map(dto -> ServerSentEvent.builder(dto).build());
   }
//   @GetMapping(value = "message/stream")
//   public Flux<ServerSentEvent<List<CustomerDto>>> messageStream233(Flux<SomeDto> s) {
//
//   }
   @GetMapping(value = "message/stream-page", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public Flux<ServerSentEvent<List<CustomerDto>>> messageStream2() {
      return pageflux.map(this::convertPage)
          .map(dto -> ServerSentEvent.builder(dto).build());
   }

   private List<CustomerDto> convertPage(List<String> list) {
      return list.stream().map(CustomerDto::new).collect(Collectors.toList());
   }

   @GetMapping("message/send")
   public void sendMessage() {
      sink.tryEmitNext("Hello " + integer.incrementAndGet());
   }

   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   static class CustomerDto {
      private String value;
   }
}
