## How to Export as EXE and JAR
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.joshuasalcedo</groupId>
    <artifactId>applications</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>Work Manager</name>
    <description>A productivity management application</description>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>24</maven.compiler.source>
        <maven.compiler.target>24</maven.compiler.target>
        <java.version>24</java.version>
    </properties>

    <dependencies>
        <!-- YAML parsing library for configuration -->
        <dependency>
            <groupId>org.yaml</groupId>
            <artifactId>snakeyaml</artifactId>
            <version>2.0</version>
        </dependency>

        <!-- Java Desktop for Swing -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-swing</artifactId>
            <version>24</version>
        </dependency>

        <!-- Testing dependencies -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.1</version>
            <scope>test</scope>
        </dependency>

        <!-- Logging framework -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.10</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.5.0</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>24</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-web</artifactId>
            <version>24</version>
        </dependency>

        <!-- Existing dependencies... -->



        <!-- Uncomment for Linux 64-bit
        <dependency>
            <groupId>org.eclipse.swt</groupId>
            <artifactId>org.eclipse.swt.gtk.linux.x86_64</artifactId>
            <version>4.6.1</version>
        </dependency>
        -->

        <!-- Uncomment for macOS
        <dependency>
            <groupId>org.eclipse.swt</groupId>
            <artifactId>org.eclipse.swt.cocoa.macosx.x86_64</artifactId>
            <version>4.6.1</version>
        </dependency>
        -->



    </dependencies>
    <!-- Add TeamDev repository -->
    <!-- Add repository for Maven Central -->
    <repositories>
        <repository>
            <id>central</id>
            <url>https://repo.maven.apache.org/maven2</url>
        </repository>
    </repositories>


    <build>
        <resources>
            <resource>
                <directory>src/main/resources</directory>
                <filtering>true</filtering>
            </resource>
        </resources>
        <plugins>
            <!-- Compiler plugin configuration -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.12.1</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                    <compilerArgs>
                        <arg>--add-modules</arg>
                        <arg>java.desktop,jdk.jsobject</arg>
                    </compilerArgs>
                </configuration>
            </plugin>

            <!-- Create executable JAR with dependencies -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.6.0</version>
                <configuration>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                    <archive>
                        <manifest>
                            <mainClass>io.joshuasalcedo.applications.Main</mainClass>
                        </manifest>
                    </archive>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- Surefire plugin for running tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
            </plugin>

            <plugin>
                <groupId>com.akathist.maven.plugins.launch4j</groupId>
                <artifactId>launch4j-maven-plugin</artifactId>
                <version>2.5.0</version>
                <executions>
                    <execution>
                        <id>create-exe</id>
                        <phase>package</phase>
                        <goals>
                            <goal>launch4j</goal>
                        </goals>
                        <configuration>
                            <headerType>gui</headerType>
                            <outfile>${project.build.directory}/WorkManager.exe</outfile>
                            <jar>${project.build.directory}/${project.artifactId}-${project.version}-jar-with-dependencies.jar</jar>
                            <dontWrapJar>false</dontWrapJar>
                            <errTitle>Work Manager Error</errTitle>
                            <!-- Option 1: Comment out the icon line to build without an icon -->
                            <!-- <icon>${project.basedir}/src/main/resources/icon/app_icon.ico</icon> -->

                            <!-- Option 2: Update to use an existing file -->
                            <!-- If you have created the .ico file, use this option -->
                            <!-- <icon>${project.basedir}/src/main/resources/icon/app_icon.ico</icon> -->

                            <classPath>
                                <mainClass>io.joshuasalcedo.applications.Main</mainClass>
                            </classPath>
                            <jre>
                                <path>%JAVA_HOME%</path>
                                <minVersion>24.0.0</minVersion>
                                <requiresJdk>false</requiresJdk>
                            </jre>
                            <versionInfo>
                                <fileVersion>1.0.0.0</fileVersion>
                                <txtFileVersion>${project.version}</txtFileVersion>
                                <fileDescription>Work Manager Application</fileDescription>
                                <copyright>Copyright © 2024 Joshua Salcedo</copyright>
                                <productVersion>1.0.0.0</productVersion>
                                <txtProductVersion>${project.version}</txtProductVersion>
                                <productName>Work Manager</productName>
                                <internalName>workmanager</internalName>
                                <originalFilename>WorkManager.exe</originalFilename>
                            </versionInfo>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```
