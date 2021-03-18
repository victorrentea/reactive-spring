package victor.training.reactor.dynamic;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactivespring.start.ThreadUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public class Grouping {
   public static void main(String[] args) {
      // TODO odd numbers should be sent in pages of 10 items to sendOdd, while even numbers to sendEven, respectively

      Flux.range(1, 100)

          .groupBy(number -> getType(number)) // a Flux<GroupedFlux<NumberType, Integer>>;
          // Flux.count() == 2 in my case.
          // GroupedFlux.count() = 50
          .flatMap(groupedFlux -> {
             if (groupedFlux.key() == NumberType.EVEN) {
                return groupedFlux
                    .buffer(10)
//          .parallel()
//          .runOn(Schedulers.parallel())
//                    .subscribeOn(Schedulers.parallel())
                    .flatMap(Grouping::sendEven); // flatmap eagerly subscribes to EACH generated Publisher (Mono) in our case.
                // -->> leading to multiple threads acquired due to .subscribeOn in the send... method
             } else {
                return groupedFlux
                    .buffer(10)
//          .parallel()
//          .runOn(Schedulers.parallel())
//                    .subscribeOn(Schedulers.parallel())
                    .flatMap(Grouping::sendOdd);
             }
          })
          .subscribe();

      ThreadUtils.sleep(2000);

//      // java 8 style
//      Map<NumberType, List<Integer>> collect = IntStream.range(1, 100)
//          .boxed()
//          .collect(Collectors.groupingBy(Grouping::getType));
//      log.info(collect.get(NumberType.ODD).toArray());

   }
   public static Mono<Void> sendNumber(Integer number) {
      return Mono.fromRunnable(() -> log.info("N" + number));
   }

   public static NumberType getType(int number) {
      return number % 2 == 1 ? NumberType.ODD : NumberType.EVEN;
   }

   public static Mono<Void> sendEven(List<Integer> page) {
      return Mono.<Void>fromRunnable(() -> log.info("Sending even over REST: {}", page))
          .subscribeOn(Schedulers.boundedElastic())
          // every subscribe signal is performed on a separate thread acquired from the target scheduler,
          // effectively enabling parallelism of the sending
          ;
   }

   public static Mono<Void> sendOdd(List<Integer> page) {
      return Mono.<Void>fromRunnable(() -> log.info("Sending odd over REST: {}", page))
          .subscribeOn(Schedulers.boundedElastic())
          ;
   }
}

enum NumberType {
   ODD, EVEN
}