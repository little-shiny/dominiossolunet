package com.dominiossolunet.service;

import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.Dominio;
import com.dominiossolunet.model.Facturacion;
import com.dominiossolunet.model.enums.EstadoFacturacion;
import com.dominiossolunet.model.enums.EstadoRenovacion;
import com.dominiossolunet.repository.ClienteRepository;
import com.dominiossolunet.repository.DominioRepository;
import com.dominiossolunet.repository.FacturacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final DominioRepository dominioRepository;
    private final FacturacionRepository facturacionRepository;

    public ClienteService(
            ClienteRepository clienteRepository,
            DominioRepository dominioRepository,
            FacturacionRepository facturacionRepository) {

        this.clienteRepository = clienteRepository;
        this.dominioRepository = dominioRepository;
        this.facturacionRepository = facturacionRepository;
    }

    @Transactional(readOnly = true)
    public List<Cliente> obtenerTodosLosClientes() {
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Cliente obtenerCliente(int idCliente) {
        return clienteRepository.findById(idCliente)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cliente no encontrado: " + idCliente));
    }

    @Transactional(readOnly = true)
    public List<Dominio> obtenerDominiosCliente(int idCliente) {
        return dominioRepository.findAll()
                .stream()
                .filter(dominio ->
                        dominio.getCliente() != null
                                && dominio.getCliente().getId() == idCliente)
                .toList();
    }

    public long contarDominios(List<Dominio> dominios) {
        return dominios.size();
    }

    public long contarPendientesRenovacion(List<Dominio> dominios) {
        return dominios.stream()
                .filter(dominio ->
                        dominio.getEstadoRenovacion()
                                == EstadoRenovacion.PENDIENTE_RENOVACION)
                .count();
    }

    public long contarRenovados(List<Dominio> dominios) {
        return dominios.stream()
                .filter(dominio ->
                        dominio.getEstadoRenovacion()
                                == EstadoRenovacion.RENOVADO)
                .count();
    }

    public long contarRechazados(List<Dominio> dominios) {
        return dominios.stream()
                .filter(dominio ->
                        dominio.getEstadoRenovacion()
                                == EstadoRenovacion.RECHAZADO)
                .count();
    }

    public long contarPendientesFacturacion(List<Dominio> dominios) {

        List<Facturacion> facturaciones =
                facturacionRepository.findAll();

        return dominios.stream()
                .filter(dominio ->
                        facturaciones.stream().anyMatch(facturacion ->
                                facturacion.getDominio().getId() == dominio.getId()
                                        && facturacion.getEstadoFacturacion()
                                        == EstadoFacturacion.PENDIENTE_FACTURAR))
                .count();
    }
}