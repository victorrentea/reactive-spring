package victor.training.reactive.reactor.advanced;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactive.intro.ThreadUtils;

import java.util.List;
import java.util.function.BiFunction;

import static reactor.core.scheduler.Schedulers.boundedElastic;

@Slf4j
public class Grouping {
   public static void main(String[] args) {
      new Grouping().run();
   }

   public void run() {

      // TODO send odd numbers to sendOdd(), while even numbers to sendEven(), respectively
      // TODO send even numbers in pages of 10 items
      // TODO for odd numbers call preSendOdd() before sendOdd()
      // TODO item 0 should not trigger any sending

      Flux.range(0, 100)

          .groupBy(NumberType::on)
//          .flatMap(groupedFlux -> {
//             if (groupedFlux.key() == NumberType.EVEN) {
//               return processEven(groupedFlux);
//             } else {
//               return processOdd(groupedFlux);
//             }
//          })

          // antipattern pe java 17
          .flatMap(groupedFlux -> groupedFlux.key().processFunction.apply(this, groupedFlux))

//          .doOnNext(p -> p<0 ? :)
//          .doOnNext(p -> p%2 ==0 ? :)

          // java 17:
//          .flatMap(groupedFlux -> switch (groupedFlux.key()) {
//                 case EVEN -> processEven(groupedFlux);
//                 case ODD -> processOdd(groupedFlux);
          // nu compileaza daca adaugi valori noi la enum
//              }
//          )


          .subscribe();


      ThreadUtils.sleep(2000);
   }

   public Mono<Void> processEven(Flux<Integer> groupedFlux) {
      return groupedFlux.buffer(10).flatMap(page -> sendEven(page)).then();
   }

   public Mono<Void> processOdd(Flux<Integer> groupedFlux) {
      return groupedFlux.buffer(10)
          .flatMap(page -> sendOdd(page).mergeWith(sendOdd2(page))).then();
   }


   public NumberType getType(int number) {
      return number % 2 == 1 ? NumberType.ODD : NumberType.EVEN;
   }

   public Mono<Void> sendEven(List<Integer> item) {
      return Mono.<Void>fromRunnable(() -> log.info("Sending even numbers: {}", item))
          .subscribeOn(boundedElastic());
   }

   public Mono<Void> sendOdd(List<Integer> item) {
      // de ce trebuie de instanta sa fie? ca vrea acces la @Autowired rx repos injectate
      return Mono.<Void>fromRunnable(() -> log.info("Sending odd numbers: {}", item))
          .subscribeOn(boundedElastic())
          ;
   }

   public Mono<Void> sendOdd2(List<Integer> item) {
      return Mono.<Void>fromRunnable(() -> log.info("Sending odd numbers: {}", item))
          .subscribeOn(boundedElastic())
          ;
   }

   public Mono<Void> preSendOdd(Integer item) {
      return Mono.fromRunnable(() -> System.out.println("Pre send odd " + item));
   }

   enum NumberType {
      ODD(Grouping::processOdd),
      EVEN(Grouping::processEven)
//      NEGATIVE_ODD, NEGATIVE_EVEN
      ;

      public final BiFunction<Grouping, Flux<Integer>, Mono<Void>> processFunction;

      NumberType(BiFunction<Grouping, Flux<Integer>, Mono<Void>> process) {
         this.processFunction = process;
      }

      public static NumberType on(Integer n) {
         return n % 2 == 1 ? NumberType.ODD : NumberType.EVEN;
      }
   }
}

