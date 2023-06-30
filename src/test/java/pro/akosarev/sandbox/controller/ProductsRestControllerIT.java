package pro.akosarev.sandbox.controller;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.whitelist.ValidationErrorsWhitelist;
import com.atlassian.oai.validator.whitelist.rule.WhitelistRules;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductsRestControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getProduct_ReturnsResponseWithStatusOk() throws Exception {
        // given
        var requestBuilder = MockMvcRequestBuilders.get("/api/catalogue/products");

        // when
        this.mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        openApi().isValid("static/openapi.json"),
                        content().contentTypeCompatibleWith("application/vnd.eselpo.catalogue.products.v1+json"),
                        content().json("""
                                [
                                  {
                                    "id": "a396a088-172c-11ee-aa6f-4f6009552211",
                                    "title": "Молоко, 3,2%, 1 литр",
                                    "details": "Молоко с жирностью 3,2% в упаковке 1 литр"
                                  },
                                  {
                                    "id": "a396a088-172c-11ee-aa6f-4f6009552212",
                                    "title": "Кефир, 3,2%, 0,5 литра",
                                    "details": "Кефир с жирностью 3,2% в упаковке 0,5 литра"
                                  }
                                ]
                                """)
                );
    }

    @Test
    void createProduct_ReturnsResponseWithStatusCreated() throws Exception {
        // given
        var requestBuilder = post("/api/catalogue/products")
                .contentType("application/vnd.eselpo.catalogue.new-product-payload.v1+json")
                .content("""
                        {
                            "title": "Молоко, 3,2%, 1 литр",
                            "details": "Молоко с жирностью 3,2% в упаковке 1 литр"
                        }
                        """);

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andExpectAll(
                        status().isCreated(),
                        openApi().isValid("static/openapi.json"),
                        header().exists(HttpHeaders.LOCATION),
                        content().contentTypeCompatibleWith("application/vnd.eselpo.catalogue.product.v1+json"),
                        content().json("""
                                {
                                    "title": "Молоко, 3,2%, 1 литр",
                                    "details": "Молоко с жирностью 3,2% в упаковке 1 литр"
                                }
                                """),
                        jsonPath("$.id").exists()
                );
    }

    @Test
    void createProduct_PayloadIsInvalid_ReturnsResponseWithStatusCreated() throws Exception {
        // given
        var requestBuilder = post("/api/catalogue/products")
                .contentType("application/vnd.eselpo.catalogue.new-product-payload.v1+json")
                .content("""
                        {
                            "title": null,
                            "details": "Молоко с жирностью 3,2% в упаковке 1 литр"
                        }
                        """);

        // when
        this.mockMvc.perform(requestBuilder)
                // then
                .andExpectAll(
                        status().isBadRequest(),
                        openApi().isValid(OpenApiInteractionValidator.createFor("static/openapi.json")
                                        .withWhitelist(ValidationErrorsWhitelist.create()
                                                .withRule("Ignoring null title",
                                                        WhitelistRules
                                                                .messageHasKey("validation.request.body.schema.type")))
                                .build())
                );
    }
}
