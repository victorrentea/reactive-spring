package victor.training.reactor.lite;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Part13Threads {

   //========================================================================================

   // TODO Run readTask on the Schedulers#boundedElastic, and return a mono of the returned value in UPPERCASE
   public Mono<String> subscribe(Supplier<String> readTask) {
      return Mono.fromSupplier(readTask)
          .subscribeOn(Schedulers.boundedElastic());
   }

   //========================================================================================

   // TODO Run readTask on the Schedulers#boundedElastic followed by the cpuTask on the Schedulers#parallel
   public Mono<Void> ioVsCpu(Runnable ioTask, Runnable cpuTask) {
      return Mono.fromRunnable(ioTask)
          .subscribeOn(Schedulers.boundedElastic())
          .publishOn(Schedulers.parallel())
          .then(Mono.fromRunnable(cpuTask));
   }

   //========================================================================================

   // TODO Run readTask on the Schedulers#boundedElastic in parallel with the cpuTask ran on the Schedulers#parallel
   public Mono<Void> ioVsCpuParallel(Runnable ioTask, Runnable cpuTask) {

      Mono<Void> ioMono = Mono.fromRunnable(ioTask)
          .subscribeOn(Schedulers.boundedElastic())
          .then();

      Mono<Void> cpuMono = Mono.fromRunnable(cpuTask)
          .subscribeOn(Schedulers.parallel())
          .then();


      return Mono.when(ioMono, cpuMono);
   }


   //========================================================================================

   interface RxService {
      Mono<String> readData();
      Mono<Integer> cpuTask(String data);
      Mono<Void> writeData(Integer i);
   }

   // TODO Run readTask and writeTask on the Schedulers#boundedElastic and cpuTask on the Schedulers#parallel
   public Mono<?> threadHopping(RxService service) {
      return service.readData()
          .subscribeOn(Schedulers.boundedElastic())
          .publishOn(Schedulers.parallel())
          .flatMap(s -> service.cpuTask(s))
          .publishOn(Schedulers.boundedElastic())
          .flatMap(i -> service.writeData(i));
   }

   //========================================================================================
   interface BlockingService {
      String readData();
      Integer cpuTask(String data);
      void writeData(Integer i);
   }

   // TODO Same as above, but with non-reactive APIs
   public Mono<?> threadHoppingNonMonoApi(BlockingService service) {
      return Mono.fromSupplier(() -> service.readData())
          .subscribeOn(Schedulers.boundedElastic())
          .publishOn(Schedulers.parallel())
          .flatMap(s -> Mono.defer(() -> Mono.just(service.cpuTask(s))))
          .publishOn(Schedulers.boundedElastic())
          .flatMap(i -> Mono.fromRunnable(() -> service.writeData(i)));
   }


   //========================================================================================

   // TODO the same as above, but the read and write happen in the caller (before and after you are invoked) - see the test
   public Mono<Integer> threadHoppingHard(Mono<String> sourceMono, Function<String, Mono<Integer>> cpuTask) {
      return sourceMono
          .subscribeOn(Schedulers.boundedElastic())
          .publishOn(Schedulers.parallel())
          .flatMap(s -> cpuTask.apply(s))
          .publishOn(Schedulers.boundedElastic())
          ;
   }
}
