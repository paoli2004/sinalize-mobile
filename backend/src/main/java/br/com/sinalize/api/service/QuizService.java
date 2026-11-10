package br.com.sinalize.api.service;

import br.com.sinalize.api.dto.quiz.QuizPalavraResponse;
import br.com.sinalize.api.dto.quiz.QuizRequest;
import br.com.sinalize.api.dto.quiz.QuizResponse;
import br.com.sinalize.api.dto.quiz.RespostaRequest;
import br.com.sinalize.api.entity.Categoria;
import br.com.sinalize.api.entity.Palavra;
import br.com.sinalize.api.entity.Quiz;
import br.com.sinalize.api.entity.QuizPalavra;
import br.com.sinalize.api.exception.ConflitoException;
import br.com.sinalize.api.exception.RecursoNaoEncontradoException;
import br.com.sinalize.api.exception.RegraNegocioException;
import br.com.sinalize.api.repository.PalavraRepository;
import br.com.sinalize.api.repository.QuizPalavraRepository;
import br.com.sinalize.api.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class QuizService {

    static final String QUIZ_NAO_ENCONTRADO = "Quiz não encontrado.";
    static final String ITEM_NAO_ENCONTRADO = "Item do quiz não encontrado.";
    static final String ITEM_JA_RESPONDIDO = "Esta palavra do quiz já foi respondida.";
    static final String SEM_PALAVRAS = "Não há palavras cadastradas nas categorias selecionadas.";

    private final QuizRepository quizRepository;
    private final QuizPalavraRepository quizPalavraRepository;
    private final PalavraRepository palavraRepository;
    private final CategoriaService categoriaService;
    private final UsuarioLogadoProvider usuarioLogado;
    private final int quantidadePalavras;

    public QuizService(QuizRepository quizRepository,
                       QuizPalavraRepository quizPalavraRepository,
                       PalavraRepository palavraRepository,
                       CategoriaService categoriaService,
                       UsuarioLogadoProvider usuarioLogado,
                       @Value("${sinalize.quiz.quantidade-palavras:5}") int quantidadePalavras) {
        this.quizRepository = quizRepository;
        this.quizPalavraRepository = quizPalavraRepository;
        this.palavraRepository = palavraRepository;
        this.categoriaService = categoriaService;
        this.usuarioLogado = usuarioLogado;
        this.quantidadePalavras = quantidadePalavras;
    }

    public List<QuizResponse> listar() {
        return quizRepository.findAllByOrderByCriadoEmAscIdAsc().stream()
                .map(QuizResponse::from)
                .toList();
    }

    public QuizResponse buscar(Long id) {
        return quizRepository.findDetalhadoById(id)
                .map(QuizResponse::from)
                .orElseThrow(() -> new RecursoNaoEncontradoException(QUIZ_NAO_ENCONTRADO));
    }

    /**
     * Cria um quiz sorteando até {@code sinalize.quiz.quantidade-palavras} palavras distintas
     * das categorias informadas.
     *
     * @throws RecursoNaoEncontradoException se alguma categoria não existir
     * @throws RegraNegocioException         se as categorias não tiverem nenhuma palavra
     */
    @Transactional
    public QuizResponse criar(QuizRequest request) {
        List<Long> categoriaIds = categoriaService.buscarEntidadesPorIds(request.categoriaIds()).stream()
                .map(Categoria::getId)
                .toList();

        List<Palavra> sorteadas = palavraRepository.sortearPorCategorias(categoriaIds, quantidadePalavras);
        if (sorteadas.isEmpty()) {
            throw new RegraNegocioException(SEM_PALAVRAS);
        }

        Quiz quiz = new Quiz(usuarioLogado.obter());
        sorteadas.forEach(quiz::adicionarPalavra);

        return QuizResponse.from(quizRepository.save(quiz));
    }

    /**
     * Registra se o jogador acertou a palavra. Cada palavra só pode ser respondida uma vez.
     *
     * @throws RecursoNaoEncontradoException se o item não existir
     * @throws ConflitoException             se o item já tiver sido respondido
     */
    @Transactional
    public QuizPalavraResponse responder(Long quizPalavraId, RespostaRequest request) {
        QuizPalavra item = quizPalavraRepository.findComPalavraById(quizPalavraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(ITEM_NAO_ENCONTRADO));

        if (item.getAcertou() != null) {
            throw new ConflitoException(ITEM_JA_RESPONDIDO);
        }
        item.responder(request.acertou());

        return QuizPalavraResponse.from(item);
    }
}
