package com.api.bicicletario.service;

import com.api.bicicletario.exception.PagamentoNaoAutorizadoException;
import com.api.bicicletario.model.Cobranca;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class CobrancaServiceTest {
    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private CobrancaService cobrancaService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testObterCobranca_Exists() {
        Cobranca cobranca1 = new Cobranca(1, "Aguardando pagamento", LocalDateTime.now(), LocalDateTime.now().plusHours(1), 50.0, 3, "1234566789");
        Cobranca cobranca2 = new Cobranca(2, "Aguardando pagamento", LocalDateTime.now(), LocalDateTime.now().plusHours(1), 50.0, 3, "9877453112");

        List<Cobranca> cobrancas = new ArrayList<>();
        cobrancas.add(cobranca1);
        cobrancas.add(cobranca2);
        cobrancaService.setCobrancas(cobrancas);

        Cobranca result = cobrancaService.obterCobranca(cobranca1.getId());

        assertEquals(cobranca1, result);
    }

    @Test
    void testObterCobranca_NotExists() {
        Cobranca cobranca1 = new Cobranca(1, "Aguardando pagamento", LocalDateTime.now(), LocalDateTime.now().plusHours(1), 50.0, 3, "1234566789");
        Cobranca cobranca2 = new Cobranca(2, "Aguardando pagamento", LocalDateTime.now(), LocalDateTime.now().plusHours(1), 50.0, 3, "9877453112");

        List<Cobranca> cobrancas = new ArrayList<>();
        cobrancas.add(cobranca1);
        cobrancas.add(cobranca2);
        cobrancaService.setCobrancas(cobrancas);

        Cobranca result = cobrancaService.obterCobranca(3);

        assertNull(result);
    }

    @Test
    public void testObterCobrancasAtrasadas() {
        Cobranca cobrancaAtrasada = new Cobranca();
        cobrancaAtrasada.setStatus("PENDENTE");
        cobrancaAtrasada.setHoraSolicitacao(LocalDateTime.now().minusHours(13));

        Cobranca cobrancaNaoAtrasada = new Cobranca();
        cobrancaNaoAtrasada.setStatus("PENDENTE");
        cobrancaNaoAtrasada.setHoraSolicitacao(LocalDateTime.now().minusHours(2));


        List<Cobranca> cobrancas = new ArrayList<>();
        cobrancas.add(cobrancaAtrasada);
        cobrancas.add(cobrancaNaoAtrasada);
        cobrancaService.setCobrancas(cobrancas);

        List<Cobranca> cobrancasAtrasadas = cobrancaService.obterCobrancasAtrasadas();

        assertEquals(1, cobrancasAtrasadas.size());
        assertEquals(cobrancaAtrasada, cobrancasAtrasadas.get(0));
    }

    @Test
    public void testRealizarCobrancaPagamentoAutorizado() {
        Cobranca cobranca = new Cobranca(1, "Aguardando pagamento", LocalDateTime.now(), LocalDateTime.now().plusHours(1), 50.0, 3, "1234566789");
        cobranca.setValor(10.0);
        cobranca.setCartao("1234566789");

        List<Cobranca> cobrancas = new ArrayList<>();
        cobrancas.add(cobranca);
        cobrancaService.setCobrancas(cobrancas);

        boolean resultado = cobrancaService.processarPagamento(cobranca.getValor(), cobranca.getCartao());

        assertTrue(resultado);
        assertEquals("Aguardando pagamento", cobranca.getStatus());
        assertNotNull(cobranca.getHoraFinalizacao());
    }

    @Test
    void realizarCobranca_PagamentoNaoAutorizado_ExceptionLancada() {
        // Dados de teste
        Cobranca cobranca = new Cobranca();
        cobranca.setId(2);
        cobranca.setValor(200);
        cobranca.setCartao("");

        // Verifica se a exceção é lançada
        assertThrows(PagamentoNaoAutorizadoException.class, () -> cobrancaService.realizarCobranca(cobranca));
    }

    @Test
    public void testEnviarNotificacao() {
        Cobranca cobranca = new Cobranca(1, "Aguardando pagamento", LocalDateTime.now(), LocalDateTime.now().plusHours(1), 50.0, 3, "1234566789");
        cobranca.setCiclista(1);
        cobranca.setHoraSolicitacao(LocalDateTime.now());
        cobranca.setValor(50.0);

        cobrancaService.enviarNotificacao(cobranca);

        verify(notificacaoService, times(1)).enviarEmail(anyString(), anyString(), anyString());
    }

    @Test
    public void testProcessarPagamento() {
        double valor = 100.0;
        String cartao = "1234567890";

        assertTrue(cobrancaService.processarPagamento(valor, cartao));
    }

    @Test
    public void testCriarMensagemNotificacao() {
        Cobranca cobranca = new Cobranca(1, "Aguardando pagamento", LocalDateTime.now(), LocalDateTime.now().plusHours(1), 50.0, 3, "1234566789");
        cobranca.setCiclista(1);
        cobranca.setHoraSolicitacao(LocalDateTime.now());
        cobranca.setValor(100.0);

        String mensagem = cobrancaService.criarMensagemNotificacao(cobranca);

        String mensagemEsperada = "Caro(a) Ciclista " + cobranca.getCiclista() + ",\n\n" +
                "De acordo com nossos registros, identificamos uma cobrança em atraso para a devolução da bicicleta.\n" +
                "Data da cobrança: " + cobranca.getHoraSolicitacao() + "\n" +
                "Valor da cobrança: " + cobranca.getValor() + "\n\n" +
                "Atenciosamente,\n" +
                "Equipe do sistema de aluguel de bicicletas";
        assertEquals(mensagemEsperada, mensagem);
    }

    @Test
    public void testAdicionarCobrancaEmFila() {
        // Criação do objeto mock para a classe CobrancaService
        CobrancaService cobrancaServiceMock = mock(CobrancaService.class);

        // Criação das cobranças para teste
        Cobranca cobranca1 = new Cobranca(1, "Aguardando pagamento", LocalDateTime.now(), LocalDateTime.now().plusHours(1), 50.0, 3, "1234566789");
        Cobranca cobranca2 = new Cobranca(2, "Aguardando pagamento", LocalDateTime.now(), LocalDateTime.now().plusHours(1), 50.0, 3, "9877453112");

        // Chamada do método a ser testado
        cobrancaServiceMock.adicionarCobrancaEmFila(cobranca1);
        cobrancaServiceMock.adicionarCobrancaEmFila(cobranca2);

        // Verificação se o método adicionarCobrancaEmFila foi chamado corretamente
        verify(cobrancaServiceMock).adicionarCobrancaEmFila(cobranca1);
        verify(cobrancaServiceMock).adicionarCobrancaEmFila(cobranca2);
    }

    @Test
    public void testValidarCartao_CartaoValido() {
        // Cenário
        String numeroCartaoValido = "4111 1111 1111 1111";

        // Verificação
        assertTrue(cobrancaService.validarCartao(numeroCartaoValido));
    }

    @Test
    public void testValidarCartao_CartaoInvalido_CaracteresNaoNumericos() {
        // Cenário
        String numeroCartaoInvalido = "4111 1111 A111 1111";

        // Verificação
        assertFalse(cobrancaService.validarCartao(numeroCartaoInvalido));
    }

    @Test
    public void testValidarCartao_CartaoInvalido_CartaoVazio() {
        // Cenário
        String numeroCartaoInvalido = "";

        // Verificação
        assertFalse(cobrancaService.validarCartao(numeroCartaoInvalido));
    }

    @Test
    public void testValidarCartao_CartaoInvalido_DigitoAlterado() {
        // Cenário
        String numeroCartaoInvalido = "4111 1111 1111 1121";

        // Verificação
        assertFalse(cobrancaService.validarCartao(numeroCartaoInvalido));
    }
}
