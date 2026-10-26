/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Usuaris.Agent.Repository;

import cat.copernic.projecte1Grup3.Usuaris.Agent.Entity.agentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Usuario
 */

@Repository
public interface agentRepository extends JpaRepository<agentEntity, String> {
    // Al extender JpaRepository<Entidad, Tipo_de_la_Clave_Primaria>, 
    // heredas automáticamente el método save()
}