package br.com.sinalize.api.controller;

import br.com.sinalize.api.dto.categoria.CategoriaResumoResponse;
import br.com.sinalize.api.dto.palavra.PalavraAtualizacaoRequest;
import br.com.sinalize.api.dto.palavra.PalavraRequest;
import br.com.sinalize.api.dto.palavra.PalavraResponse;
import br.com.sinalize.api.dto.palavra.PalavraResumoResponse;
import br.com.sinalize.api.dto.usuario.UsuarioResumoResponse;
import br.com.sinalize.api.exception.ConflitoException;
import br.com.sinalize.api.exception.RecursoNaoEncontradoException;
import br.com.sinalize.api.service.PalavraService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PalavraController.class)
class PalavraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PalavraService palavraService;

    private static PalavraResponse palavra(long id, String texto) {
        return new PalavraResponse(id, texto, LocalDateTime.of(2026, 11, 10, 20, 0), null,
                new UsuarioResumoResponse(1L, "Administrador"), null,
                List.of(new CategoriaResumoResponse(3L, "Cores")));
    }

    @Test
    void listarDevolvePalavrasComCategorias() throws Exception {
        when(palavraService.listar()).thenReturn(List.of(palavra(1, "Azul")));

        mockMvc.perform(get("/palavras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].palavra").value("Azul"))
                .andExpect(jsonPath("$[0].categorias[0].nome").value("Cores"))
                .andExpect(jsonPath("$[0].criado_por.id").value(1));
    }

    @Test
    void aleatoriasUsaValoresPadrao() throws Exception {
        when(palavraService.sortearAleatorias(List.of(), 3))
                .thenReturn(List.of(new PalavraResumoResponse(5L, "Gato")));

        mockMvc.perform(get("/palavras/aleatorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].palavra").value("Gato"));
    }

    @Test
    void aleatoriasAceitaListaDeIdsSeparadosPorVirgula() throws Exception {
        mockMvc.perform(get("/palavras/aleatorias").param("excluir", "3,8").param("quantidade", "2"))
                .andExpect(status().isOk());

        verify(palavraService).sortearAleatorias(List.of(3L, 8L), 2);
    }

    @Test
    void aleatoriasComQuantidadeForaDoLimiteDevolve400() throws Exception {
        mockMvc.perform(get("/palavras/aleatorias").param("quantidade", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("quantidade"))
                .andExpect(jsonPath("$.campos[0].mensagem").value("O parâmetro quantidade deve ser no mínimo 1."));

        verifyNoInteractions(palavraService);
    }

    @Test
    void aleatoriasComIdInvalidoDevolve400() throws Exception {
        mockMvc.perform(get("/palavras/aleatorias").param("excluir", "3,abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O parâmetro excluir possui um valor inválido: 3,abc."));
    }

    @Test
    void criarDevolve201ComLocation() throws Exception {
        when(palavraService.criar(any())).thenReturn(palavra(50, "Lilás"));

        mockMvc.perform(post("/palavras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"palavra": "Lilás", "categoria_ids": [3]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/palavras/50"))
                .andExpect(jsonPath("$.palavra").value("Lilás"));

        verify(palavraService).criar(new PalavraRequest("Lilás", List.of(3L)));
    }

    @Test
    void criarSemCategoriaDevolve400() throws Exception {
        mockMvc.perform(post("/palavras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"palavra": "Lilás", "categoria_ids": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("categoria_ids"))
                .andExpect(jsonPath("$.campos[0].mensagem")
                        .value("A palavra deve estar associada a pelo menos uma categoria."));

        verifyNoInteractions(palavraService);
    }

    @Test
    void criarComCategoriaNaoNumericaDevolve400() throws Exception {
        mockMvc.perform(post("/palavras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"palavra": "Lilás", "categoria_ids": ["x"]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O campo categoria_ids possui um formato inválido."));
    }

    @Test
    void criarComCategoriaInexistenteDevolve404() throws Exception {
        when(palavraService.criar(any()))
                .thenThrow(new RecursoNaoEncontradoException("Categoria com id 99 não encontrada."));

        mockMvc.perform(post("/palavras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"palavra": "Lilás", "categoria_ids": [99]}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Categoria com id 99 não encontrada."));
    }

    @Test
    void atualizarSomenteCategorias() throws Exception {
        when(palavraService.atualizar(any(), any())).thenReturn(palavra(1, "Azul"));

        mockMvc.perform(patch("/palavras/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoria_ids": [3, 5]}
                                """))
                .andExpect(status().isOk());

        verify(palavraService).atualizar(1L, new PalavraAtualizacaoRequest(null, List.of(3L, 5L)));
    }

    @Test
    void atualizarComListaDeCategoriasVaziaDevolve400() throws Exception {
        mockMvc.perform(patch("/palavras/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoria_ids": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("categoria_ids"));
    }

    @Test
    void excluirDevolve204() throws Exception {
        mockMvc.perform(delete("/palavras/4"))
                .andExpect(status().isNoContent());

        verify(palavraService).excluir(4L);
    }

    @Test
    void excluirPalavraUsadaEmQuizDevolve409() throws Exception {
        doThrow(new ConflitoException("A palavra não pode ser excluída porque já foi usada em um quiz."))
                .when(palavraService).excluir(4L);

        mockMvc.perform(delete("/palavras/4"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("A palavra não pode ser excluída porque já foi usada em um quiz."));
    }

    @Test
    void naoChamaServiceQuandoCorpoInvalido() throws Exception {
        mockMvc.perform(post("/palavras").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos", hasSize(2)));

        verifyNoInteractions(palavraService);
    }
}
