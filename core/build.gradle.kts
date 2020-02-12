/*
 * Copyright (C) 2019 Knot.x Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.nosphere.apache.rat.RatTask

plugins {
    id("io.knotx.java-library")
    id("io.knotx.codegen")
    id("io.knotx.codegen-test")
    id("io.knotx.unit-test")
    id("io.knotx.maven-publish")
    id("io.knotx.jacoco")
    id("org.nosphere.apache.rat") version "0.6.0"
}

dependencies {
    implementation(platform("io.knotx:knotx-dependencies:${project.version}"))

    api(project(":knotx-server-http-api"))
    implementation(group = "io.vertx", name = "vertx-core")
    implementation(group = "io.vertx", name = "vertx-service-proxy")
    implementation(group = "io.vertx", name = "vertx-rx-java2")
    implementation(group = "io.vertx", name = "vertx-web-api-contract")
    implementation(group = "io.vertx", name = "vertx-web-api-service")
    implementation(group = "io.vertx", name = "vertx-web")
    implementation(group = "io.vertx", name = "vertx-web-client")
    implementation(group = "io.vertx", name = "vertx-circuit-breaker")

    testImplementation(group = "io.vertx", name = "vertx-auth-jwt")
    testImplementation(group = "io.vertx", name = "vertx-auth-shiro")

    testImplementation("io.knotx:knotx-launcher:${project.version}")
    testImplementation(group = "org.mockito", name = "mockito-core")
    testImplementation(group = "org.mockito", name = "mockito-junit-jupiter")
    testImplementation(group = "io.rest-assured", name = "rest-assured", version = "3.3.0")
}

sourceSets.named("test") {
    resources.srcDir("../conf")
}

tasks {
    named<RatTask>("rat") {
        excludes.addAll(listOf("*.md", "**/*.md", "**/build/*", "**/out/*", "**/generated/*", "**/*.adoc", "**/security/keystore.jceks"))
    }
    getByName("build").dependsOn("rat")
}

publishing {
    publications {
        withType(MavenPublication::class) {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }
}