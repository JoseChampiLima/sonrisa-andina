package pe.edu.clinica.sonrisa_andina.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.domain.Especialidad;
import pe.edu.clinica.sonrisa_andina.domain.Servicio;
import pe.edu.clinica.sonrisa_andina.exception.ReglaNegocioException;
import pe.edu.clinica.sonrisa_andina.repository.EspecialidadRepository;
import pe.edu.clinica.sonrisa_andina.repository.ServicioRepository;
import pe.edu.clinica.sonrisa_andina.web.form.ServicioForm;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final EspecialidadRepository especialidadRepository;

    public List<Servicio> listar() {
        return servicioRepository.findAllByOrderByNombreAsc();
    }

    public List<Servicio> listarActivos() {
        return servicioRepository.findByActivoTrueOrderByNombreAsc();
    }

    public Servicio obtener(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("El servicio solicitado no existe."));
    }

    @Transactional
    public Servicio crear(ServicioForm form) {
        String nombre = form.getNombre().trim();
        if (servicioRepository.existsByNombre(nombre)) {
            throw new ReglaNegocioException("Ya existe un servicio con el nombre " + nombre + ".");
        }
        Servicio servicio = new Servicio(nombre, form.getDuracionMin(), form.getPrecio());
        servicio.setActivo(form.isActivo());
        servicio.setEspecialidades(especialidades(form.getEspecialidades()));
        return servicioRepository.save(servicio);
    }

    @Transactional
    public Servicio actualizar(Long id, ServicioForm form) {
        Servicio servicio = obtener(id);
        String nombre = form.getNombre().trim();
        if (!nombre.equalsIgnoreCase(servicio.getNombre()) && servicioRepository.existsByNombre(nombre)) {
            throw new ReglaNegocioException("Ya existe otro servicio con el nombre " + nombre + ".");
        }
        servicio.setNombre(nombre);
        servicio.setDuracionMin(form.getDuracionMin());
        servicio.setPrecio(form.getPrecio());
        servicio.setActivo(form.isActivo());
        servicio.setEspecialidades(especialidades(form.getEspecialidades()));
        return servicioRepository.save(servicio);
    }

    @Transactional
    public void cambiarEstado(Long id, boolean activo) {
        Servicio servicio = obtener(id);
        servicio.setActivo(activo);
        servicioRepository.save(servicio);
    }

    public ServicioForm aFormulario(Servicio servicio) {
        ServicioForm form = new ServicioForm();
        form.setNombre(servicio.getNombre());
        form.setDuracionMin(servicio.getDuracionMin());
        form.setPrecio(servicio.getPrecio());
        form.setActivo(servicio.isActivo());
        form.setEspecialidades(servicio.getEspecialidades().stream().map(Especialidad::getId).toList());
        return form;
    }

    private java.util.Set<Especialidad> especialidades(List<Long> ids) {
        List<Especialidad> lista = new ArrayList<>();
        if (ids != null) {
            lista = especialidadRepository.findAllById(ids);
        }
        if (lista.isEmpty()) {
            throw new ReglaNegocioException("Seleccione al menos una especialidad habilitada para el servicio.");
        }
        return new java.util.HashSet<>(lista);
    }
}
