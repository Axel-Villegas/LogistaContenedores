package com.transporte.ms_solicitudes.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transporte.ms_solicitudes.model.EstadoSolicitud;
import com.transporte.ms_solicitudes.model.Solicitud;
import com.transporte.ms_solicitudes.service.SolicitudesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
public class SolicitudesControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private SolicitudesService service;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        public void testCrearSolicitud() throws Exception {
                com.transporte.ms_solicitudes.api.dto.SolicitudRequestDTO req = new com.transporte.ms_solicitudes.api.dto.SolicitudRequestDTO(
                                "cliente-1",
                                "contenedor-1",
                                new com.transporte.ms_solicitudes.api.dto.SolicitudRequestDTO.Localizacion("Origen", 0.0, 0.0),
                                new com.transporte.ms_solicitudes.api.dto.SolicitudRequestDTO.Localizacion("Destino", 10.0, 10.0));

                Solicitud solicitud = Solicitud.builder()
                                .nroSolicitud("12345678")
                                .estado(EstadoSolicitud.BORRADOR)
                                .build();

                when(service.crearSolicitud(anyString(), anyString(), anyDouble(), anyDouble(), anyDouble(),
                                anyDouble()))
                                .thenReturn(solicitud);

                mockMvc.perform(post("/api/v1/solicitudes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.nroSolicitud").value("12345678"))
                                .andExpect(jsonPath("$.estado").value("BORRADOR"));
        }

        @Test
        public void testCrearSolicitudInvalid() throws Exception {
                // Missing body
                mockMvc.perform(post("/api/v1/solicitudes")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void testAceptarSolicitud() throws Exception {
                Solicitud solicitud = Solicitud.builder()
                                .nroSolicitud("12345678")
                                .estado(EstadoSolicitud.ACEPTADA)
                                .build();

                when(service.aceptarSolicitud("12345678")).thenReturn(Optional.of(solicitud));

                mockMvc.perform(put("/api/v1/solicitudes/12345678/aceptar"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.estado").value("ACEPTADA"));
        }
}
