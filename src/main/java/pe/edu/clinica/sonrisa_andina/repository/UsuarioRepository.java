package pe.edu.clinica.sonrisa_andina.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.clinica.sonrisa_andina.domain.Rol;
import pe.edu.clinica.sonrisa_andina.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<Usuario> findByPersonaId(Long idPersona);

    boolean existsByPersonaId(Long idPersona);

    List<Usuario> findAllByOrderByUsernameAsc();

    long countByRol(Rol rol);
}
