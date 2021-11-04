package victor.training.reactive.reactor.pitfalls;

import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.tuple.Tuple2;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

import static reactor.function.TupleUtils.function;
import static victor.training.reactive.intro.ThreadUtils.sleep;
import static victor.training.reactive.intro.ThreadUtils.waitForEnter;

@Slf4j
public class MultipleSubscribersMono_Zip3 {

   public static void main(String[] args) {
      Mono<A> aMono = getA(); // COLD PUBLISHER

      // TODO callMe(c).subscribe();
      // 1) zip + resubscribe
      // 2) cache
      // 3) zipWhen + TupleUtils

      waitForEnter();
   }

   public Mono<C> callMe(C c) {
      System.out.println("Got C = " + Objects.requireNonNull(c));


      // solutia1L cache
//      Mono<A> aMono = getA().cache();
//      Mono<B> bMono = aMono.flatMap(a -> getB(a));
//      return aMono.zipWith(bMono, (a, b) -> getC(a, b)).flatMap(Function.identity());

      // Solutia2: .connect()

//      Tuple2<Tuple3<String, Integer, Map<String, Integer>>, LocalDate> doamneMiluieste;

      // Cuplu doamneMiluieste; {
      //         Copil copil;
      //         LocalDateTime dataCasatoriei;
      //      }

      // solutia3:
      return getA()
          .flatMap(a -> getB(a).map(b -> Tuples.of(a, b)))
          .flatMap(function((a, b) -> getC(a, b)));

   }

   public static Mono<A> getA() {
      return Mono.fromCallable(() -> {
         log.info("getA() -- Sending expensive REST call...");
         sleep(1000);
         return new A();
      });
   }

   public static Mono<B> getB(A a) {
      return Mono.fromCallable(() -> {
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