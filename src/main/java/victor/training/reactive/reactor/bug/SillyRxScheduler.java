package victor.training.reactive.reactor.bug;

import javafx.application.Platform;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.NonNull;
import reactor.util.annotation.NonNullApi;

public class SillyRxScheduler implements Scheduler {
   @Override
   @NonNull
   public Disposable schedule(@NonNull Runnable task) {
      Platform.runLater(task);
      return () -> { };
   }

   @Override
   @NonNull
   public Worker createWorker() {
      return new Worker() {
         @Override
         @NonNull
         public Disposable schedule(@NonNull Runnable task) {
            Platform.runLater(task);
            return () -> { };
         }

         @Override
         public void dispose() {

         }
      };
   }
}
