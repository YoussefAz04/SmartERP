package BuissnesManager.BuissnesManager;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BuissnesManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BuissnesManagerApplication.class, args);
	}

	@Bean
	public Tesseract tesseract (){
		Tesseract tesseract = new Tesseract();
		tesseract.setLanguage("eng+fra+ara");
		tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
		return tesseract ;
	}

}
