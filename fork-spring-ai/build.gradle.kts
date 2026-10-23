plugins {
	java
	id("org.springframework.boot") version "4.1.0-SNAPSHOT"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com"
version = "0.0.1-SNAPSHOT"
description = "Spring Webflux + AIChatClient"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	// 최신화된 AI 버전과의 호환성 우회
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://repo.spring.io/snapshot") }
	maven {
		name = "Central Portal Snapshots"
		url = uri("https://repo.spring.io/snapshots/central-portal")
	}
}

extra["springAiVersion"] = "2.0.0-SNAPSHOT"

dependencies {
	// implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.ai:spring-ai-starter-model-ollama")
	implementation("org.springframework.ai:spring-ai-starter-model-openai")
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation(platform("org.springframework.ai:spring-ai-bom:2.0.1")) // 버전 강제
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-webtestclient")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("org.springframework.boot:spring-boot-starter-data-r2dbc-test")
	// R2DBC 의존성 추가
	runtimeOnly("org.postgresql:r2dbc-postgresql")
	testImplementation("org.testcontainers:testcontainers-r2dbc") // 비동기 처리
	testImplementation("io.projectreactor:reactor-test")

}
// Spring AI Bom 버전 ( spring-ai-bom:2.0.1 )
/*
dependencyManagement {
	imports {
		mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
	}
}
*/
tasks.withType<Test> {
	useJUnitPlatform()
}
