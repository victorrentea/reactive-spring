package victor.training.asyncrx;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import victor.training.reactivespring.start.ThreadUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class HowToRunOneIOTask {
   public static void main(String[] args) {

      // TODO run method() on .io (rx)    .boundedElastic (reactor) schedulers
      ExecutorService pool = Executors.newFixedThreadPool(2);
      Scheduler myOwnSched = Schedulers.fromExecutor(pool);

//      Mono.just(method())
      Mono.fromSupplier(() -> method())
          .subscribeOn(Schedulers.boundedElastic())
          .log()
          .doOnNext(s -> log.info("ON NEXT: " + s))
          .subscribe();


      ThreadUtils.sleep(1000);
//          .publishOn(Schedulers.boundedElastic()) // observeOn ~ Rx
   }

   public static String method() {
       log.info("IO task");
       return "a";
   }
}
