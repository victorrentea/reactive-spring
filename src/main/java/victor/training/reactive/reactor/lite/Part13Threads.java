package victor.training.reactive.reactor.lite;


import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class Part13Threads {

   //========================================================================================

   // TODO Run readTask on the Schedulers#boundedElastic, and return a mono of the returned value in UPPERCASE
   public Mono<String> subscribe(Supplier<String> readTask) {
      return Mono.fromSupplier(readTask)
          .subscribeOn(Schedulers.boundedElastic())
          .map(String::toUpperCase);
   }

   //========================================================================================

   // TODO Run readTask on the Schedulers#boundedElastic followed by the cpuTask on the Schedulers#parallel
   public Mono<Void> ioVsCpu(Runnable ioTask, Runnable cpuTask) {
      return Mono.fromRunnable(ioTask)
//          .doOnSubscribe(s -> log.info("subscribe 2"))
          .subscribeOn(Schedulers.boundedElastic())
          .publishOn(Schedulers.single())
          .then(runCpu(cpuTask))
          .doOnTerminate(() -> log.info("WTF"))
          .then()
          ;
   }

   private Mono<Object> runCpu(Runnable cpuTask) {
      return Mono.fromRunnable(cpuTask).subscribeOn(Schedulers.parallel());
   }

   //========================================================================================

   // TODO Run readTask on the Schedulers#boundedElastic in parallel with the cpuTask ran on the Schedulers#parallel
   public Mono<Void> ioVsCpuParallel(Runnable ioTask, Runnable cpuTask) {
      Mono<Void> ioMono = Mono.fromRunnable(ioTask)
//          .doOnTerminate(() -> ThreadUtils.sleep(10000))
          .subscribeOn(Schedulers.boundedElastic()).then();
      Mono<Void> cpuMono = Mono.fromRunnable(cpuTask).subscribeOn(Schedulers.parallel())
//          .doOnTerminate(() -> {throw new IllegalArgumentException();})
          .then();

      return ioMono.and(cpuMono);
   }


   //========================================================================================

   // TODO Run readTask and writeTask on the Schedulers#boundedElastic and cpuTask on the Schedulers#parallel
   public Mono<?> threadHopping(RxService service) {
      return service.readData()
          .subscribeOn(Schedulers.boundedElastic())

          .flatMap(s -> service.cpuTask(s).subscribeOn(Schedulers.parallel()))
          .flatMap(i-> service.writeData(i).subscribeOn(Schedulers.boundedElastic()))
          ;
   }

   // TODO Same as above, but with non-reactive APIs
   public Mono<?> threadHoppingNonMonoApi(BlockingService service) {
      return Mono.fromSupplier(service::readData)
          .subscribeOn(Schedulers.boundedElastic())
          .publishOn(Schedulers.parallel())
          .flatMap(s -> Mono.fromSupplier(() -> service.cpuTask(s)))
          .publishOn(Schedulers.boundedElastic())
          .flatMap(i -> Mono.fromRunnable(() -> service.writeData(i)));
   }

   // TODO the same as above, but the read and write happen in the caller (before and after you are invoked) - see the test
   public Mono<Integer> threadHoppingHard(Mono<String> sourceMono, Function<String, Mono<Integer>> cpuTask) {
      return sourceMono
          .subscribeOn(Schedulers.boundedElastic())
          .publishOn(Schedulers.parallel())
          .flatMap(s -> cpuTask.apply(s))
          .publishOn(Schedulers.boundedElastic())
          ;
   }

   interface RxService {
      Mono<String> readData();

      Mono<Integer> cpuTask(String data);

      Mono<Void> writeData(Integer i);
   }


   //========================================================================================

   //========================================================================================
   interface BlockingService {
      String readData();

      Integer cpuTask(String data);

      void writeData(Integer i);
   }
}
