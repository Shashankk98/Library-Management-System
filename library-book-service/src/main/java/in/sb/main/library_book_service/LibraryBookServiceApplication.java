package in.sb.main.library_book_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LibraryBookServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibraryBookServiceApplication.class, args);
	}

}
