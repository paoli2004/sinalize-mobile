package br.com.sinalize.api.service;

import br.com.sinalize.api.dto.palavra.PalavraAtualizacaoRequest;
import br.com.sinalize.api.dto.palavra.PalavraRequest;
import br.com.sinalize.api.dto.palavra.PalavraResponse;
import br.com.sinalize.api.dto.palavra.PalavraResumoResponse;
import br.com.sinalize.api.entity.Categoria;
import br.com.sinalize.api.entity.Palavra;
import br.com.sinalize.api.entity.Usuario;
import br.com.sinalize.api.exception.ConflitoException;
import br.com.sinalize.api.exception.RecursoNaoEncontradoException;
import br.com.sinalize.api.repository.PalavraRepository;
import br.com.sinalize.api.repository.QuizPalavraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PalavraServiceTest {

    @Mock
    private PalavraRepository palavraRepository;

    @Mock
    private QuizPalavraRepository quizPalavraRepository;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private UsuarioLogadoProvider usuarioLogado;

    @InjectMocks
    private PalavraService palavraService;

    private Usuario admin;
    private Categoria cores;
    private Categoria alimentos;

    @BeforeEach
    void setUp() {
        admin = new Usuario("Administrador", "admin@sinalize.com.br", "hash");
        cores = categoria(3L, "Cores");
        alimentos = categoria(5L, "Alimentos");
    }

    private Categoria categoria(Long id, String nome) {
        Categoria categoria = new Categoria(nome, "Descrição", admin);
        ReflectionTestUtils.setField(categoria, "id", id);
        return categoria;
    }

    private Palavra palavra(Long id, String texto, Categoria... categorias) {
        Palavra palavra = new Palavra(texto, List.of(categorias), admin);
        ReflectionTestUtils.setField(palavra, "id", id);
        return palavra;
    }

    @Test
    void criarAssociaCategoriasERegistraAutor() {
        when(categoriaService.buscarEntidadesPorIds(List.of(3L, 5L))).thenReturn(List.of(cores, alimentos));
        when(usuarioLogado.obter()).thenReturn(admin);
        when(palavraRepository.save(any(Palavra.class))).thenAnswer(inv -> inv.getArgument(0));

        PalavraResponse resposta = palavraService.criar(new PalavraRequest(" Laranja ", List.of(3L, 5L)));

        ArgumentCaptor<Palavra> salva = ArgumentCaptor.forClass(Palavra.class);
        verify(palavraRepository).save(salva.capture());
        assertThat(salva.getValue().getPalavra()).isEqualTo("Laranja");
        assertThat(salva.getValue().getCriadoPor()).isSameAs(admin);
        // categorias da resposta saem em ordem alfabética
        assertThat(resposta.categorias()).extracting("nome").containsExactly("Alimentos", "Cores");
    }

    @Test
    void criarComCategoriaInexistentePropagaNaoEncontradoSemSalvar() {
        when(categoriaService.buscarEntidadesPorIds(List.of(99L)))
                .thenThrow(new RecursoNaoEncontradoException("Categoria com id 99 não encontrada."));

        assertThatThrownBy(() -> palavraService.criar(new PalavraRequest("Lilás", List.of(99L))))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Categoria com id 99 não encontrada.");
        verify(palavraRepository, never()).save(any());
    }

    @Test
    void atualizarSubstituiCategoriasQuandoInformadas() {
        Palavra laranja = palavra(10L, "Laranja", cores);
        when(palavraRepository.findDetalhadaById(10L)).thenReturn(Optional.of(laranja));
        when(categoriaService.buscarEntidadesPorIds(List.of(5L))).thenReturn(List.of(alimentos));
        when(usuarioLogado.obter()).thenReturn(admin);

        PalavraResponse resposta = palavraService.atualizar(10L, new PalavraAtualizacaoRequest(null, List.of(5L)));

        assertThat(resposta.palavra()).isEqualTo("Laranja");
        assertThat(resposta.categorias()).extracting("nome").containsExactly("Alimentos");
        assertThat(resposta.atualizadoPor()).isNotNull();
    }

    @Test
    void atualizarSemCategoriasMantemAsAtuais() {
        Palavra laranja = palavra(10L, "Laranja", cores);
        when(palavraRepository.findDetalhadaById(10L)).thenReturn(Optional.of(laranja));
        when(usuarioLogado.obter()).thenReturn(admin);

        PalavraResponse resposta = palavraService.atualizar(10L, new PalavraAtualizacaoRequest("Laranjas", null));

        assertThat(resposta.palavra()).isEqualTo("Laranjas");
        assertThat(resposta.categorias()).extracting("nome").containsExactly("Cores");
        verify(categoriaService, never()).buscarEntidadesPorIds(any());
    }

    @Test
    void excluirPalavraUsadaEmQuizLancaConflito() {
        when(palavraRepository.existsById(4L)).thenReturn(true);
        when(quizPalavraRepository.existsByPalavraId(4L)).thenReturn(true);

        assertThatThrownBy(() -> palavraService.excluir(4L))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("A palavra não pode ser excluída porque já foi usada em um quiz.");
        verify(palavraRepository, never()).deleteById(any());
    }

    @Test
    void excluirInexistenteLancaNaoEncontrado() {
        when(palavraRepository.existsById(4L)).thenReturn(false);

        assertThatThrownBy(() -> palavraService.excluir(4L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Palavra não encontrada.");
    }

    @Test
    void excluirPalavraNuncaUsadaApaga() {
        when(palavraRepository.existsById(4L)).thenReturn(true);
        when(quizPalavraRepository.existsByPalavraId(4L)).thenReturn(false);

        palavraService.excluir(4L);

        verify(palavraRepository).deleteById(4L);
    }

    @Test
    void sortearSemExclusoesUsaConsultaSimples() {
        when(palavraRepository.sortear(3)).thenReturn(List.of(palavra(1L, "Gato", cores)));

        List<PalavraResumoResponse> sorteadas = palavraService.sortearAleatorias(List.of(), 3);

        assertThat(sorteadas).containsExactly(new PalavraResumoResponse(1L, "Gato"));
        verify(palavraRepository, never()).sortearExcluindo(any(), anyInt());
    }

    @Test
    void sortearComExclusoesUsaConsultaComFiltro() {
        when(palavraRepository.sortearExcluindo(List.of(7L), 3)).thenReturn(List.of());

        palavraService.sortearAleatorias(List.of(7L), 3);

        verify(palavraRepository, never()).sortear(anyInt());
    }
}
