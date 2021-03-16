package victor.training.reactor.pitfalls;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactivespring.start.ThreadUtils;

@RequiredArgsConstructor
public class LongRunningTask {


   public static void main(String[] args) {
      new LongRunningTask().longRunningProblem();
      ThreadUtils.sleep(2000);
   }

   public void longRunningProblem() {
      observe()
          .flatMap(this::save)
          .subscribeOn(Schedulers.boundedElastic())
          .doOnTerminate(() -> System.out.println("END")) // How many times does this print ?
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
