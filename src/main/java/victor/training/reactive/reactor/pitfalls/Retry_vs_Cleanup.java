package victor.training.reactive.reactor.pitfalls;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.intro.ThreadUtils;

@RequiredArgsConstructor
public class Retry_vs_Cleanup {


   public static void main(String[] args) {
      new Retry_vs_Cleanup().longRunningProblem();
      ThreadUtils.sleep(2000);
   }

   public void longRunningProblem() {
      observe()
          .flatMap(this::save)
          .subscribeOn(Schedulers.boundedElastic())
          .doOnTerminate(() -> System.out.println("~finally {  } END")) // How many times does this print ?
          .retry(2)
          .subscribe();
   }

   Flux<String> observe() {
      return Flux.create(sink-> {
         sink.next("a");
         sink.next("b");
         sink.error(new RuntimeException());
      });
   }

   Mono<Void> save(String s) {
      return Mono.empty();
   }
}
