package victor.training.reactive.reactor.advanced;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class OnErrorResumevsContinue {

   public static void main(String[] args) {
      Mono.just("a")
          .map(s -> Integer.parseInt(s))
          .map(i -> i + 1)
          .onErrorResume(ex -> Mono.just(-1))
          .subscribe(System.out::println);

      List<Integer> list = Flux.just("a", "1")
          .map(s -> Integer.parseInt(s))
          .map(i -> i + 1)
          .log()
          .onErrorContinue((ex, criminal) ->
              {
                 throw new RuntimeException();
              }
//              System.err.println("Booboo on " + criminal + " : " + ex.getMessage())
       )
//          .subscribe(System.out::println)
          .collectList().block();

      System.out.println(list);
   }
}
