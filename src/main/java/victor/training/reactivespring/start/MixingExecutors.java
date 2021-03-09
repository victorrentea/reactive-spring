package victor.training.reactivespring.start;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MixingExecutors {

   public static void main(String[] args) {

      int i = ioRead();
      int i2 = cpu(i);
      ioWrite(i2);
   }

   public static int ioRead() {
      log.info("read");
      ThreadUtils.sleep(100);
      return 1;
   }
   public static int cpu(int i) {
      log.info("CPU");
      return i * 2;
   }
   public static void ioWrite(int i) {
      log.info("write " + i );
   }
}
