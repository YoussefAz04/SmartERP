package BuissnesManager.BuissnesManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BuissnesManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BuissnesManagerApplication.class, args);
	}

}
