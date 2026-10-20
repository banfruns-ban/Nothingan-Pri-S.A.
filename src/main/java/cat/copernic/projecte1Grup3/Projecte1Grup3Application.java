package cat.copernic.projecte1Grup3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "cat.copernic.projecte1Grup3.Usuaris.Client.Repository")
@EnableMongoRepositories(basePackages = "cat.copernic.projecte1Grup3.Usuaris.Client.Repository")

public class Projecte1Grup3Application {

	public static void main(String[] args) {
		SpringApplication.run(Projecte1Grup3Application.class, args);
	}

}



