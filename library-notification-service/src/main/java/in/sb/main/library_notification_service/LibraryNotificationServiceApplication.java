package in.sb.main.library_notification_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LibraryNotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibraryNotificationServiceApplication.class, args);
	}

}
