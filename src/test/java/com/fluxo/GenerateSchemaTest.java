package com.fluxo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.properties.jakarta.persistence.schema-generation.scripts.action=create",
    "spring.jpa.properties.jakarta.persistence.schema-generation.scripts.create-target=schema.sql",
    "spring.sql.init.mode=never",
    "spring.jpa.hibernate.ddl-auto=none"
})
public class GenerateSchemaTest {
    @Test
    public void generateSchema() {
        System.out.println("Generating schema...");
    }
}
