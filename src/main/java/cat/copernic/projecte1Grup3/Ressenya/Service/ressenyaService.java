/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cat.copernic.projecte1Grup3.Ressenya.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.projecte1Grup3.Ressenya.Entity.ressenyaEntity;
import cat.copernic.projecte1Grup3.Ressenya.Repository.ressenyaRepository;
import cat.copernic.projecte1Grup3.Usuaris.Client.Repository.clientRepository;
import cat.copernic.projecte1Grup3.Vehicles.Repository.VehiclesRepository;

/**
 *
 * @author User
 */
@Service
public class ressenyaService {
    
    private final ressenyaRepository resenyaRepo;
    private final VehiclesRepository vehicleRepo;
    private final clientRepository clientRepo;

    public ressenyaService(ressenyaRepository resenyaRepo, VehiclesRepository vehicleRepo, clientRepository clientRepo) {
        this.resenyaRepo = resenyaRepo;
        this.vehicleRepo = vehicleRepo;
        this.clientRepo = clientRepo;
    }
    
    public List<ressenyaEntity> llistarPerVehicle(String matricula) {
    return resenyaRepo.findByVehicleMatriculaOrderByDataCreacioDesc(matricula);
}
     @Transactional
    public void crear(String matricula, String dni, int puntuacio, String comentari) {
    var vehicle = vehicleRepo.findById(matricula)
            .orElseThrow(() -> new IllegalArgumentException("Vehicle no trobat"));
    var client = clientRepo.findById(dni.trim().toUpperCase())
            .orElseThrow(() -> new IllegalArgumentException("Client no trobat"));

    if (puntuacio < 1 || puntuacio > 5)
        throw new IllegalArgumentException("La puntuació ha de ser entre 1 i 5");

    String text = (comentari == null || comentari.isBlank()) ? null : comentari.trim();
    if (text != null && text.length() > 500)
        throw new IllegalArgumentException("El comentari no pot superar els 500 caràcters");

    ressenyaEntity r = new ressenyaEntity();
    r.setVehicle(vehicle);
    r.setClient(client);
    r.setPuntuacio(puntuacio);
    r.setComentari(text);
    r.setDataCreacio(LocalDateTime.now());
    resenyaRepo.save(r);
}

    @Transactional
    public void eliminar(Long codi) {
        if (!resenyaRepo.existsById(codi))
            throw new IllegalArgumentException("Ressenya no trobada");
        resenyaRepo.deleteById(codi);
    }
}
    
    
    

