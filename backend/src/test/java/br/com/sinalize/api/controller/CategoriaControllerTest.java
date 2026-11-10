package br.com.sinalize.api.controller;

import br.com.sinalize.api.dto.categoria.CategoriaAtualizacaoRequest;
import br.com.sinalize.api.dto.categoria.CategoriaRequest;
import br.com.sinalize.api.dto.categoria.CategoriaResponse;
import br.com.sinalize.api.dto.usuario.UsuarioResumoResponse;
import br.com.sinalize.api.exception.RecursoNaoEncontradoException;
import br.com.sinalize.api.service.CategoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    private static CategoriaResponse categoria(long id, String nome) {
        return new CategoriaResponse(id, nome, "Descrição de " + nome,
                LocalDateTime.of(2026, 11, 10, 20, 0), null,
                new UsuarioResumoResponse(1L, "Administrador"), null);
    }

    @Test
    void listarDevolveCategoriasEmSnakeCase() throws Exception {
        when(categoriaService.listar()).thenReturn(List.of(categoria(1, "Cores"), categoria(2, "Animais")));

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("Cores"))
                .andExpect(jsonPath("$[0].criado_em").value("2026-11-10T20:00:00"))
                .andExpect(jsonPath("$[0].criado_por.nome").value("Administrador"))
                .andExpect(jsonPath("$[0].atualizado_por").value(nullValue()));
    }

    @Test
    void buscarInexistenteDevolve404NoFormatoPadrao() throws Exception {
        when(categoriaService.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Categoria não encontrada."));

        mockMvc.perform(get("/categorias/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Categoria não encontrada."))
                .andExpect(jsonPath("$.path").value("/categorias/99"))
                .andExpect(jsonPath("$.timestamp").value(notNullValue()))
                .andExpect(jsonPath("$.campos").doesNotExist());
    }

    @Test
    void buscarComIdNaoNumericoDevolve400() throws Exception {
        mockMvc.perform(get("/categorias/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O parâmetro id possui um valor inválido: abc."));
    }

    @Test
    void criarDevolve201ComLocation() throws Exception {
        when(categoriaService.criar(any(CategoriaRequest.class))).thenReturn(categoria(7, "Esportes"));

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Esportes", "descricao": "Modalidades esportivas."}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/categorias/7"))
                .andExpect(jsonPath("$.id").value(7));

        verify(categoriaService).criar(new CategoriaRequest("Esportes", "Modalidades esportivas."));
    }

    @Test
    void criarSemCamposObrigatoriosDevolve400ComErrosPorCampo() throws Exception {
        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dados inválidos."))
                .andExpect(jsonPath("$.campos", hasSize(2)))
                .andExpect(jsonPath("$.campos[0].campo").value("descricao"))
                .andExpect(jsonPath("$.campos[0].mensagem").value("O campo descricao é obrigatório."))
                .andExpect(jsonPath("$.campos[1].campo").value("nome"))
                .andExpect(jsonPath("$.campos[1].mensagem").value("O campo nome é obrigatório."));

        verifyNoInteractions(categoriaService);
    }

    @Test
    void criarComCampoDesconhecidoDevolve400() throws Exception {
        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "X", "descricao": "Y", "cor": "azul"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O campo cor não é permitido."));
    }

    @Test
    void criarComJsonMalformadoDevolve400() throws Exception {
        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O corpo da requisição é inválido ou está mal formatado."));
    }

    @Test
    void atualizarParcialmenteDevolve200() throws Exception {
        when(categoriaService.atualizar(eq(1L), any())).thenReturn(categoria(1, "Cores e tons"));

        mockMvc.perform(patch("/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Cores e tons"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Cores e tons"));

        verify(categoriaService).atualizar(1L, new CategoriaAtualizacaoRequest("Cores e tons", null));
    }

    @Test
    void atualizarComNomeEmBrancoDevolve400() throws Exception {
        mockMvc.perform(patch("/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("nome"))
                .andExpect(jsonPath("$.campos[0].mensagem").value("O campo nome não pode estar em branco."));
    }

    @Test
    void excluirDevolve204() throws Exception {
        mockMvc.perform(delete("/categorias/3"))
                .andExpect(status().isNoContent());

        verify(categoriaService).excluir(3L);
    }

    @Test
    void excluirInexistenteDevolve404() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Categoria não encontrada."))
                .when(categoriaService).excluir(99L);

        mockMvc.perform(delete("/categorias/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void metodoNaoSuportadoDevolve405() throws Exception {
        mockMvc.perform(put("/categorias/1"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").value("O método PUT não é suportado nesta rota."));
    }
}
