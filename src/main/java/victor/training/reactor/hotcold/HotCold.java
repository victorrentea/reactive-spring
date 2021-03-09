package victor.training.reactor.hotcold;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class HotCold {

   public static String captureThread(String workType) {
       return workType + ":" + Thread.currentThread().getName();
   }
   public static void main(String[] args) {
      List<String> steps =new ArrayList<>();

      Runnable readTask = () -> steps.add(captureThread("READ"));

      Runnable cpuTask = () -> steps.add(captureThread("CPU"));
      Runnable writeTask = () -> steps.add(captureThread("WRITE"));


      Mono<String> mono = Mono.just("a")
          .then(Mono.fromRunnable(readTask))
          .subscribeOn(Schedulers.boundedElastic())
          .publishOn(Schedulers.parallel())
          .then(Mono.fromRunnable(cpuTask))
          .publishOn(Schedulers.boundedElastic())
          .then(Mono.fromRunnable(writeTask))
          ;
      StepVerifier.create(mono)
          .verifyComplete();
      Assertions.assertThat(steps.toString())
          .contains("READ:boundedElastic")
          .contains("CPU:parallel")
          .contains("WRITE:boundedElastic");
   }
}
