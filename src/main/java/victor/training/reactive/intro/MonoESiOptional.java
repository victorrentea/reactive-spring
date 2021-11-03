package victor.training.reactive.intro;

import reactor.core.publisher.Mono;

import java.io.IOException;

public class MonoESiOptional {
   public static void main(String[] args) {
//      Mono.just(null)
//          .subscribe();
      Mono.empty();

      Mono.create(sink -> {
         sink.success();
      });
      Mono.create(sink -> {
         sink.success("date");
      });
      Mono.error(new IOException());
      
   }

   public Mono<Void> functieCareNuTiDaNimic() {
      return Mono.empty();
   }

   {
      functieCareAruncaExceptieCandva(1)
          .onErrorReturn("BUBA")
          .flatMap(s -> trimiteEmail(s))
          .block();
   }

   private Mono<Void> trimiteEmail(String s) {
      return null;
   }

   // daca vezi return Mono/Flux<>  mereu faci Mono/flux.error in loc de throw
   public Mono<String> functieCareAruncaExceptieCandva(int x) {

      if (x == 0) {
//         throw new IllegalArgumentException(); // asa NU
         return Mono.error(new IllegalArgumentException()); // DA
      }
      return Mono.just("aa");
      // apeluri blocante
   }
}
