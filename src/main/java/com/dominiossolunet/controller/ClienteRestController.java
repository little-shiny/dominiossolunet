package com.dominiossolunet.controller;

import com.dominiossolunet.dto.ClienteDetalleResponse;
import com.dominiossolunet.dto.ClienteResumen;
import com.dominiossolunet.dto.ClienteResumenResponse;
import com.dominiossolunet.dto.DominioClienteResponse;
import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.service.ClienteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteRestController {

    private final ClienteService clienteService;

    public ClienteRestController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<ClienteResumenResponse> listarClientes() {
        return clienteService.obtenerResumenClientes()
                .stream()
                .map(this::convertirARespuesta)
                .toList();
    }

    @GetMapping("/{id}")
    public ClienteDetalleResponse detalleCliente(@PathVariable int id) {
        var cliente = clienteService.obtenerCliente(id);
        var dominios = clienteService.obtenerDominiosCliente(id);

        var dominiosRespuesta = dominios.stream()
                .map(dominio -> new DominioClienteResponse(
                        dominio.getId(),
                        dominio.getNombreDominio(),
                        dominio.getFechaExpiracion(),
                        dominio.getEstado(),
                        dominio.getEstadoRenovacion(),
                        dominio.getRegistrador(),
                        dominio.getUltimoAviso()
                ))
                .toList();

        return new ClienteDetalleResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getEmail(),
                clienteService.contarDominios(dominios),
                clienteService.contarPendientesRenovacion(dominios),
                clienteService.contarRenovados(dominios),
                clienteService.contarRechazados(dominios),
                clienteService.contarPendientesFacturacion(dominios),
                dominiosRespuesta
        );
    }

    private ClienteResumenResponse convertirARespuesta(ClienteResumen resumen) {
        Cliente cliente = resumen.getCliente();

        return new ClienteResumenResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getEmail(),
                resumen.getTotalDominios(),
                resumen.getPendientesRenovacion(),
                resumen.getRenovados(),
                resumen.getRechazados(),
                resumen.getPendientesFacturacion()
        );
    }
}