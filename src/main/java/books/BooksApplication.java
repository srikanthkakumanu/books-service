/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package books;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("books")
@EnableDiscoveryClient
public class BooksApplication {

    public static void main(String[] args) {
        SpringApplication.run(BooksApplication.class, args);
    }
}