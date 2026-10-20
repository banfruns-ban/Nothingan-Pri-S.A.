/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */package cat.copernic.projecte1Grup3.Usuaris.Client.Repository;

import cat.copernic.projecte1Grup3.Usuaris.Client.Entity.clientEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface clientRepository extends JpaRepository<clientEntity, String> {

    List<clientEntity> findByNomContainingIgnoreCaseOrDniContainingIgnoreCase(String nom, String dni);
}