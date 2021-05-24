package victor.training.reactive.reactor.pitfalls;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.intro.ThreadUtils;

import java.util.Objects;

import static victor.training.reactive.intro.ThreadUtils.sleep;
import static victor.training.reactive.intro.ThreadUtils.waitForEnter;

@Slf4j
public class MultipleSubscribersMono {

   public static void main(String[] args) {
      Mono<A> aMono = getA(); // COLD PUBLISHER

      // TODO callMe(c).subscribe();
      // 1) zip + resubscribe
      // 2) cache
      // 3) zipWhen + TupleUtils

      waitForEnter();
   }

   public Mono<Void> callMe(C c) {
      System.out.println("Got C = " + Objects.requireNonNull(c));
      return Mono.empty();
   }

   public static Mono<A> getA() {
      return Mono.fromCallable(() -> {
         log.info("getA() -- Sending expensive REST call...");
         sleep(1000);
         return new A();
      });
   }

   public static Mono<B> getB(A a) {
      return Mono.fromCallable(()-> {
         log.info("getB({})", a);
         return new B();
      });
   }

   public static Mono<C> getC(A a, B b) {
      return Mono.fromCallable(() -> {
         log.info("getC({},{})", a, b);
         return new C();
      });
   }
}

class A {
}

class B {
}

class C {
}