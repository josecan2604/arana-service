package com.clara.ops.challenge.document_management_service_challenge.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI documentManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("File Management API")
                        .description("Service for uploading, searching, and downloading PDF documents")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Arana Jose - Clara Team")
                                .email("support@clara.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}
