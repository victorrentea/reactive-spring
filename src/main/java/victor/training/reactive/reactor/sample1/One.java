package victor.training.reactive.reactor.sample;

import lombok.Data;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Locale;

public class One {
   private ARxRepo repo;
   private Sender sender
       ;
   //   Flux>call http for each, augment>persist each>
   //   notify all ids with GUARANTEED DELIVER EXATCLY ONE (=blockin sending) > return a flux of IDs as persisted
   public Flux<Long> method(Flux<A> input) {
//      input = null;
      return input
          .flatMap(a ->
                  WebClient.create().post().uri("bla/")

                      .body(Mono
                          .fromSupplier(() -> encryptASYMbsn_HIGHCPU(a.getBsn())), String.class)
                          .retrieve()
                          .bodyToMono(B.class)
                           .map(b -> {
                              // NEVER DO THIS:
                              a.setB(b);
                              return a;
                           })
              )
          .flatMap(a -> repo.save(a))
//          .flatMap(a -> Mono.fromRunnable(() -> sender.sendMessage(a)).subscribeOn(Schedulers.boundedElastic()).thenReturn(a))
          .delayUntil(a -> Mono.fromRunnable(() -> sender.sendMessage(a)).subscribeOn(Schedulers.boundedElastic()))
          .map(A::getId);
   }

   public String encryptASYMbsn_HIGHCPU(String s) {
      return s.toUpperCase(Locale.ROOT);
   }
}

interface Sender {
   void sendMessage(A a);
}
@Data
class A {
   private Long id; // initially NULL
   private String  bsn;
   private String name;
   private B b;
}

interface ARxRepo {
   Mono<A> save(A a);
}

@Data
class B {

}