package com.subsel.healthledger.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@Profile("dev")
public class SwaggerConfig {

//	private static final String AUTHORIZATION_HEADER = "Authorization";
//	private static final String DEFAULT_INCLUDE_PATTERN = "/.*";
//	private static final String CLIENT_ID = "dev@priyo.com";
//	private static final String CLIENT_SECRET = "dev";
//	private static final String AUTH_SERVER = "";

	@Bean(name = "SwaggerConfig")
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.priyo"))
				.paths(PathSelectors.any()).build();
	}
}
