// ===== CABECERA =====
package cat.copernic.projecte1Grup3.Usuaris.Agent.Entity;

import cat.copernic.projecte1Grup3.Usuaris.Entity.usuariEntity; // Clase padre (dni, nom, email, password)
import jakarta.persistence.Column;     // Configura una columna (nullable, etc.)
import jakarta.persistence.Entity;     // Marca la clase como entidad JPA
import jakarta.persistence.EnumType;   // Forma de guardar el enum (STRING u ORDINAL)
import jakarta.persistence.Enumerated; // Indica que un campo es un enum
import jakarta.persistence.Table;      // Define el nombre de la tabla

// ===== DECLARACIÓN DE LA CLASE =====
@Entity                 // Esta clase se mapea a una tabla: cada objeto es una fila (tabla en SQL)
@Table(name = "agents") // Nombre de la tabla (si no, se llamaría AgentEntity) (Nombre de la tabla creada)
public class AgentEntity extends usuariEntity { // Un agente ES un usuario: hereda dni, nom, email y password

    // ===== ATRIBUTOS (columnas de la tabla) =====
    @Column(nullable = false)   // Obligatorio: NOT NULL en la BD
    private String hora_entrada;

    @Column(nullable = false)   // Obligatorio
    private String hora_sortida;

    @Enumerated(EnumType.STRING) // Guarda el enum como texto ("ADMIN"), no como número:
    @Column(nullable = false)    // si reordenas el enum, los datos antiguos no se estropean
    private Rol rol;

    @Column(nullable = true)    // Opcional
    private String foto; // Guarda el nom o ruta del fitxer de la imatge (la imagen está en disco, no en la BD)

    // ===== GETTERS Y SETTERS =====
    // Los campos son private; Spring y Thymeleaf usan estos métodos para leer y rellenar el formulario
    public String getHora_entrada() {
        return hora_entrada;
    }

    public void setHora_entrada(String hora_entrada) {
        this.hora_entrada = hora_entrada;
    }

    public String getHora_sortida() {
        return hora_sortida;
    }

    public void setHora_sortida(String hora_sortida) {
        this.hora_sortida = hora_sortida;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getFoto() {
        return foto;
    } // El controlador lo usa al editar para no perder la foto anterior

    public void setFoto(String foto) {
        this.foto = foto;
    }

    // ===== CONSTRUCTORES =====
    // Vacío: lo exige JPA para leer de la BD, y Spring MVC para crear el objeto del formulario
    public AgentEntity() {
        super();
    }

    // Completo: super(...) inicializa primero la parte de usuariEntity y después se asignan los campos propios
    public AgentEntity(String hora_entrada, String hora_sortida, Rol rol, String dni, String nom, String email, String password) {
        super(dni, nom, email, password);
        this.hora_entrada = hora_entrada;
        this.hora_sortida = hora_sortida;
        this.rol = rol;
    }
}