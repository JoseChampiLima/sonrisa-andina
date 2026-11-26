package pe.edu.clinica.sonrisa_andina.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cuenta de acceso al sistema. Guarda la contrasena cifrada con BCrypt
 * (nunca en texto plano) y el rol que define sus permisos.
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona")
    private Persona persona;

    @Column(name = "username", nullable = false, unique = true, length = 60)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    public Usuario(String username, String passwordHash, Rol rol, Persona persona) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.persona = persona;
    }

    public String getNombreVisible() {
        return persona != null ? persona.getNombreCompleto() : username;
    }

    public boolean isAdmin() {
        return rol == Rol.ADMIN;
    }

    public boolean isRecepcion() {
        return rol == Rol.RECEPCION;
    }

    public boolean isOdontologo() {
        return rol == Rol.ODONTOLOGO;
    }

    public boolean isPaciente() {
        return rol == Rol.PACIENTE;
    }

    public String getIniciales() {
        String base = getNombreVisible();
        String[] partes = base.trim().split("\\s+");
        if (partes.length == 1) {
            return partes[0].substring(0, 1).toUpperCase();
        }
        return (partes[0].substring(0, 1) + partes[partes.length - 1].substring(0, 1)).toUpperCase();
    }
}
