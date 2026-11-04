/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Ressenya.Repository;

import cat.copernic.projecte1Grup3.Ressenya.Entity.ressenyaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author User
 */
@Repository
public interface ressenyaRepository extends JpaRepository<ressenyaEntity, Long> {

    List<ressenyaEntity> findByVehicleMatriculaOrderByDataCreacioDesc(String matricula);
}
