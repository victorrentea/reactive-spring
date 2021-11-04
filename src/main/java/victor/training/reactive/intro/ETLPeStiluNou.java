package victor.training.reactive.intro;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class ETLPeStiluNou {

   public static Flux<Long> sursa() {
      return Flux.interval(Duration.ofMillis(1))
          .doOnSubscribe(s -> System.out.println("DESCHID SURSA"))
          .doOnCancel(() -> System.out.println("CANCEL SURSA"));
   }

   public static Mono<Void> destinatie(Flux<String> deScrisInDB) {
      return deScrisInDB
          .doOnNext(s -> {
             System.out.println("Scriu " + s);
             if (s.length() > 2) throw new IllegalArgumentException();
          })
          .then();
   }
   public static void main(String[] args) {


      Mono<Void> f = sursa()
          .log("DupaSursa")
          .map(String::valueOf)
          .log("inainte de do")
          .doOnNext(s -> {
             System.out.println("Scriu " + s);
             if (s.length() > 2) throw new IllegalArgumentException();
          })
          .log("inainte de then")
          .then();
//          .retry(2);

      f.subscribe();
      f.subscribe();



      ThreadUtils.sleep(100000);



   }
}
