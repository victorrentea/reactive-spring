package victor.training.reactive.intro;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.blockhound.BlockingOperationError;

import java.io.PrintWriter;
import java.io.StringWriter;

@RestControllerAdvice
public class ReportBlockingError {
   @ExceptionHandler(BlockingOperationError.class)
   @ResponseStatus
   public String handleBlockingDetected(BlockingOperationError e) {
      StringWriter writer = new StringWriter();
      e.printStackTrace(new PrintWriter(writer));
      return "<h1>!!BLOCKING CALL DETECTED!!</h1>\n" +
             writer.toString().replaceAll("\n", "<br>");
   }
}
