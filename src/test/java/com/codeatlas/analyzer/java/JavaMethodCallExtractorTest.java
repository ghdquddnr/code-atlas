package com.codeatlas.analyzer.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JavaMethodCallExtractorTest {

    private final JavaMethodCallExtractor extractor = new JavaMethodCallExtractor();

    @Test
    void extractsDirectFieldMethodCallsFromSampleService() {
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
                "service",
                "OrderService.java"
        );

        List<ExtractedJavaClass> classes = extractor.extract(sourceFilePath);

        ExtractedJavaClass orderService = classes.getFirst();
        assertThat(orderService.className()).isEqualTo("OrderService");
        assertThat(orderService.category()).isEqualTo(JavaClassCategory.SERVICE);
        assertThat(orderService.fields())
                .extracting(ExtractedJavaField::name, ExtractedJavaField::type)
                .contains(
                        tuple("orderMapper", "OrderMapper"),
                        tuple("orderQueryService", "OrderQueryService")
                );
        assertThat(orderService.annotations()).contains("Service");
        assertThat(orderService.fieldTypes())
                .containsEntry("orderMapper", "OrderMapper")
                .containsEntry("orderQueryService", "OrderQueryService");
        assertThat(orderService.fieldTypes()).hasSize(2);
        assertThat(orderService.methodCalls())
                .extracting(
                        ExtractedMethodCall::callerMethodName,
                        ExtractedMethodCall::targetClassName,
                        ExtractedMethodCall::targetMethodName
                )
                .contains(
                        tuple("createOrder", "OrderMapper", "insertOrder"),
                        tuple("createOrder", "OrderMapper", "selectOrderDetail"),
                        tuple("getOrder", "OrderQueryService", "findOrderDetail"),
                        tuple("searchOrders", "OrderQueryService", "findRecentOrders"),
                        tuple("updateOrderStatus", "OrderMapper", "updateOrderStatus"),
                        tuple("cancelOrder", "OrderMapper", "deleteOrder"),
                        tuple("findOrderDetail", "OrderMapper", "selectOrderDetail")
                );
    }

    @Test
    void classifiesCommonSpringAndLegacyClassTypes(@TempDir Path tempDir) throws IOException {
        Path sourceFile = tempDir.resolve("MixedTypes.java");
        Files.writeString(sourceFile, """
                package com.example.legacy;

                import jakarta.persistence.Entity;
                import org.apache.ibatis.annotations.Mapper;
                import org.springframework.context.annotation.Configuration;
                import org.springframework.data.repository.Repository;
                import org.springframework.scheduling.annotation.Scheduled;
                import org.springframework.stereotype.Component;
                import org.springframework.stereotype.Service;
                import org.springframework.web.bind.annotation.RestController;

                @RestController
                class OrderController {}

                @Service
                class OrderService {}

                @Repository
                class OrderRepository {}

                @Mapper
                interface OrderMapper {}

                @Entity
                class OrderEntity {}

                record CreateOrderRequest(Long customerId, String memo) {}

                @Configuration
                class OrderConfiguration {}

                class OrderScheduler {
                    @Scheduled(cron = "0 0 * * * *")
                    void run() {}
                }

                class DailyOrderBatch {}

                @Component
                class OrderComponent {}

                class LegacyStringUtils {}
                """);

        List<ExtractedJavaClass> classes = extractor.extract(sourceFile);

        assertThat(classes)
                .extracting(ExtractedJavaClass::className, ExtractedJavaClass::category)
                .contains(
                        tuple("OrderController", JavaClassCategory.CONTROLLER),
                        tuple("OrderService", JavaClassCategory.SERVICE),
                        tuple("OrderRepository", JavaClassCategory.REPOSITORY),
                        tuple("OrderMapper", JavaClassCategory.MAPPER),
                        tuple("OrderEntity", JavaClassCategory.ENTITY),
                        tuple("CreateOrderRequest", JavaClassCategory.DTO),
                        tuple("OrderConfiguration", JavaClassCategory.CONFIGURATION),
                        tuple("OrderScheduler", JavaClassCategory.SCHEDULER),
                        tuple("DailyOrderBatch", JavaClassCategory.BATCH),
                        tuple("OrderComponent", JavaClassCategory.COMPONENT),
                        tuple("LegacyStringUtils", JavaClassCategory.UTILITY)
                );
        assertThat(classes)
                .filteredOn(javaClass -> javaClass.className().equals("CreateOrderRequest"))
                .singleElement()
                .satisfies(javaClass -> assertThat(javaClass.fields())
                        .extracting(ExtractedJavaField::name, ExtractedJavaField::type)
                        .contains(tuple("customerId", "Long")));
    }
}
