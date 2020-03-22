package com.bntan.tsa.service.web.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerDocumentationWebConfig {

    ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("TSA service")
                .description("TSA service REST API description. This service is used to timestamp PDF documents.")
                .version("1.0.0")
                .contact(new Contact("Bun-Ny TAN", "https://www.bntan.com", "me@bntan.com"))
                .build();
    }

    @Bean()
    public Docket webImplementation() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex("/timestamp"))
                .build()
                .groupName("TSA service")
                .directModelSubstitute(org.joda.time.LocalDate.class, java.sql.Date.class)
                .directModelSubstitute(org.joda.time.DateTime.class, java.util.Date.class)
                .apiInfo(apiInfo());
    }

}
