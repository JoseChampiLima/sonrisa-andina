package pe.edu.clinica.sonrisa_andina.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.domain.Paciente;
import pe.edu.clinica.sonrisa_andina.exception.ReglaNegocioException;
import pe.edu.clinica.sonrisa_andina.repository.CitaRepository;
import pe.edu.clinica.sonrisa_andina.repository.PacienteRepository;
import pe.edu.clinica.sonrisa_andina.web.form.PacienteForm;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final CitaRepository citaRepository;

    public List<Paciente> listar() {
        return pacienteRepository.findAllByOrderByApellidosAsc();
    }

    public List<Paciente> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return listar();
        }
        return pacienteRepository.buscar(texto.trim());
    }

    public Paciente obtener(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("El paciente solicitado no existe."));
    }

    @Transactional
    public Paciente crear(PacienteForm form) {
        String dni = form.getDni() == null ? "" : form.getDni().trim();
        if (pacienteRepository.existsByDni(dni)) {
            throw new ReglaNegocioException("Ya existe un paciente registrado con el DNI " + dni + ".");
        }
        if (form.getCorreo() != null && !form.getCorreo().isBlank()
                && pacienteRepository.existsByCorreo(form.getCorreo().trim())) {
            throw new ReglaNegocioException("El correo " + form.getCorreo() + " ya esta registrado.");
        }
        Paciente paciente = new Paciente(dni, form.getNombres().trim(), form.getApellidos().trim(),
                form.getTelefono().trim(), normalizarCorreo(form.getCorreo()));
        return pacienteRepository.save(paciente);
    }

    @Transactional
    public Paciente actualizar(Long id, PacienteForm form) {
        Paciente paciente = obtener(id);
        String dni = form.getDni().trim();
        if (!dni.equals(paciente.getDni()) && pacienteRepository.existsByDni(dni)) {
            throw new ReglaNegocioException("Ya existe otro paciente con el DNI " + dni + ".");
        }
        String correo = normalizarCorreo(form.getCorreo());
        if (correo != null && !correo.equals(paciente.getCorreo())
                && pacienteRepository.existsByCorreo(correo)) {
            throw new ReglaNegocioException("El correo " + correo + " ya esta registrado.");
        }
        paciente.setDni(dni);
        paciente.setNombres(form.getNombres().trim());
        paciente.setApellidos(form.getApellidos().trim());
        paciente.setTelefono(form.getTelefono().trim());
        paciente.setCorreo(correo);
        return pacienteRepository.save(paciente);
    }

    @Transactional
    public void eliminar(Long id) {
        Paciente paciente = obtener(id);
        if (!citaRepository.findByPacienteIdOrderByInicioDesc(id).isEmpty()) {
            throw new ReglaNegocioException("No se puede eliminar un paciente con citas registradas. "
                    + "Considere conservar la informacion por trazabilidad.");
        }
        pacienteRepository.delete(paciente);
    }

    public PacienteForm aFormulario(Paciente paciente) {
        PacienteForm form = new PacienteForm();
        form.setDni(paciente.getDni());
        form.setNombres(paciente.getNombres());
        form.setApellidos(paciente.getApellidos());
        form.setTelefono(paciente.getTelefono());
        form.setCorreo(paciente.getCorreo());
        return form;
    }

    private String normalizarCorreo(String correo) {
        return (correo == null || correo.isBlank()) ? null : correo.trim().toLowerCase();
    }
}
