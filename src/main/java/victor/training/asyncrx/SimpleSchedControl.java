package victor.training.asyncrx;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactivespring.start.ThreadUtils;

@Slf4j
public class SimpleSchedControl {

   public static void main(String[] args) {

      Mono.fromRunnable(() -> cpu())
          .subscribeOn(Schedulers.parallel())
          .subscribe();

      Mono.fromRunnable(SimpleSchedControl::blockingIO)
          .subscribeOn(Schedulers.boundedElastic())
          .subscribe();

      Mono.fromCallable(SimpleSchedControl::nonBlockingIO)
          .subscribeOn(Schedulers.single())
          .subscribe(v -> log.info(v));


      ThreadUtils.sleep(1000);
   }


   public static void cpu() {
       log.info("CPU");
   }
   public static void blockingIO() {
       log.info("blockingIO");
   }
   public static String nonBlockingIO()  throws Exception {
       log.info("nonBlockingIO");
       return "a";
   }
}
