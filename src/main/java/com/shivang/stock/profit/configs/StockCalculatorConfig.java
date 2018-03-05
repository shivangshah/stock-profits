package com.shivang.stock.profit.configs;

import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivang.stock.profit.utils.UnquestioningTruststoreManager;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static springfox.documentation.schema.AlternateTypeRules.newRule;

@Configuration
public class StockCalculatorConfig {


    /**
     * Rest Template bean autoconfigured with wildcard cert acceptance
     *
     * @return Rest Template bean
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    @Bean
    public RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        UnquestioningTruststoreManager.setupUnquestioningTruststoreManager();
        return new RestTemplate();
    }

    /**
     * Jackson Object Mapper bean autoconfigured to be used across the application
     *
     * @return Jackson ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        DateFormat dateFormatWithTime = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        mapper.setDateFormat(dateFormatWithTime);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return mapper;
    }

    /**
     * Bean creation for initializing Swagger Dockets
     *
     * @return
     */
    @Bean
    public Docket api(TypeResolver typeResolver, SwaggerApiInfo swaggerApiInfo) {
        return new Docket(DocumentationType.SWAGGER_2)
                .tags(new Tag("Stock Profit Calculator Service", "API to calculator best buy, best sell and probable profit based on a given time window"))
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.shivang.stock.profit"))
                .paths(PathSelectors.any())
                .build()
                .genericModelSubstitutes(ResponseEntity.class)
                .alternateTypeRules(
                        newRule(typeResolver.resolve(DeferredResult.class,
                                typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
                                typeResolver.resolve(WildcardType.class)))
                .apiInfo(apiInfo(swaggerApiInfo.getTitle(),
                        swaggerApiInfo.getDescription(),
                        swaggerApiInfo.getContactName(),
                        swaggerApiInfo.getContactUrl(),
                        swaggerApiInfo.getContactEmail(),
                        swaggerApiInfo.getVersion()));
    }

    private ApiInfo apiInfo(String title, String description, String contactName,
                            String contactUrl, String contactEmail, String version) {
        return new ApiInfoBuilder()
                .title(title)
                .description(description)
                .contact(new Contact(contactName, contactUrl, contactEmail))
                .version(version)
                .build();
    }

    @ConfigurationProperties(prefix = "springfox.documentation.swagger")
    @Component
    public static class SwaggerApiInfo {

        private String version;
        private String title;
        private String description;
        private String contactName;
        private String contactUrl;
        private String contactEmail;

        public SwaggerApiInfo() {
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getContactName() {
            return contactName;
        }

        public void setContactName(String contactName) {
            this.contactName = contactName;
        }

        public String getContactUrl() {
            return contactUrl;
        }

        public void setContactUrl(String contactUrl) {
            this.contactUrl = contactUrl;
        }

        public String getContactEmail() {
            return contactEmail;
        }

        public void setContactEmail(String contactEmail) {
            this.contactEmail = contactEmail;
        }
    }
}
