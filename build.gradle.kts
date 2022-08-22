import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication

plugins {
  `java`
  `maven-publish`
}

group = "com.loosebazooka.example"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = rootProject.name
            from(components["java"])

            pom {
                name.set(rootProject.name)
                description.set("xyz")
                url.set("https://github.com/loosebazooka/slsa-java-test")

                // https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:resolved_dependencies
                versionMapping {
                    usage("java-api") {
                        fromResolutionOf("runtimeClasspath")
                    }
                    usage("java-runtime") {
                        fromResolutionResult()
                    }
                }

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        organization.set("loosebazooka corp")
                        organizationUrl.set("https://loosebazooka.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/loosebazooka/slsa-java-test.git")
                    developerConnection.set("scm:git:ssh://github.com/loosebazooka/slsa-java-test.git")
                    url.set("https://github.com/sigstore/sigstore-java")
                }
            }
        }
    }
}

// this task should be used by github actions to create a release bundle along with a slsa
// attestation. The artifact can be uploaded to maven central using the
// ./scripts/sign-and-release.sh script against a release artifact
tasks.register("createReleaseBundle") {
    val releaseDir = layout.buildDirectory.dir("release")
    outputs.dir(releaseDir)
    dependsOn((publishing.publications["mavenJava"] as DefaultMavenPublication).publishableArtifacts)
    doLast {
        (publishing.publications["mavenJava"] as DefaultMavenPublication).publishableArtifacts.files
            .forEach {
                project.copy {
                    from(it.absolutePath)
                    into(releaseDir)
                }
            }
    }
}
