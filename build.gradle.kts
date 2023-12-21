import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.6.2"
	id("io.spring.dependency-management") version "1.0.14.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
}

group = "com.vicc"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("org.springframework.retry:spring-retry")
	implementation("org.springframework:spring-aspects")


	val elasticsearchVersion = "6.8.6"
	implementation("org.elasticsearch:elasticsearch:${elasticsearchVersion}")
	implementation("org.elasticsearch:elasticsearch-core:${elasticsearchVersion}")
	implementation("org.elasticsearch.client:elasticsearch-rest-client:${elasticsearchVersion}")
	implementation("org.elasticsearch.client:elasticsearch-rest-high-level-client:${elasticsearchVersion}")

	val springDataElasticsearchVersion = "3.2.13.RELEASE"
	implementation("org.springframework.data:spring-data-elasticsearch:${springDataElasticsearchVersion}") {
		exclude(group = "org.elasticsearch.client", module = "transport")
		exclude(group = "org.elasticsearch.plugin", module = "transport-netty4-client")
	}

//	implementation("org.springframework.data:spring-data-elasticsearch:3.2.13.RELEASE") {
//		exclude("org.elasticsearch.client:transport")
//		exclude("org.elasticsearch.plugin:transport-netty4-client")
//	}

	implementation(files("libs/ucloudstorage_sdk_v0.1.1.jar"))

	implementation("org.imgscalr:imgscalr-lib:4.2")


	implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka-streams")
	implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("log4j:log4j:1.2.17")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(module = "mockito-core")
	}

	implementation("org.jsoup:jsoup:1.15.3")

	implementation("org.apache.poi:poi:5.2.2")
	implementation("org.apache.poi:poi-ooxml:5.2.2")

	implementation("commons-net:commons-net:3.9.0")
	implementation("javax.xml.bind:jaxb-api:2.1")

	testImplementation("io.kotest:kotest-runner-junit5:4.6.4")
	testImplementation("io.kotest:kotest-extensions-spring:4.4.3")
	testImplementation("com.ninja-squad:springmockk:3.0.1")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2021.0.4")
		mavenBom("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
