package victor.training.reactive.reactor.pitfalls;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.intro.ThreadUtils;

import java.util.Objects;

import static victor.training.reactive.intro.ThreadUtils.sleep;
import static victor.training.reactive.intro.ThreadUtils.waitForEnter;

@Slf4j
public class MultipleSubscribersCache {

   public static void main(String[] args) {
      Mono<String> restMono = expensiveRestCall(); // COLD PUBLISHER

      // TODO start in parallel f() and g() with the String returned by this mono

      waitForEnter();
   }

   public static void f(String s) {
      log.info("f({})", s);
      sleep(1000);
   }

   public static void g(String s) {
      log.info("g({})", s);
      sleep(1000);
   }

   public static Mono<String> expensiveRestCall() {
      return Mono.fromCallable(() -> {
         log.info("Sending REST call...");
         ThreadUtils.sleep(1000);
         return "Data";
      }).subscribeOn(Schedulers.boundedElastic());
   }
}
