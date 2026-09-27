package com.dominiossolunet.service;

import com.dominiossolunet.dto.ClienteResumen;
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

    /**
     * Obtiene todos los clientes junto con las estadísticas
     * necesarias para la pantalla de gestión.
     *
     * No utiliza cliente.getDominios(), evitando depender
     * de la carga LAZY de la relación.
     */
    @Transactional(readOnly = true)
    public List<ClienteResumen> obtenerResumenClientes() {

        List<Cliente> clientes = clienteRepository.findAll();
        List<Dominio> dominios = dominioRepository.findAll();
        List<Facturacion> facturaciones = facturacionRepository.findAll();

        return clientes.stream()
                .map(cliente -> crearResumen(
                        cliente,
                        dominios,
                        facturaciones))
                .toList();
    }

    private ClienteResumen crearResumen(
            Cliente cliente,
            List<Dominio> dominios,
            List<Facturacion> facturaciones) {

        List<Dominio> dominiosCliente = dominios.stream()
                .filter(dominio ->
                        dominio.getCliente() != null
                                && dominio.getCliente().getId() == cliente.getId())
                .toList();

        long totalDominios = dominiosCliente.size();

        long pendientesRenovacion = dominiosCliente.stream()
                .filter(dominio ->
                        dominio.getEstadoRenovacion()
                                == EstadoRenovacion.PENDIENTE_RENOVACION)
                .count();

        long renovados = dominiosCliente.stream()
                .filter(dominio ->
                        dominio.getEstadoRenovacion()
                                == EstadoRenovacion.RENOVADO)
                .count();

        long rechazados = dominiosCliente.stream()
                .filter(dominio ->
                        dominio.getEstadoRenovacion()
                                == EstadoRenovacion.RECHAZADO)
                .count();

        long pendientesFacturacion = dominiosCliente.stream()
                .filter(dominio ->
                        tieneFacturacionPendiente(
                                dominio,
                                facturaciones))
                .count();

        return new ClienteResumen(
                cliente,
                totalDominios,
                pendientesRenovacion,
                renovados,
                rechazados,
                pendientesFacturacion);
    }

    private boolean tieneFacturacionPendiente(
            Dominio dominio,
            List<Facturacion> facturaciones) {

        return facturaciones.stream()
                .anyMatch(facturacion ->
                        facturacion.getDominio().getId() == dominio.getId()
                                && facturacion.getEstadoFacturacion()
                                == EstadoFacturacion.PENDIENTE_FACTURAR);
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

    @Transactional(readOnly = true)
    public long contarPendientesFacturacion(
            List<Dominio> dominios) {

        List<Facturacion> facturaciones =
                facturacionRepository.findAll();

        return dominios.stream()
                .filter(dominio ->
                        tieneFacturacionPendiente(
                                dominio,
                                facturaciones))
                .count();
    }
}