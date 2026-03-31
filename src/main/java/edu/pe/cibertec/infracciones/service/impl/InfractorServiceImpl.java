package edu.pe.cibertec.infracciones.service.impl;

import edu.pe.cibertec.infracciones.dto.InfractorRequestDTO;
import edu.pe.cibertec.infracciones.dto.InfractorResponseDTO;
import edu.pe.cibertec.infracciones.exception.InfractorNotFoundException;
import edu.pe.cibertec.infracciones.exception.VehiculoNotFoundException;
import edu.pe.cibertec.infracciones.model.Infractor;
import edu.pe.cibertec.infracciones.model.Vehiculo;
import edu.pe.cibertec.infracciones.repository.InfractorRepository;
import edu.pe.cibertec.infracciones.repository.VehiculoRepository;
import edu.pe.cibertec.infracciones.service.IInfractorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import edu.pe.cibertec.infracciones.repository.MultaRepository;
import edu.pe.cibertec.infracciones.model.Multa;
import edu.pe.cibertec.infracciones.model.EstadoMulta;

@Service
@RequiredArgsConstructor
public class InfractorServiceImpl implements IInfractorService {

    private final InfractorRepository infractorRepository;
    private final VehiculoRepository vehiculoRepository;
    private final MultaRepository   multaRepository;

    @Override
    public InfractorResponseDTO registrarInfractor(InfractorRequestDTO dto) {
        Infractor infractor = new Infractor();
        infractor.setDni(dto.getDni());
        infractor.setNombre(dto.getNombre());
        infractor.setApellido(dto.getApellido());
        infractor.setEmail(dto.getEmail());
        infractor.setBloqueado(false);
        return mapToResponse(infractorRepository.save(infractor));
    }

    @Override
    public InfractorResponseDTO obtenerInfractorPorId(Long id) {
        Infractor infractor = infractorRepository.findById(id)
                .orElseThrow(() -> new InfractorNotFoundException(id));
        return mapToResponse(infractor);
    }

    @Override
    public List<InfractorResponseDTO> obtenerTodos() {
        return infractorRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }




    private InfractorResponseDTO mapToResponse(Infractor infractor) {
        InfractorResponseDTO dto = new InfractorResponseDTO();
        dto.setId(infractor.getId());
        dto.setDni(infractor.getDni());
        dto.setNombre(infractor.getNombre());
        dto.setApellido(infractor.getApellido());
        dto.setEmail(infractor.getEmail());
        dto.setBloqueado(infractor.isBloqueado());
        return dto;
    }

    @Override
    public double calcularDeuda(Long infractorId) {

        List<Multa> multas = multaRepository.findByInfractor_Id(infractorId);

        double total = 0;

        for (Multa multa : multas) {

            if (multa.getEstado() == EstadoMulta.PENDIENTE) {
                total += multa.getMonto();
            }

            if (multa.getEstado() == EstadoMulta.VENCIDA) {
                total += multa.getMonto() * 1.15;
            }
        }

        return total;
    }


    @Override
    public void desasignarVehiculo(Long infractorId, Long vehiculoId) {

        // 1. Buscar infractor
        Infractor infractor = infractorRepository.findById(infractorId)
                .orElseThrow(() -> new RuntimeException("Infractor no encontrado"));

        // 2. Validar multas pendientes
        List<Multa> multasPendientes = multaRepository
                .findByVehiculo_IdAndEstado(vehiculoId, EstadoMulta.PENDIENTE);

        if (!multasPendientes.isEmpty()) {
            throw new RuntimeException("No se puede desasignar, tiene multas pendientes");
        }

        // 3. Remover vehículo
        infractor.getVehiculos()
                .removeIf(v -> v.getId().equals(vehiculoId));

        // 4. Guardar cambios
        infractorRepository.save(infractor);
    }


}