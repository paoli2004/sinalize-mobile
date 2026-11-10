package br.com.sinalize.api.controller;

import br.com.sinalize.api.dto.palavra.PalavraResumoResponse;
import br.com.sinalize.api.dto.quiz.QuizPalavraResponse;
import br.com.sinalize.api.dto.quiz.QuizRequest;
import br.com.sinalize.api.dto.quiz.QuizResponse;
import br.com.sinalize.api.dto.quiz.RespostaRequest;
import br.com.sinalize.api.dto.usuario.UsuarioResumoResponse;
import br.com.sinalize.api.exception.ConflitoException;
import br.com.sinalize.api.exception.RecursoNaoEncontradoException;
import br.com.sinalize.api.exception.RegraNegocioException;
import br.com.sinalize.api.service.QuizService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuizController.class)
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizService quizService;

    private static QuizResponse quiz() {
        return new QuizResponse(12L, LocalDateTime.of(2026, 11, 10, 21, 0),
                new UsuarioResumoResponse(1L, "Administrador"),
                List.of(new QuizPalavraResponse(30L, null, null, new PalavraResumoResponse(10L, "Azul")),
                        new QuizPalavraResponse(31L, null, null, new PalavraResumoResponse(20L, "Verde"))));
    }

    @Test
    void criarDevolve201ComItensEmSnakeCase() throws Exception {
        when(quizService.criar(any())).thenReturn(quiz());

        mockMvc.perform(post("/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoria_ids": [3, 4]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/quizzes/12"))
                .andExpect(jsonPath("$.quiz_palavras", hasSize(2)))
                .andExpect(jsonPath("$.quiz_palavras[0].id").value(30))
                .andExpect(jsonPath("$.quiz_palavras[0].acertou").value(nullValue()))
                .andExpect(jsonPath("$.quiz_palavras[0].respondido_em").value(nullValue()))
                .andExpect(jsonPath("$.quiz_palavras[0].palavra.palavra").value("Azul"));

        verify(quizService).criar(new QuizRequest(List.of(3L, 4L)));
    }

    @Test
    void criarSemCategoriasDevolve400() throws Exception {
        mockMvc.perform(post("/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoria_ids": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("categoria_ids"))
                .andExpect(jsonPath("$.campos[0].mensagem").value("Informe pelo menos uma categoria."));

        verifyNoInteractions(quizService);
    }

    @Test
    void criarComCategoriasSemPalavrasDevolve422() throws Exception {
        when(quizService.criar(any()))
                .thenThrow(new RegraNegocioException("Não há palavras cadastradas nas categorias selecionadas."));

        mockMvc.perform(post("/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoria_ids": [7]}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("Não há palavras cadastradas nas categorias selecionadas."));
    }

    @Test
    void buscarDevolveQuiz() throws Exception {
        when(quizService.buscar(12L)).thenReturn(quiz());

        mockMvc.perform(get("/quizzes/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.usuario.nome").value("Administrador"));
    }

    @Test
    void buscarInexistenteDevolve404() throws Exception {
        when(quizService.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Quiz não encontrado."));

        mockMvc.perform(get("/quizzes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Quiz não encontrado."));
    }

    @Test
    void listarDevolveQuizzes() throws Exception {
        when(quizService.listar()).thenReturn(List.of(quiz()));

        mockMvc.perform(get("/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void responderDevolveItemAtualizado() throws Exception {
        when(quizService.responder(eq(30L), any())).thenReturn(new QuizPalavraResponse(30L, true,
                LocalDateTime.of(2026, 11, 10, 21, 1), new PalavraResumoResponse(10L, "Azul")));

        mockMvc.perform(patch("/quizzes/palavras/30/responder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"acertou": true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acertou").value(true))
                .andExpect(jsonPath("$.respondido_em").value("2026-11-10T21:01:00"));

        verify(quizService).responder(30L, new RespostaRequest(true));
    }

    @Test
    void responderSemAcertouDevolve400() throws Exception {
        mockMvc.perform(patch("/quizzes/palavras/30/responder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].mensagem").value("O campo acertou é obrigatório."));

        verifyNoInteractions(quizService);
    }

    @Test
    void responderComValorNaoBooleanoDevolve400() throws Exception {
        mockMvc.perform(patch("/quizzes/palavras/30/responder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"acertou": "talvez"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O campo acertou possui um formato inválido."));
    }

    @Test
    void responderDuasVezesDevolve409() throws Exception {
        when(quizService.responder(eq(30L), any()))
                .thenThrow(new ConflitoException("Esta palavra do quiz já foi respondida."));

        mockMvc.perform(patch("/quizzes/palavras/30/responder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"acertou": false}
                                """))
                .andExpect(status().isConflict());
    }
}
