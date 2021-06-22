package victor.training.reactive.reactor.advanced;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactive.intro.ThreadUtils;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import static reactor.core.scheduler.Schedulers.boundedElastic;

@Slf4j
public class Grouping {
   public static void main(String[] args) {
      new Grouping().method();
   }


   enum NumberType {
      ODD(Grouping::sendOdd),
      EVEN(Grouping::sendEven);
//      EVEN((a,b) -> Mono.empty());
      public final BiFunction<Grouping, List<Integer>, Mono<Void>> processor;
      NumberType(BiFunction<Grouping, List<Integer>, Mono<Void>> processor) {
         this.processor = processor;
      }
   }

   public void method() {

      // TODO send odd numbers to sendOdd(), while even numbers to sendEven(), respectively
      // TODO send even numbers in pages of 10 items
      // TODO for odd numbers call preSendOdd() before sendOdd()
      // TODO item 0 should not trigger any sending


      Flux.range(0, 100)
          // if many combinations, try this :
//          .delayUntil(n -> sendIfEven(n))
//          .delayUntil(n -> sendIfOdd(n))
//          .delayUntil(n -> sendIfRed(n))


          .groupBy(n -> getType(n))
          .flatMap(groupedFlux -> groupedFlux.buffer(10).flatMap(page -> groupedFlux.key().processor.apply(this, page))  )
          .subscribe();

      ThreadUtils.sleep(2000);



//      Map<Boolean, List<Integer>> collect = IntStream.range(1, 10)
//          .boxed()
//          .collect(Collectors.groupingBy(n -> n % 2 == 0));
   }

   private Mono<Void> sendIfEven(Integer n) {
      if (getType(n) == NumberType.EVEN) {
         // actually to a network call
      }
      return Mono.empty();
   }

//   private Mono<Void> sendNumber(Integer n) {
//      return getType(n).processor.apply(this, n);
//   }

   public static NumberType getType(int number) {
      return number % 2 == 1 ? NumberType.ODD : NumberType.EVEN;
   }

   public Mono<Void> sendEven(List<Integer> item) {
      return Mono.<Void>fromRunnable(() -> log.info("Sending even numbers: {}", item))
          .subscribeOn(boundedElastic())
          ;
   }

   public Mono<Void> sendOdd(List<Integer> item) {
      // WebClient.post.... .flatMap(webclient.sendToThisOthreEndpoint)
      return Mono.<Void>fromRunnable(() -> log.info("Sending odd numbers: {}", item))
          .subscribeOn(boundedElastic())
          ;
   }

   public Mono<Void> preSendOdd(Integer item) {
      return Mono.fromRunnable(() -> System.out.println("Pre send odd " + item));
   }
}

