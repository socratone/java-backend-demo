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

/** 실제 Spring 빈과 H2 DB를 사용해 API 응답·검증·DB 분리·문서를 함께 확인합니다. */
@SpringBootTest
@AutoConfigureMockMvc
class MultiDbApiTest {
    // TCP 서버를 띄우지 않고 Spring MVC 요청 처리 과정을 실행합니다.
    @Autowired MockMvc mvc;
    // 빈 이름을 키로 받아 테스트 대상 DB의 JdbcTemplate을 선택합니다.
    @Autowired Map<String, JdbcTemplate> templates;

    /** API별 경로 구성 요소, 예상 컬럼, 정상 생성 요청을 묶은 테스트 데이터입니다. */
    record Resource(String name, List<String> columns, String request) {
        // 리소스 이름으로 실제 API 경로를 만듭니다.
        String path() { return "/api/" + name; }
    }

    /** 같은 검증을 상품·고객·주문 API에 각각 적용할 입력을 제공합니다. */
    static Stream<Resource> resources() {
        return Stream.of(
                new Resource("products", List.of("id", "name", "price", "stock"),
                        "{\"name\":\"Mouse\",\"price\":12900.50,\"stock\":5}"),
                new Resource("customers", List.of("id", "name", "email"),
                        "{\"name\":\"Kim\",\"email\":\"kim@example.com\"}"),
                new Resource("orders", List.of("id", "itemName", "quantity"),
                        "{\"itemName\":\"Mouse\",\"quantity\":3}"));
    }

    // 매 테스트 전에 테이블과 자동 ID를 초기화하여 실행 순서에 영향을 받지 않게 합니다.
    @BeforeEach
    void resetDatabases() {
        resources().forEach(resource -> {
            JdbcTemplate jdbc = templates.get(resource.name() + "JdbcTemplate");
            // 테이블명은 외부 입력이 아닌 resources()의 고정된 테스트 값입니다.
            jdbc.execute("DROP TABLE " + resource.name());
            new ResourceDatabasePopulator(new ClassPathResource("db/" + resource.name() + ".sql"))
                    .execute(jdbc.getDataSource());
        });
    }

    /** 생성 결과가 목록에 포함되고 다른 두 DB의 응답은 그대로인지 확인합니다. */
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

    /** 데이터를 모두 삭제해도 컬럼 목록은 유지되는지 확인합니다. */
    @ParameterizedTest
    @MethodSource("resources")
    void emptyTableKeepsColumns(Resource resource) throws Exception {
        templates.get(resource.name() + "JdbcTemplate").update("DELETE FROM " + resource.name());
        mvc.perform(get(resource.path()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.columns").value(org.hamcrest.Matchers.equalTo(resource.columns())))
                .andExpect(jsonPath("$.rows").isEmpty());
    }

    /** 연결된 DB 이름과 실제 테이블 목록으로 물리적인 DB 분리를 확인합니다. */
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

    /** 필수값 누락, 잘못된 JSON, 필드별 형식·범위 오류 사례를 제공합니다. */
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

    /** 잘못된 요청이 400으로 거부되고 어느 DB에도 변경이 생기지 않는지 확인합니다. */
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

    /** 숫자 하한값을 허용하고 SQL처럼 보이는 문자열도 데이터로 저장하는지 확인합니다. */
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

    /** Swagger 페이지·설정과 세 경로의 GET/POST 및 성공 응답 문서를 확인합니다. */
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

    /** GET의 성공 상태를 확인하고 후속 비교에 사용할 JSON 본문을 반환합니다. */
    private String getBody(String path) throws Exception {
        return mvc.perform(get(path)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }
}
