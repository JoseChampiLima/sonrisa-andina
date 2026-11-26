package pe.edu.clinica.sonrisa_andina;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SonrisaAndinaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SonrisaAndinaApplication.class, args);
	}

}
