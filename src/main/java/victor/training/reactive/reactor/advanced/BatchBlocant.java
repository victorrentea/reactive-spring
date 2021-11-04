package victor.training.reactive.reactor.advanced;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.intro.ThreadUtils;

import java.time.Duration;
import java.util.List;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toList;

@Slf4j
public class BatchBlocant {



   public static void main(String[] args) {
      // Exercise:
      // - fetch IDs in pages of 10 on 4 threads,
      // - then process each on n=CPU
      // - then write them in pages of 100 on 1 thread.
      // * All blocking IO

//      BlockHound.install();

      List<Long> ids = LongStream.range(0, 100).boxed().collect(toList());

      Flux.fromIterable(ids)
          .buffer(10)
          .flatMap(idPage -> fetch(idPage)
                  .subscribeOn(Schedulers.boundedElastic()),
              4
          )
          .publishOn(Schedulers.parallel()) // nenecesara in practica
          .map(BatchBlocant::processCPU)
          .buffer(50)
//          .publishOn(POOL_UNIC)
          .publishOn(Schedulers.boundedElastic())
          .flatMap(resultsPage -> persist(resultsPage), 1
          //    .subscribeOn(POOL_UNIC)
          )
          .then()
          .block();

   }

   public static Flux<String> fetch(List<Long> idPage) {
      return Flux.fromStream(
          () -> {
             log.info("Fetch page START " + idPage);
             ThreadUtils.sleep(50);
             log.info("Fetch page END " + idPage);
             return idPage.stream().map(l -> l + "");
          }
      )
          ;
   }

   public static Integer processCPU(String elem) {
      log.info("Process " + elem);
      int sum = 0;
      for (int i = 0; i < 10_000_000; i++) {
         sum++;

      }
      log.info("Process END " + elem + " : " + sum);
      return Integer.parseInt(elem) * 2;
   }

   public static final Scheduler POOL_UNIC = Schedulers.newSingle("descris");
   public static Mono<Void> persist(List<Integer> resultsPage) {
      return Mono.fromRunnable(
              () -> {
                 log.info("Sending START " + resultsPage);
                 ThreadUtils.sleep(50);
                 log.info("Sending DONE" + resultsPage);
              }
          )
//          .subscribeOn(Schedulers.boundedElastic())
//          .subscribeOn(POOL_UNIC) // doar exceptional. In general preferi bounded cu concurrency la flatMap
          .then();
   }
}
