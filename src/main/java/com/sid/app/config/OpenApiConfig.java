package com.sid.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    // ─────────────── Main OpenAPI Bean ───────────────

    @Bean
    public OpenAPI workplaceTrackerOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .externalDocs(new ExternalDocumentation()
                        .description("📖 Full Project Documentation & Source Code on GitHub")
                        .url("https://github.com/siddhantpatni0407/workplace-tracker-service")
                )
                .servers(List.of(
                        new Server().url("http://localhost:8010").description("🖥️ Local Development Server"),
                        new Server().url("https://api.workplace-tracker.com").description("🚀 Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer token. Obtain via `/login` (regular users) or `/platform-auth/login` (platform users). " +
                                        "Paste the raw token — the `Bearer ` prefix is added automatically.")
                        )
                )
                .tags(List.of(
                        new Tag().name("Authentication").description("User registration, login, password management and token refresh"),
                        new Tag().name("Platform User Auth").description("Platform-level user signup, login and token refresh"),
                        new Tag().name("User Management").description("Fetch, update, activate/deactivate and delete tenant users"),
                        new Tag().name("User Profile").description("Manage user profile information and addresses"),
                        new Tag().name("User Settings").description("Manage personal user settings and preferences"),
                        new Tag().name("Special Days").description("Birthdays, anniversaries and current-month special day listings"),
                        new Tag().name("Holiday Management").description("Create, retrieve, update and delete public holidays"),
                        new Tag().name("Leave Policy").description("Define and manage tenant-specific leave policies"),
                        new Tag().name("User Leave").description("Apply for, approve, reject and track user leaves"),
                        new Tag().name("User Leave Balance").description("View, adjust and recalculate user leave balances"),
                        new Tag().name("Office Visit").description("Record and query office visit check-ins"),
                        new Tag().name("Daily Tasks").description("Create and manage day-level task records"),
                        new Tag().name("Daily View Records").description("Fetch consolidated daily view records"),
                        new Tag().name("Analytics").description("Aggregate analytics for visits and leaves over time"),
                        new Tag().name("User Notes").description("Personal notes — create, search, pin, archive and bulk operations"),
                        new Tag().name("User Tasks").description("Personal task tracker — create, assign priority/status, overdue and bulk operations"),
                        new Tag().name("Tenant Management").description("Platform-level CRUD for tenants and tenant subscriptions"),
                        new Tag().name("Subscription Management").description("Manage subscription plans and assignments"),
                        new Tag().name("Platform User Management").description("Platform user management of Super Admins"),
                        new Tag().name("Super Admin Management").description("Super Admin management of Admin users"),
                        new Tag().name("Platform Stats").description("High-level platform-wide statistics"),
                        new Tag().name("Database Backup").description("Trigger and manage database backup operations")
                ));
    }

    // ─────────────── API Groups ───────────────

    @Bean
    public GroupedOpenApi allApis() {
        return GroupedOpenApi.builder()
                .group("00-all")
                .displayName("📋 All APIs")
                .pathsToMatch("/api/v1/workplace-tracker-service/**")
                .build();
    }

    @Bean
    public GroupedOpenApi publicApis() {
        return GroupedOpenApi.builder()
                .group("01-public")
                .displayName("🌐 Public APIs (No Auth Required)")
                .pathsToMatch(
                        "/api/v1/workplace-tracker-service/register",
                        "/api/v1/workplace-tracker-service/login",
                        "/api/v1/workplace-tracker-service/forgot/**",
                        "/api/v1/workplace-tracker-service/auth/refresh",
                        "/api/v1/workplace-tracker-service/platform-auth/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi userApis() {
        return GroupedOpenApi.builder()
                .group("02-user")
                .displayName("👤 User & Manager APIs")
                .pathsToMatch(
                        "/api/v1/workplace-tracker-service/notes/**",
                        "/api/v1/workplace-tracker-service/tasks/**",
                        "/api/v1/workplace-tracker-service/user-leaves/**",
                        "/api/v1/workplace-tracker-service/visits/**",
                        "/api/v1/workplace-tracker-service/user/profile/**",
                        "/api/v1/workplace-tracker-service/user/settings/**",
                        "/api/v1/workplace-tracker-service/user/change-password",
                        "/api/v1/workplace-tracker-service/special-days/**",
                        "/api/v1/workplace-tracker-service/fetch-daily-view-records"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi adminApis() {
        return GroupedOpenApi.builder()
                .group("03-admin")
                .displayName("🔑 Admin & Super Admin APIs")
                .pathsToMatch(
                        "/api/v1/workplace-tracker-service/user/**",
                        "/api/v1/workplace-tracker-service/holidays/**",
                        "/api/v1/workplace-tracker-service/leave-policies/**",
                        "/api/v1/workplace-tracker-service/daily-tasks/**",
                        "/api/v1/workplace-tracker-service/analytics/**",
                        "/api/v1/workplace-tracker-service/super-admin/**",
                        "/api/v1/workplace-tracker-service/user-leave-balance/**",
                        "/api/v1/workplace-tracker-service/db-backup/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi platformApis() {
        return GroupedOpenApi.builder()
                .group("04-platform")
                .displayName("🏢 Platform Admin APIs")
                .pathsToMatch(
                        "/api/v1/workplace-tracker-service/tenants/**",
                        "/api/v1/workplace-tracker-service/tenant/**",
                        "/api/v1/workplace-tracker-service/subscriptions/**",
                        "/api/v1/workplace-tracker-service/subscription/**",
                        "/api/v1/workplace-tracker-service/platform/**",
                        "/api/v1/workplace-tracker-service/platform-users/**"
                )
                .build();
    }

    // ─────────────── Global Response Customizer ───────────────

    @Bean
    public OperationCustomizer globalApiResponseCustomizer() {
        return (operation, handlerMethod) -> {
            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }
            responses.putIfAbsent("400", buildApiResponse("❌ Bad Request — Invalid input or missing required fields"));
            responses.putIfAbsent("401", buildApiResponse("🔒 Unauthorized — Missing or invalid JWT token"));
            responses.putIfAbsent("403", buildApiResponse("🚫 Forbidden — Insufficient role/permissions for this operation"));
            responses.putIfAbsent("404", buildApiResponse("🔍 Not Found — The requested resource does not exist"));
            responses.putIfAbsent("500", buildApiResponse("💥 Internal Server Error — An unexpected error occurred"));
            return operation;
        };
    }

    @SuppressWarnings("rawtypes")
    private ApiResponse buildApiResponse(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content()
                        .addMediaType(
                                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ResponseDTO"))
                        )
                );
    }

    // ─────────────── API Info ───────────────

    private Info apiInfo() {
        return new Info()
                .title("Workplace Tracker Service API")
                .description("""
                        REST API for the **Workplace Tracker Service** — a multi-tenant workforce management platform.

                        ## 🔐 Authentication
                        Most endpoints require a valid JWT Bearer token. Obtain one via:
                        - **POST** `/api/v1/workplace-tracker-service/login` — for regular users
                        - **POST** `/api/v1/workplace-tracker-service/platform-auth/login` — for platform users

                        Click the **Authorize 🔒** button, paste the raw token (no `Bearer ` prefix needed), then click **Authorize**.

                        ---

                        ## 👥 Roles & Access Levels
                        | Role | Level | Description |
                        |------|-------|-------------|
                        | `PLATFORM_USER` | Platform | Top-level platform administrator |
                        | `SUPER_ADMIN` | Tenant | Tenant-level super administrator |
                        | `ADMIN` | Tenant | Tenant administrator |
                        | `MANAGER` | User | Team manager |
                        | `USER` | User | Regular employee |

                        ---

                        ## 📦 Standard Response Envelope
                        All endpoints return the same JSON structure:
                        ```json
                        {
                          "status":  "SUCCESS | FAILED",
                          "message": "Human-readable result message",
                          "data":    { }
                        }
                        ```

                        ---

                        ## 🏷️ API Groups
                        Use the **definition selector** (top-left dropdown) to browse by access level:
                        - 🌐 **Public APIs** — No authentication required
                        - 👤 **User & Manager APIs** — USER / MANAGER role
                        - 🔑 **Admin & Super Admin APIs** — ADMIN / SUPER_ADMIN role
                        - 🏢 **Platform Admin APIs** — PLATFORM_USER role
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Siddhant Patni")
                        .email("siddhantpatni0407@gmail.com")
                        .url("https://github.com/siddhantpatni0407")
                )
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")
                );
    }
}
