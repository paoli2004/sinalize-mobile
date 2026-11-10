package br.com.sinalize.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração: aplicação completa (controllers, services, JPA, Flyway) contra o
 * PostgreSQL real do docker-compose, no schema isolado "it".
 *
 * <p>Pré-requisito: {@code docker compose up -d}. Execução: {@code mvnw verify}.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
class SinalizeApiIT {

    /** Recria o schema de teste a cada execução, partindo sempre das migrations e da seed. */
    @TestConfiguration
    static class FlywayLimpo {
        @Bean
        FlywayMigrationStrategy limparEMigrar() {
            return flyway -> {
                flyway.clean();
                flyway.migrate();
            };
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void migrationsCriamOsDadosIniciais() throws Exception {
        assertThat(jdbc.queryForObject("SELECT current_schema()", String.class)).isEqualTo("it");

        JsonNode categorias = json(get("/categorias"));
        assertThat(categorias).extracting(c -> c.get("nome").asText())
                .contains("Saudações", "Família", "Cores", "Animais", "Alimentos", "Lugares");

        Integer saudacoes = jdbc.queryForObject("""
                SELECT count(*) FROM palavras_categorias pc
                JOIN categorias c ON c.id = pc.categoria_id WHERE c.nome = 'Saudações'
                """, Integer.class);
        assertThat(saudacoes).isEqualTo(7);
    }

    @Test
    void partidaCompletaDeQuiz() throws Exception {
        long cores = idDaCategoria("Cores");
        long alimentos = idDaCategoria("Alimentos");

        // 1. cria o quiz
        JsonNode quiz = json(post("/quizzes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoria_ids\": [" + cores + ", " + alimentos + "]}"), 201);
        JsonNode itens = quiz.get("quiz_palavras");
        assertThat(itens).hasSize(5);

        Set<Long> palavrasDoQuiz = new HashSet<>();
        itens.forEach(item -> palavrasDoQuiz.add(item.get("palavra").get("id").asLong()));
        assertThat(palavrasDoQuiz).as("palavras distintas").hasSize(5);

        // 2. para cada item: busca alternativas erradas e responde
        List<Boolean> respostas = new ArrayList<>();
        for (JsonNode item : itens) {
            long palavraId = item.get("palavra").get("id").asLong();
            JsonNode alternativas = json(get("/palavras/aleatorias")
                    .param("excluir", String.valueOf(palavraId)).param("quantidade", "3"));
            assertThat(alternativas).hasSize(3)
                    .allSatisfy(alt -> assertThat(alt.get("id").asLong()).isNotEqualTo(palavraId));

            boolean acertou = respostas.size() % 2 == 0;
            respostas.add(acertou);
            mockMvc.perform(patch("/quizzes/palavras/" + item.get("id").asLong() + "/responder")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"acertou\": " + acertou + "}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.acertou").value(acertou));
        }

        // 3. o resultado fica gravado no banco
        long quizId = quiz.get("id").asLong();
        Integer acertos = jdbc.queryForObject(
                "SELECT count(*) FROM quiz_palavras WHERE id_quiz = ? AND acertou", Integer.class, quizId);
        Integer semData = jdbc.queryForObject(
                "SELECT count(*) FROM quiz_palavras WHERE id_quiz = ? AND respondido_em IS NULL", Integer.class, quizId);
        assertThat(acertos).isEqualTo(3);
        assertThat(semData).isZero();

        // 4. responder de novo e excluir palavra usada são bloqueados
        mockMvc.perform(patch("/quizzes/palavras/" + itens.get(0).get("id").asLong() + "/responder")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"acertou\": true}"))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/palavras/" + palavrasDoQuiz.iterator().next()))
                .andExpect(status().isConflict());

        // 5. o quiz pode ser consultado depois
        mockMvc.perform(get("/quizzes/" + quizId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quiz_palavras[0].respondido_em").isNotEmpty());
    }

    @Test
    void cicloDeVidaDeUmaPalavra() throws Exception {
        long cores = idDaCategoria("Cores");
        long animais = idDaCategoria("Animais");

        JsonNode criada = json(post("/palavras").contentType(MediaType.APPLICATION_JSON)
                .content("{\"palavra\": \"Lilás\", \"categoria_ids\": [" + cores + "]}"), 201);
        long id = criada.get("id").asLong();

        mockMvc.perform(patch("/palavras/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoria_ids\": [" + cores + ", " + animais + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias.length()").value(2))
                .andExpect(jsonPath("$.atualizado_por.id").value(1));

        mockMvc.perform(delete("/palavras/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/palavras/" + id)).andExpect(status().isNotFound());

        Integer vinculos = jdbc.queryForObject(
                "SELECT count(*) FROM palavras_categorias WHERE palavra_id = ?", Integer.class, id);
        assertThat(vinculos).isZero();
    }

    @Test
    void usuarioTemSenhaGuardadaComoHashENuncaDevolvida() throws Exception {
        JsonNode criado = json(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome": "Maria", "email": "Maria.IT@Email.com", "senha": "Senha123"}
                        """), 201);
        long id = criado.get("id").asLong();
        assertThat(criado.has("senha")).isFalse();
        assertThat(criado.get("email").asText()).isEqualTo("maria.it@email.com");

        String hash = jdbc.queryForObject("SELECT senha FROM usuarios WHERE id = ?", String.class, id);
        assertThat(hash).startsWith("$2a$").isNotEqualTo("Senha123");

        // e-mail duplicado, mesmo com maiúsculas diferentes
        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Outra", "email": "MARIA.IT@email.com", "senha": "Senha123"}
                                """))
                .andExpect(status().isConflict());

        // o usuário padrão tem registros associados e não pode ser excluído
        mockMvc.perform(delete("/usuarios/1")).andExpect(status().isConflict());

        // um usuário sem registros pode
        mockMvc.perform(delete("/usuarios/" + id)).andExpect(status().isNoContent());
    }

    private long idDaCategoria(String nome) {
        Long id = jdbc.queryForObject("SELECT id FROM categorias WHERE nome = ?", Long.class, nome);
        assertThat(id).isNotNull();
        return id;
    }

    private JsonNode json(MockHttpServletRequestBuilder requisicao) throws Exception {
        return json(requisicao, 200);
    }

    private JsonNode json(MockHttpServletRequestBuilder requisicao, int statusEsperado) throws Exception {
        ResultActions resultado = mockMvc.perform(requisicao).andExpect(status().is(statusEsperado));
        return objectMapper.readTree(resultado.andReturn().getResponse().getContentAsByteArray());
    }
}
