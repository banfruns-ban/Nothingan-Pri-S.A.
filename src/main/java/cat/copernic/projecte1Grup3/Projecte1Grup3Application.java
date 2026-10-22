package cat.copernic.projecte1Grup3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
// Le decimos a Spring dónde están los repositorios JPA i MongoDB
@EnableJpaRepositories(basePackages = {
    "cat.copernic.projecte1Grup3.Vehicles.Repository",
    "cat.copernic.projecte1Grup3.Usuaris.Client.Repository" // Si clientRepository és JPA
})
@EnableMongoRepositories(basePackages = {
    "cat.copernic.projecte1Grup3.Usuaris.Client.Repository" // O on tinguis el repositori Mongo
})
@EntityScan(basePackages = "cat.copernic.projecte1Grup3")
public class Projecte1Grup3Application {

    public static void main(String[] args) {
        SpringApplication.run(Projecte1Grup3Application.class, args);
    }
}