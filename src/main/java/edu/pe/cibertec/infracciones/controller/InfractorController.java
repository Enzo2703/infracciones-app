package edu.pe.cibertec.infracciones.controller;

import edu.pe.cibertec.infracciones.dto.InfractorRequestDTO;
import edu.pe.cibertec.infracciones.dto.InfractorResponseDTO;
import edu.pe.cibertec.infracciones.service.IInfractorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/infractores")
@RequiredArgsConstructor
public class InfractorController {

    private final IInfractorService infractorService;

    @PostMapping
    public ResponseEntity<InfractorResponseDTO> registrar(@RequestBody InfractorRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(infractorService.registrarInfractor(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InfractorResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(infractorService.obtenerInfractorPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<InfractorResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(infractorService.obtenerTodos());
    }
    @DeleteMapping("/{infractorId}/vehiculos/{vehiculoId}")
    public ResponseEntity<Void> desasignarVehiculo(
            @PathVariable Long infractorId,
            @PathVariable Long vehiculoId) {

        infractorService.desasignarVehiculo(infractorId, vehiculoId);
        return ResponseEntity.noContent().build();
    }


}