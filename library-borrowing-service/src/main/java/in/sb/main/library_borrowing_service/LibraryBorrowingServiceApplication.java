package in.sb.main.library_borrowing_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LibraryBorrowingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibraryBorrowingServiceApplication.class, args);
	}

}
