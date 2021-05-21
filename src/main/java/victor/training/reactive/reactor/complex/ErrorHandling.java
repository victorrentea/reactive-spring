package victor.training.reactive.reactor.complex;

import reactor.core.publisher.Mono;

public class ErrorHandling {
   public static void main(String[] args) {

         Mono.error(new IllegalStateException())
//             .flatMap()
//             .flatMap()
//             .flatMap()
//             .flatMap()
//             .flatMap()
//             .flatMap()
             .onErrorResume(IllegalArgumentException.class, t->Mono.empty())
             .switchIfEmpty(met())
//             .doOnTerminate()
//             .onErrorContinue()
            .subscribe(System.out::println);

   }

   private static Mono<String> met() {
      return null;
   }
}
