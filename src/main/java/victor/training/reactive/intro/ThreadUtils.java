package victor.training.reactive.intro;

import java.util.Scanner;

public class ThreadUtils {
	
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}		
	}

	public static void waitForEnter() {
		System.out.println("\nHit [ENTER] to continue");
		new Scanner(System.in).next();
	}
	
}
