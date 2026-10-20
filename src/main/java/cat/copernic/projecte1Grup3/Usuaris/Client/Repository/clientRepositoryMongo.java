/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Client.Repository;

import cat.copernic.projecte1Grup3.Usuaris.Client.Documents.clientDocuments;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 *
 * @author User
 */
public interface clientRepositoryMongo extends MongoRepository<clientDocuments, String> {

}