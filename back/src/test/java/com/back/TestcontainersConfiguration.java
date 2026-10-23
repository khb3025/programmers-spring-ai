package com.back;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
	@Value("${DATABASE_USERNAME:faq}")
	private String username;
	@Value("${DATABASE_PASSWORD:faq123}")
	private String password;
	@Value("${DATABASE_NAME:faqdb}")
	private String databaseName;
	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer(DockerImageName
				.parse("groonga/pgroonga:latest-alpine-18-slim")
				.asCompatibleSubstituteFor("postgres"))
				.withDatabaseName(databaseName)
				.withUsername(username)
				.withPassword(password);
	}
}