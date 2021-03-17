package victor.training.reactor.lite;

import lombok.Value;
import reactor.core.publisher.Flux;
import victor.training.reactivespring.start.ThreadUtils;

import java.util.stream.IntStream;
import java.util.stream.Stream;

@Value
class OldNew {
   int oldValue;
   int newValue;

   public boolean isChanged() {
      return oldValue != newValue;
   }
}

public class EmitOnChange4Calin {

   public static void main(String[] args) {


       Flux.just(1, 1, 1, 2, 2, 2, 5, 5, 5)
//           .bufferUntilChanged()
//           .map(list -> list.get(0))
//           .doOnNext(System.out::println)

          .scan(new OldNew(0,0), (prev, n) -> new OldNew(prev.getNewValue(), n))
           .doOnNext(System.out::println)
           .filter(OldNew::isChanged)
           .map(oldNew -> "Delta: " + (oldNew.getNewValue() - oldNew.getOldValue()))
           .skip(1)
           .doOnNext(System.out::println)
          .subscribe()
          ;

      ThreadUtils.sleep(1000);

      Stream<Integer> intStream = IntStream.range(1, 10).boxed();

      Integer reduce = intStream.reduce(0, (acc, n) -> acc + n);
      System.out.println(reduce);
   }
}
