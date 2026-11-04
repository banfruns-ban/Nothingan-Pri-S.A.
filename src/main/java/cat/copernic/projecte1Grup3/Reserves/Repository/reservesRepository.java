/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Reserves.Repository;
 
import cat.copernic.projecte1Grup3.Reserves.Entity.reservesEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 @Repository
public interface reservesRepository extends JpaRepository<reservesEntity, Long> {
 
    // Reserves d'un vehicle que no tenen l'estat indicat (s'usa per ignorar les CANCELADA)
    List<reservesEntity> findByVehicleMatriculaAndEstatNot(String matricula, String estat); //Spring lee el nombre del método y construye la consulta solo:
}
 