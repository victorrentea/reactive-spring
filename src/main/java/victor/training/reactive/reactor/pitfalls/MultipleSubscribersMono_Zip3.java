package victor.training.reactive.reactor.pitfalls;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import victor.training.reactive.intro.ThreadUtils;

import java.util.Objects;

import static victor.training.reactive.intro.ThreadUtils.sleep;
import static victor.training.reactive.intro.ThreadUtils.waitForEnter;

@Slf4j
public class MultipleSubscribersMono_Zip3 {

   public static void main(String[] args) {
      // option3
//      Mono<A> aMono = getA().cache();
//      Mono<B> bMono = aMono.flatMap(a -> getB(a));
//      Mono<B> cMono = aMono.flatMap(a -> bMono.flatMap());//flatMap(a -> getB(a));

      // option2
      Mono<C> cMono = getA()
          .zipWhen(a->getB(a))
//          .map(TupleUtils.function(a,b)->new AB(a,b)) // better if "AB" makes sense
//          .flatMap(tuple2 -> getC(tuple2.getT1(), tuple2.getT2())));
          .flatMap(TupleUtils.function((a, b) -> getC(a, b))); // ok

      // option3
      getA()
          .flatMap(a -> getB(a).flatMap(b-> getC(a,b))); // easy to do mistakes



      cMono.subscribe(c -> callMe(c));


      waitForEnter();
   }

   // TASK: fettch a C and call this method with it.
   public static Mono<Void> callMe(C c) {
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