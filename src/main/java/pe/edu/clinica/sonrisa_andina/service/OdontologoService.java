package pe.edu.clinica.sonrisa_andina.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.clinica.sonrisa_andina.domain.Especialidad;
import pe.edu.clinica.sonrisa_andina.domain.HorarioTrabajo;
import pe.edu.clinica.sonrisa_andina.domain.Odontologo;
import pe.edu.clinica.sonrisa_andina.exception.ReglaNegocioException;
import pe.edu.clinica.sonrisa_andina.repository.EspecialidadRepository;
import pe.edu.clinica.sonrisa_andina.repository.HorarioTrabajoRepository;
import pe.edu.clinica.sonrisa_andina.repository.OdontologoRepository;
import pe.edu.clinica.sonrisa_andina.web.form.OdontologoForm;

@Service
@RequiredArgsConstructor
public class OdontologoService {

    private final OdontologoRepository odontologoRepository;
    private final EspecialidadRepository especialidadRepository;
    private final HorarioTrabajoRepository horarioRepository;

    public List<Odontologo> listar() {
        return odontologoRepository.findAllByOrderByApellidosAsc();
    }

    public List<Odontologo> listarActivos() {
        return odontologoRepository.findByActivoTrueOrderByApellidosAsc();
    }

    public List<Odontologo> listarPorEspecialidad(Long idEspecialidad) {
        return odontologoRepository.findByEspecialidadIdAndActivoTrue(idEspecialidad);
    }

    public Odontologo obtener(Long id) {
        return odontologoRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("El odontologo solicitado no existe."));
    }

    public List<HorarioTrabajo> horarios(Long idOdontologo) {
        return horarioRepository.findByOdontologoIdOrderByDiaSemanaAscHoraInicioAsc(idOdontologo);
    }

    @Transactional
    public Odontologo crear(OdontologoForm form) {
        String dni = form.getDni().trim();
        if (odontologoRepository.existsByDni(dni)) {
            throw new ReglaNegocioException("Ya existe una persona registrada con el DNI " + dni + ".");
        }
        if (odontologoRepository.existsByNroColegiatura(form.getNroColegiatura().trim())) {
            throw new ReglaNegocioException("El numero de colegiatura ya esta registrado.");
        }
        Especialidad especialidad = especialidad(form.getIdEspecialidad());
        Odontologo odontologo = new Odontologo(dni, form.getNombres().trim(), form.getApellidos().trim(),
                form.getTelefono().trim(), especialidad, form.getNroColegiatura().trim());
        odontologo.setActivo(form.isActivo());
        odontologoRepository.save(odontologo);
        guardarHorarios(odontologo, form);
        return odontologo;
    }

    @Transactional
    public Odontologo actualizar(Long id, OdontologoForm form) {
        Odontologo odontologo = obtener(id);
        String dni = form.getDni().trim();
        if (!dni.equals(odontologo.getDni()) && odontologoRepository.existsByDni(dni)) {
            throw new ReglaNegocioException("Ya existe otra persona con el DNI " + dni + ".");
        }
        String cop = form.getNroColegiatura().trim();
        if (!cop.equals(odontologo.getNroColegiatura())
                && odontologoRepository.existsByNroColegiatura(cop)) {
            throw new ReglaNegocioException("El numero de colegiatura " + cop + " ya esta registrado.");
        }
        odontologo.setDni(dni);
        odontologo.setNombres(form.getNombres().trim());
        odontologo.setApellidos(form.getApellidos().trim());
        odontologo.setTelefono(form.getTelefono().trim());
        odontologo.setEspecialidad(especialidad(form.getIdEspecialidad()));
        odontologo.setNroColegiatura(cop);
        odontologo.setActivo(form.isActivo());
        odontologoRepository.save(odontologo);
        guardarHorarios(odontologo, form);
        return odontologo;
    }

    @Transactional
    public void cambiarEstado(Long id, boolean activo) {
        Odontologo odontologo = obtener(id);
        odontologo.setActivo(activo);
        odontologoRepository.save(odontologo);
    }

    @Transactional
    public void guardarHorarios(Odontologo odontologo, OdontologoForm form) {
        if (form.getHoraFin() == null || form.getHoraInicio() == null
                || !form.getHoraFin().isAfter(form.getHoraInicio())) {
            throw new ReglaNegocioException("La hora de fin debe ser mayor que la hora de inicio.");
        }
        if (form.getDias() == null || form.getDias().isEmpty()) {
            throw new ReglaNegocioException("Seleccione al menos un dia de atencion.");
        }
        horarioRepository.deleteByOdontologoId(odontologo.getId());
        horarioRepository.flush();
        for (Integer dia : form.getDias()) {
            if (dia == null || dia < 1 || dia > 7) {
                continue;
            }
            horarioRepository.save(new HorarioTrabajo(odontologo, dia,
                    form.getHoraInicio(), form.getHoraFin()));
        }
    }

    public OdontologoForm aFormulario(Odontologo odontologo) {
        OdontologoForm form = new OdontologoForm();
        form.setDni(odontologo.getDni());
        form.setNombres(odontologo.getNombres());
        form.setApellidos(odontologo.getApellidos());
        form.setTelefono(odontologo.getTelefono());
        form.setIdEspecialidad(odontologo.getEspecialidad().getId());
        form.setNroColegiatura(odontologo.getNroColegiatura());
        form.setActivo(odontologo.isActivo());
        List<HorarioTrabajo> bloques = horarios(odontologo.getId());
        if (!bloques.isEmpty()) {
            form.setDias(bloques.stream().map(HorarioTrabajo::getDiaSemana).distinct().sorted().toList());
            form.setHoraInicio(bloques.get(0).getHoraInicio());
            form.setHoraFin(bloques.get(0).getHoraFin());
        }
        return form;
    }

    private Especialidad especialidad(Long id) {
        if (id == null) {
            throw new ReglaNegocioException("Debe seleccionar una especialidad.");
        }
        return especialidadRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("La especialidad seleccionada no existe."));
    }
}
