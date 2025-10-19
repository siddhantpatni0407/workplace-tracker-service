# 🔧 Build Plugins & Tools Documentation

## Workplace Tracker Service - Build System Overview

This document provides comprehensive information about all plugins, tools, and configurations used in the Workplace Tracker Service build system.

---

## 📋 Table of Contents

- [Core Gradle Plugins](#core-gradle-plugins)
- [Spring Boot Plugins](#spring-boot-plugins)
- [Code Quality & Coverage](#code-quality--coverage)
- [Custom Build Tasks](#custom-build-tasks)
- [Dependencies Overview](#dependencies-overview)
- [Build Configuration](#build-configuration)
- [Usage Commands](#usage-commands)
- [Report Generation](#report-generation)

---

## 🏗️ Core Gradle Plugins

### 1. Java Plugin (`java`)
**Purpose**: Provides Java compilation, testing, and packaging capabilities.

**Configuration**:
```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```

**Features**:
- ☑️ Java 21 language support via toolchain
- ☑️ Source and target compatibility management
- ☑️ Compilation and JAR packaging
- ☑️ Test execution framework

**Commands**:
- `gradle compileJava` - Compile main source code
- `gradle compileTestJava` - Compile test source code
- `gradle jar` - Create JAR file

---

## 🚀 Spring Boot Plugins

### 1. Spring Boot Plugin (`org.springframework.boot`)
**Version**: `3.5.5`  
**Purpose**: Provides Spring Boot application packaging and executable JAR creation.

**Features**:
- ☑️ Executable JAR/WAR packaging
- ☑️ Development tools integration
- ☑️ Dependency management
- ☑️ Application startup optimization

**Configuration**:
```groovy
id 'org.springframework.boot' version "${springBootVersion}"
```

**Commands**:
- `gradle bootRun` - Run application in development mode
- `gradle bootJar` - Create executable JAR
- `gradle bootBuildImage` - Create Docker image

### 2. Spring Dependency Management (`io.spring.dependency-management`)
**Version**: `1.1.7`  
**Purpose**: Manages Spring Boot dependencies and versions.

**Features**:
- ☑️ Automatic version management for Spring dependencies
- ☑️ BOM (Bill of Materials) import
- ☑️ Dependency conflict resolution
- ☑️ Platform dependency management

---

## 📊 Code Quality & Coverage

### 1. JaCoCo Plugin (`jacoco`)
**Version**: `0.8.11`  
**Purpose**: Code coverage analysis and reporting.

**Configuration**:
```groovy
jacoco {
    toolVersion = "0.8.11"
}

jacocoTestReport {
    dependsOn test
    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
    
    // Excludes configuration, DTO, entity, model classes
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                "**/config/**",
                "**/dto/**", 
                "**/entity/**",
                "**/model/**",
                "**/*Application*",
                "**/*Config*"
            ])
        }))
    }
}
```

**Features**:
- ☑️ Line, branch, and instruction coverage
- ☑️ HTML and XML report generation
- ☑️ Configurable coverage thresholds
- ☑️ Integration with build pipeline
- ☑️ Excludes non-business logic classes

**Reports Location**: `build/reports/jacoco/test/html/index.html`

**Commands**:
- `gradle jacocoTestReport` - Generate coverage reports
- `gradle jacocoTestCoverageVerification` - Verify coverage thresholds

**Coverage Thresholds**:
- Current minimum: `0%` (for initial development)
- Target minimum: `70%` (when tests are implemented)

---

## 📈 Custom Build Tasks

### 1. Lines of Code (LOC) Statistics (`locStats`)
**File**: `gradle/loc-stats.gradle`  
**Purpose**: Generates detailed code statistics and beautiful HTML reports.

**Supported File Types**:
- **Java**: `**/*.java`
- **Kotlin**: `**/*.kt` 
- **Groovy**: `**/*.groovy`
- **XML**: `**/*.xml`
- **Properties**: `**/*.properties`
- **YAML**: `**/*.yml`, `**/*.yaml`
- **SQL**: `**/*.sql`
- **Scripts**: `**/*.bat`, `**/*.sh`
- **Web**: `**/*.js`, `**/*.ts`, `**/*.css`, `**/*.html`

**Features**:
- ☑️ Professional HTML reports with animations and colors
- ☑️ File-by-file analysis with ranking system
- ☑️ Extension-based categorization with color coding
- ☑️ Interactive tables with hover effects
- ☑️ Summary statistics dashboard
- ☑️ Responsive design for mobile devices
- ☑️ Text and HTML report formats

**Report Features**:
- 🎨 **Gradient backgrounds** with glassmorphism effects
- 🏆 **Ranking system** for largest files (top 5 highlighted)
- 🎯 **Color-coded badges** for each file type:
  - 🔴 JAVA - Red badge
  - 🟢 SQL - Green badge
  - 🟡 BAT - Yellow badge
  - 🔵 YAML/YML - Teal badge
  - 🟣 XML - Blue badge
- ✨ **Hover animations** and smooth transitions
- 📱 **Mobile responsive** design

**Reports Location**: 
- HTML: `build/reports/loc/loc-report.html`
- Text: `build/reports/loc/loc.txt`

**Commands**:
- `gradle locStats` - Generate LOC statistics
- Automatically runs with `gradle build`

**Customization Options**:
```bash
# Include only specific file types
gradle locStats -PlocInclude="**/*.java,**/*.sql"

# Exclude certain patterns  
gradle locStats -PlocExclude="**/generated/**,**/vendor/**"
```

---

## 📦 Dependencies Overview

### Core Dependencies

| Category | Dependencies | Purpose |
|----------|--------------|---------|
| **Spring Framework** | `spring-boot-starter-web`<br>`spring-boot-starter-data-jpa`<br>`spring-boot-starter-security`<br>`spring-boot-starter-validation`<br>`spring-boot-starter-aop` | Web MVC, Data persistence, Security, Validation, AOP |
| **Database** | `postgresql`<br>`liquibase-core` | PostgreSQL driver, Database migrations |
| **Security** | `jjwt-api` v0.12.6<br>`jjwt-impl`<br>`jjwt-jackson` | JWT token handling (latest secure version) |
| **Documentation** | `springdoc-openapi-starter-webmvc-ui` v2.7.0 | OpenAPI/Swagger documentation |
| **Utilities** | `lombok` v1.18.34<br>`jackson-datatype-jsr310` v2.18.1<br>`commons-beanutils` v1.9.4 | Code generation, Date handling, Utilities |
| **Testing** | `spring-boot-starter-test`<br>`testcontainers` v1.20.4 | Unit testing, Integration testing |
| **Logging** | `slf4j-ext` | Enhanced logging capabilities |

### Security Updates Applied
- ✅ **JWT Library**: Updated to v0.12.6 (latest secure version)
- ✅ **Commons BeanUtils**: Updated to v1.9.4 (security fix)

---

## ⚙️ Build Configuration

### Java Configuration
```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```
- **Java Version**: 21 (LTS)
- **Toolchain**: Automatic JDK management
- **Source/Target Compatibility**: Java 21

### Repositories
- **Maven Central**: Primary dependency source

### Configurations
- **Annotation Processing**: Lombok integration
- **Compile-only Dependencies**: Development-time only dependencies

---

## 🚀 Usage Commands

### Basic Build Commands
```bash
# Clean and build the application
gradle clean build

# Run the application in development mode  
gradle bootRun

# Create executable JAR
gradle bootJar

# Run tests only
gradle test

# Skip tests during build
gradle build -x test
```

### Quality & Analysis Commands
```bash
# Generate code coverage report
gradle jacocoTestReport

# Verify coverage thresholds
gradle jacocoTestCoverageVerification  

# Generate LOC statistics
gradle locStats

# Run all quality checks
gradle check
```

### Docker Commands
```bash
# Build Docker image
gradle bootBuildImage

# Build with custom image name
gradle bootBuildImage --imageName=workplace-tracker:latest
```

---

## 📊 Report Generation

### Automated Reports Generated on Build

| Report Type | Location | Description |
|-------------|----------|-------------|
| **Code Coverage** | `build/reports/jacoco/test/html/index.html` | Interactive coverage report with line-by-line analysis |
| **LOC Statistics** | `build/reports/loc/loc-report.html` | Beautiful interactive code statistics with file analysis |
| **Test Results** | `build/reports/tests/test/index.html` | Detailed test execution results |
| **Problems Report** | `build/reports/problems/problems-report.html` | Build problems and deprecation warnings |

### Report Features

#### JaCoCo Coverage Report
- 📈 **Coverage Metrics**: Line, branch, and instruction coverage
- 🎯 **Class-level Analysis**: Detailed breakdown per class
- 🔍 **Source Code View**: Line-by-line coverage highlighting
- 📊 **Summary Dashboard**: Project-wide coverage overview

#### LOC Statistics Report  
- 🎨 **Modern UI**: Professional design with gradients and animations
- 📁 **File Analysis**: Detailed file-by-file breakdown
- 🏆 **Rankings**: Top files by size with special highlighting
- 📊 **Statistics**: Comprehensive metrics and summaries
- 🎯 **File Type Analysis**: Breakdown by extension with color coding

---

## 🔧 Build Pipeline Integration

### Task Dependencies
```
build
├── jacocoTestCoverageVerification
│   └── jacocoTestReport  
│       └── test
└── locStats
    └── classes
        └── compileJava
```

### Quality Gates
1. ✅ **Compilation**: All Java code must compile successfully
2. ✅ **Tests**: All unit tests must pass
3. ✅ **Coverage**: Code coverage verification (currently 0%, target 70%)
4. ✅ **Statistics**: LOC analysis and reporting

---

## 🛠️ Maintenance & Updates

### Regular Maintenance Tasks
- 📅 **Dependency Updates**: Check for security updates monthly
- 📊 **Coverage Goals**: Increase coverage threshold as tests are added  
- 🧹 **Clean Reports**: Reports are regenerated on each build
- 🔍 **Plugin Updates**: Monitor for plugin version updates

### Version Management
All versions are centrally managed in `gradle.properties`:
- Spring Boot: `springBootVersion=3.5.5`
- Java: `javaVersion=21` 
- JaCoCo: `jacocoVersion=0.8.11`
- Dependencies: Centralized version properties

---

## 📞 Support & Troubleshooting

### Common Issues & Solutions

**Build Failure**: 
```bash
gradle clean build --refresh-dependencies
```

**Coverage Report Not Generating**:
```bash  
gradle clean test jacocoTestReport
```

**LOC Stats Missing**:
```bash
gradle clean classes locStats
```

**Dependency Conflicts**:
```bash
gradle dependencies --configuration compileClasspath
```

---

## 📚 Additional Resources

- [Spring Boot Gradle Plugin Documentation](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/html/)
- [JaCoCo Gradle Plugin Guide](https://docs.gradle.org/current/userguide/jacoco_plugin.html)
- [Gradle Build Tool Documentation](https://docs.gradle.org/)
- [Spring Boot Reference Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/)

---

*Generated for Workplace Tracker Service v0.0.1-SNAPSHOT*  
*Build System: Gradle with Java 21*  
*Last Updated: October 2025*
