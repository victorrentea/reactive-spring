package victor.training.reactor.pitfalls;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RequiredArgsConstructor
public class LongRunningTask {
   private final LongRepo service;

   public void longRunningProblem() {
      // monitor observe(), save any string there, and start on elastic scheduler. Crash with exception;
      // doOnTerminate / retry()

      service.observe()
          .flatMap(service::save, 10)
          .doOnTerminate(this::longRunningProblem) // start over on terminate
          .subscribeOn(Schedulers.elastic())
          .retry() // retry indefinitely
          .subscribe();
   }
}

interface LongRepo {
   Flux<String> observe();

   Mono<Void> save(String s);
}
