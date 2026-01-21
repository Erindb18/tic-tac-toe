plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}

javafx {
    version = "23.0.1"
    modules = listOf("javafx.controls")
}

application {
    mainClass.set("de.erind.tictactoe.MainApp")
}
