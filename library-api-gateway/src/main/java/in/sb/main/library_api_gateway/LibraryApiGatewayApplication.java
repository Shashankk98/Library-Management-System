package in.sb.main.library_api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LibraryApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApiGatewayApplication.class, args);
    }

}
