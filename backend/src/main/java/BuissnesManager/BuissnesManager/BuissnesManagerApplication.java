package BuissnesManager.BuissnesManager;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${tesseract.datapath:#{null}}")
    private String tesseractDatapath;

    @Bean
    Tesseract tesseract() {
        Tesseract tesseract = new Tesseract();
        tesseract.setLanguage("eng+fra+ara");
        String datapath = tesseractDatapath;
        if (datapath == null || datapath.isEmpty()) {
            // Default paths by OS
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("win")) {
                datapath = "C:\\Program Files\\Tesseract-OCR\\tessdata";
            } else {
                datapath = "/usr/share/tesseract-ocr/4.00/tessdata";
            }
        }
        tesseract.setDatapath(datapath);
        return tesseract ;
    }

}
