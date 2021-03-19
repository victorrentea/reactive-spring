package victor.training.reactive.reactor.lite;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Part13Threads {

   //========================================================================================

   // TODO Run readTask on the Schedulers#boundedElastic, and return a mono of the returned value in UPPERCASE
   public Mono<String> subscribe(Supplier<String> readTask) {
      return null;
   }

   //========================================================================================

   // TODO Run readTask on the Schedulers#boundedElastic followed by the cpuTask on the Schedulers#parallel
   public Mono<Void> ioVsCpu(Runnable ioTask, Runnable cpuTask) {
      return null;
   }

   //========================================================================================

   // TODO Run readTask on the Schedulers#boundedElastic in parallel with the cpuTask ran on the Schedulers#parallel
   public Mono<Void> ioVsCpuParallel(Runnable ioTask, Runnable cpuTask) {
      return null;
   }


   //========================================================================================

   interface RxService {
      Mono<String> readData();
      Mono<Integer> cpuTask(String data);
      Mono<Void> writeData(Integer i);
   }

   // TODO Run readTask and writeTask on the Schedulers#boundedElastic and cpuTask on the Schedulers#parallel
   public Mono<?> threadHopping(RxService service) {
      return null;
   }

   //========================================================================================
   interface BlockingService {
      String readData();
      Integer cpuTask(String data);
      void writeData(Integer i);
   }

   // TODO Same as above, but with non-reactive APIs
   public Mono<?> threadHoppingNonMonoApi(BlockingService service) {
      return null;
   }


   //========================================================================================

   // TODO the same as above, but the read and write happen in the caller (before and after you are invoked) - see the test
   public Mono<Integer> threadHoppingHard(Mono<String> sourceMono, Function<String, Mono<Integer>> cpuTask) {
      return sourceMono
          .flatMap(s -> cpuTask.apply(s))
          ;
   }
}
