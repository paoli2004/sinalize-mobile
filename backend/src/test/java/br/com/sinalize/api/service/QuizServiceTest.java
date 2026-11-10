package br.com.sinalize.api.service;

import br.com.sinalize.api.dto.quiz.QuizPalavraResponse;
import br.com.sinalize.api.dto.quiz.QuizRequest;
import br.com.sinalize.api.dto.quiz.QuizResponse;
import br.com.sinalize.api.dto.quiz.RespostaRequest;
import br.com.sinalize.api.entity.Categoria;
import br.com.sinalize.api.entity.Palavra;
import br.com.sinalize.api.entity.Quiz;
import br.com.sinalize.api.entity.QuizPalavra;
import br.com.sinalize.api.entity.Usuario;
import br.com.sinalize.api.exception.ConflitoException;
import br.com.sinalize.api.exception.RecursoNaoEncontradoException;
import br.com.sinalize.api.exception.RegraNegocioException;
import br.com.sinalize.api.repository.PalavraRepository;
import br.com.sinalize.api.repository.QuizPalavraRepository;
import br.com.sinalize.api.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class QuizServiceTest {

    private static final int QUANTIDADE_PALAVRAS = 5;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizPalavraRepository quizPalavraRepository;

    @Mock
    private PalavraRepository palavraRepository;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private UsuarioLogadoProvider usuarioLogado;

    private QuizService quizService;
    private Usuario admin;
    private Categoria cores;

    @BeforeEach
    void setUp() {
        quizService = new QuizService(quizRepository, quizPalavraRepository, palavraRepository,
                categoriaService, usuarioLogado, QUANTIDADE_PALAVRAS);
        admin = new Usuario("Administrador", "admin@sinalize.com.br", "hash");
        cores = new Categoria("Cores", "d", admin);
        ReflectionTestUtils.setField(cores, "id", 3L);
    }

    private Palavra palavra(Long id, String texto) {
        Palavra palavra = new Palavra(texto, List.of(cores), admin);
        ReflectionTestUtils.setField(palavra, "id", id);
        return palavra;
    }

    @Test
    void criarSorteiaPalavrasDasCategoriasNaOrdemDoSorteio() {
        when(categoriaService.buscarEntidadesPorIds(List.of(3L))).thenReturn(List.of(cores));
        when(palavraRepository.sortearPorCategorias(List.of(3L), QUANTIDADE_PALAVRAS))
                .thenReturn(List.of(palavra(20L, "Verde"), palavra(10L, "Azul")));
        when(usuarioLogado.obter()).thenReturn(admin);
        when(quizRepository.save(any(Quiz.class))).thenAnswer(inv -> inv.getArgument(0));

        QuizResponse quiz = quizService.criar(new QuizRequest(List.of(3L)));

        assertThat(quiz.usuario().nome()).isEqualTo("Administrador");
        assertThat(quiz.quizPalavras()).extracting(item -> item.palavra().palavra())
                .containsExactly("Verde", "Azul");
        assertThat(quiz.quizPalavras()).allSatisfy(item -> {
            assertThat(item.acertou()).isNull();
            assertThat(item.respondidoEm()).isNull();
        });
    }

    @Test
    void criarComCategoriasSemPalavrasLancaRegraNegocio() {
        when(categoriaService.buscarEntidadesPorIds(List.of(3L))).thenReturn(List.of(cores));
        when(palavraRepository.sortearPorCategorias(List.of(3L), QUANTIDADE_PALAVRAS)).thenReturn(List.of());

        assertThatThrownBy(() -> quizService.criar(new QuizRequest(List.of(3L))))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não há palavras cadastradas nas categorias selecionadas.");
        verify(quizRepository, never()).save(any());
    }

    @Test
    void criarComCategoriaInexistenteNaoSorteia() {
        when(categoriaService.buscarEntidadesPorIds(List.of(99L)))
                .thenThrow(new RecursoNaoEncontradoException("Categoria com id 99 não encontrada."));

        assertThatThrownBy(() -> quizService.criar(new QuizRequest(List.of(99L))))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(palavraRepository, never()).sortearPorCategorias(any(), anyInt());
    }

    @Test
    void responderRegistraAcertoEData() {
        Quiz quiz = new Quiz(admin);
        QuizPalavra item = quiz.adicionarPalavra(palavra(10L, "Azul"));
        when(quizPalavraRepository.findComPalavraById(7L)).thenReturn(Optional.of(item));

        QuizPalavraResponse resposta = quizService.responder(7L, new RespostaRequest(true));

        assertThat(resposta.acertou()).isTrue();
        assertThat(resposta.respondidoEm()).isNotNull();
        assertThat(resposta.palavra().palavra()).isEqualTo("Azul");
    }

    @Test
    void responderDuasVezesLancaConflito() {
        Quiz quiz = new Quiz(admin);
        QuizPalavra item = quiz.adicionarPalavra(palavra(10L, "Azul"));
        item.responder(false);
        when(quizPalavraRepository.findComPalavraById(7L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> quizService.responder(7L, new RespostaRequest(true)))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Esta palavra do quiz já foi respondida.");
        assertThat(item.getAcertou()).isFalse();
    }

    @Test
    void responderItemInexistenteLancaNaoEncontrado() {
        when(quizPalavraRepository.findComPalavraById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.responder(7L, new RespostaRequest(true)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Item do quiz não encontrado.");
    }

    @Test
    void buscarInexistenteLancaNaoEncontrado() {
        when(quizRepository.findDetalhadoById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.buscar(1L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Quiz não encontrado.");
    }
}
