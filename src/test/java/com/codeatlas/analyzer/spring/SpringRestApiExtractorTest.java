package com.codeatlas.analyzer.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SpringRestApiExtractorTest {

    private final SpringRestApiExtractor extractor = new SpringRestApiExtractor();

    @Test
    void extractsRestApisFromSampleOrderController() {
        Path sourceFilePath = Path.of(
                "samples",
                "legacy-spring-mybatis",
                "src",
                "main",
                "java",
                "com",
                "example",
                "legacy",
                "order",
                "controller",
                "OrderController.java"
        );

        List<ExtractedSpringApi> apis = extractor.extract(sourceFilePath);

        assertThat(apis)
                .extracting(ExtractedSpringApi::httpMethod, ExtractedSpringApi::path, ExtractedSpringApi::methodName)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("POST", "/api/orders", "createOrder"),
                        org.assertj.core.groups.Tuple.tuple("GET", "/api/orders/{orderId}", "getOrder"),
                        org.assertj.core.groups.Tuple.tuple("GET", "/api/orders", "searchOrders"),
                        org.assertj.core.groups.Tuple.tuple("PUT", "/api/orders/{orderId}/status", "updateOrderStatus"),
                        org.assertj.core.groups.Tuple.tuple("DELETE", "/api/orders/{orderId}", "cancelOrder")
                );

        assertThat(apis)
                .allSatisfy(api -> {
                    assertThat(api.controllerClassName()).isEqualTo("OrderController");
                    assertThat(api.sourceFilePath()).endsWith("OrderController.java");
                    assertThat(api.lineNumber()).isPositive();
                });
        assertThat(apis)
                .filteredOn(api -> api.methodName().equals("createOrder"))
                .singleElement()
                .satisfies(api -> {
                    assertThat(api.requestDtoName()).isEqualTo("CreateOrderRequest");
                    assertThat(api.responseDtoName()).isEqualTo("OrderResponse");
                });
        assertThat(apis)
                .filteredOn(api -> api.methodName().equals("searchOrders"))
                .singleElement()
                .satisfies(api -> assertThat(api.responseDtoName()).isEqualTo("OrderResponse"));
    }

    @Test
    void extractsPathsFromPathAttributesAndArrayValues(@TempDir Path tempDir) throws IOException {
        Path sourceFile = tempDir.resolve("AdminOrderController.java");
        Files.writeString(sourceFile, """
                package com.example;

                import org.springframework.web.bind.annotation.PatchMapping;
                import org.springframework.web.bind.annotation.RequestMapping;
                import org.springframework.web.bind.annotation.RequestMethod;
                import org.springframework.web.bind.annotation.RestController;

                @RestController
                @RequestMapping(path = {"/admin/orders"})
                class AdminOrderController {

                    @PatchMapping(path = {"/{orderId}/status"})
                    void patchStatus() {
                    }

                    @RequestMapping(path = "/legacy-sync", method = RequestMethod.POST)
                    void legacySync() {
                    }
                }
                """);

        List<ExtractedSpringApi> apis = extractor.extract(sourceFile);

        assertThat(apis)
                .extracting(ExtractedSpringApi::httpMethod, ExtractedSpringApi::path, ExtractedSpringApi::methodName)
                .containsExactlyInAnyOrder(
                        tuple("PATCH", "/admin/orders/{orderId}/status", "patchStatus"),
                        tuple("POST", "/admin/orders/legacy-sync", "legacySync")
                );
    }
}
