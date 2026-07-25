package com.dominiossolunet.service;

import com.dominiossolunet.dto.ResultadoValidacionRec;
import com.dominiossolunet.model.Cliente;
import com.dominiossolunet.model.TokenCliente;
import com.dominiossolunet.model.enums.ResultadoValidacion;
import com.dominiossolunet.repository.TokenClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenClienteRepository tokenClienteRepository;

    @InjectMocks
    private TokenService tokenService;

    // TESTS
    @Test
    void validarToken_cuandoTokenValido_devuelveResultadoValido(){

        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        cliente.setEmail("ana@ana.com");

        //Token valido
        TokenCliente token = new TokenCliente();
        token.setToken("abc123");
        token.setUsado(false);
        token.setFechaCreacion(LocalDateTime.now());
        token.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        token.setCliente(cliente);

        when(tokenClienteRepository.findByToken("abc123")).thenReturn(Optional.of(token));

        ResultadoValidacionRec resultado = tokenService.validarToken("abc123");

        assertThat(resultado.resultado()).isEqualTo(ResultadoValidacion.VALIDO);
        assertThat(resultado.token()).isEqualTo(token);
        assertThat(resultado.token().getCliente().getNombre()).isEqualTo("Ana");

    }
}