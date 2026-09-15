package com.example.multidb;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MultiDbApiTest {
    @Autowired MockMvc mvc;
    @Autowired Map<String, JdbcTemplate> templates;

    record Resource(String name, List<String> columns, String request) {
        String path() { return "/api/" + name; }
    }

    static Stream<Resource> resources() {
        return Stream.of(
                new Resource("products", List.of("id", "name", "price", "stock"),
                        "{\"name\":\"Mouse\",\"price\":12900.50,\"stock\":5}"),
                new Resource("customers", List.of("id", "name", "email"),
                        "{\"name\":\"Kim\",\"email\":\"kim@example.com\"}"),
                new Resource("orders", List.of("id", "itemName", "quantity"),
                        "{\"itemName\":\"Mouse\",\"quantity\":3}"));
    }

    @BeforeEach
    void resetDatabases() {
        resources().forEach(resource -> {
            JdbcTemplate jdbc = templates.get(resource.name() + "JdbcTemplate");
            jdbc.execute("DROP TABLE " + resource.name());
            new ResourceDatabasePopulator(new ClassPathResource("db/" + resource.name() + ".sql"))
                    .execute(jdbc.getDataSource());
        });
    }

    @ParameterizedTest
    @MethodSource("resources")
    void createThenListAndLeaveOtherDatabasesUnchanged(Resource resource) throws Exception {
        List<Resource> others = resources().filter(r -> !r.name().equals(resource.name())).toList();
        String firstBefore = getBody(others.get(0).path());
        String secondBefore = getBody(others.get(1).path());
        mvc.perform(get(resource.path()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns").value(org.hamcrest.Matchers.equalTo(resource.columns())))
                .andExpect(jsonPath("$.rows", hasSize(1)));

        String created = mvc.perform(post(resource.path()).contentType(MediaType.APPLICATION_JSON)
                        .content(resource.request()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.columns").value(org.hamcrest.Matchers.equalTo(resource.columns())))
                .andExpect(jsonPath("$.rows", hasSize(1)))
                .andReturn().getResponse().getContentAsString();
        Map<String, Object> row = JsonPath.read(created, "$.rows[0]");
        Map<String, Object> input = JsonPath.read(resource.request(), "$");
        assertThat(row).containsAllEntriesOf(input);
        assertThat(((Number) row.get("id")).longValue()).isGreaterThan(1);
        assertThat(row.keySet()).containsExactlyInAnyOrderElementsOf(resource.columns());

        List<Map<String, Object>> rows = JsonPath.read(getBody(resource.path()), "$.rows");
        assertThat(rows).hasSize(2).contains(row);
        assertThat(getBody(others.get(0).path())).isEqualTo(firstBefore);
        assertThat(getBody(others.get(1).path())).isEqualTo(secondBefore);
    }

    @ParameterizedTest
    @MethodSource("resources")
    void emptyTableKeepsColumns(Resource resource) throws Exception {
        templates.get(resource.name() + "JdbcTemplate").update("DELETE FROM " + resource.name());
        mvc.perform(get(resource.path()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns").value(org.hamcrest.Matchers.equalTo(resource.columns())))
                .andExpect(jsonPath("$.rows").isEmpty());
    }

    @Test
    void eachConnectionHasItsOwnDatabaseAndTable() {
        resources().forEach(resource -> {
            JdbcTemplate jdbc = templates.get(resource.name() + "JdbcTemplate");
            assertThat(jdbc.queryForObject("SELECT DATABASE()", String.class))
                    .isEqualToIgnoringCase(resource.name() + "_db");
            assertThat(jdbc.queryForList(
                    "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
                    String.class)).containsExactly(resource.name().toUpperCase(java.util.Locale.ROOT));
        });
    }

    static Stream<Arguments> invalidRequests() {
        Stream<Arguments> common = resources().flatMap(resource -> Stream.of(
                Arguments.of(resource.path(), "{}"),
                Arguments.of(resource.path(), "null"),
                Arguments.of(resource.path(), "{")));
        Stream<Arguments> fields = Stream.of(
                Arguments.of("/api/products", "{\"name\":\" \",\"price\":1,\"stock\":1}"),
                Arguments.of("/api/products", "{\"name\":\"a\",\"price\":-1,\"stock\":1}"),
                Arguments.of("/api/products", "{\"name\":\"a\",\"price\":1,\"stock\":-1}"),
                Arguments.of("/api/products", "{\"name\":\"a\",\"price\":1.001,\"stock\":1}"),
                Arguments.of("/api/products", "{\"name\":\"a\",\"price\":10000000000,\"stock\":1}"),
                Arguments.of("/api/products", "{\"name\":\"a\",\"price\":1,\"stock\":2147483648}"),
                Arguments.of("/api/products", "{\"name\":\"a\",\"price\":\"invalid\",\"stock\":1}"),
                Arguments.of("/api/customers", "{\"name\":\" \",\"email\":\"a@example.com\"}"),
                Arguments.of("/api/customers", "{\"name\":\"a\",\"email\":\"invalid\"}"),
                Arguments.of("/api/customers", "{\"name\":\"a\",\"email\":\"\"}"),
                Arguments.of("/api/customers", "{\"name\":\"" + "a".repeat(101) + "\",\"email\":\"a@example.com\"}"),
                Arguments.of("/api/orders", "{\"itemName\":\" \",\"quantity\":1}"),
                Arguments.of("/api/orders", "{\"itemName\":\"a\",\"quantity\":0}"),
                Arguments.of("/api/orders", "{\"itemName\":\"a\",\"quantity\":1.5}"),
                Arguments.of("/api/products", "{\"name\":\"a\",\"price\":1,\"stock\":0.5}"),
                Arguments.of("/api/orders", "{\"itemName\":\"a\",\"quantity\":-1}"),
                Arguments.of("/api/orders", "{\"itemName\":\"a\",\"quantity\":null}"),
                Arguments.of("/api/orders", "{\"itemName\":\"a\",\"quantity\":2147483648}"));
        return Stream.concat(common, fields);
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void invalidInputReturns400WithoutChangingAnyDatabase(String path, String request) throws Exception {
        List<String> paths = resources().map(Resource::path).toList();
        String products = getBody(paths.get(0));
        String customers = getBody(paths.get(1));
        String orders = getBody(paths.get(2));
        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        assertThat(getBody(paths.get(0))).isEqualTo(products);
        assertThat(getBody(paths.get(1))).isEqualTo(customers);
        assertThat(getBody(paths.get(2))).isEqualTo(orders);
    }

    @Test
    void acceptsNumericLowerBoundsAndBindsSqlAsData() throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"O'Reilly'); DROP TABLE products; --\",\"price\":0,\"stock\":0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rows[0].price").value(0))
                .andExpect(jsonPath("$.rows[0].stock").value(0))
                .andExpect(jsonPath("$.rows[0].name").value("O'Reilly'); DROP TABLE products; --"));
        mvc.perform(get("/api/products")).andExpect(jsonPath("$.rows", hasSize(2)));
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemName\":\"a\",\"quantity\":1}"))
                .andExpect(status().isCreated());
    }

    @Test
    void swaggerUiAndAllSixOperationsAreAvailable() throws Exception {
        mvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Swagger UI")));
        mvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/v3/api-docs"));
        String document = getBody("/v3/api-docs");
        Map<String, Object> paths = JsonPath.read(document, "$.paths");
        assertThat(paths.keySet()).containsExactlyInAnyOrder("/api/products", "/api/customers", "/api/orders");
        for (Resource resource : resources().toList()) {
            Map<String, Object> operations = JsonPath.read(document, "$.paths['" + resource.path() + "']");
            assertThat(operations.keySet()).containsExactlyInAnyOrder("get", "post");
            assertThat((Object) JsonPath.read(document,
                    "$.paths['" + resource.path() + "'].get.responses['200']")).isNotNull();
            assertThat((Object) JsonPath.read(document,
                    "$.paths['" + resource.path() + "'].post.responses['201']")).isNotNull();
        }
    }

    private String getBody(String path) throws Exception {
        return mvc.perform(get(path)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }
}
