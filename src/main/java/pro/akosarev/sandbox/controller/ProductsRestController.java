package pro.akosarev.sandbox.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/catalogue/products")
public class ProductsRestController {

    public record ProductV1Presentation(String id, String title, String details) {
    }

    public record NewProductPayloadV1(String title, String details) {
    }

    @GetMapping
    public ResponseEntity<List<ProductV1Presentation>> getProducts() {
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("application/vnd.eselpo.catalogue.products.v1+json"))
                .body(List.of(
                        new ProductV1Presentation("a396a088-172c-11ee-aa6f-4f6009552211",
                                "Молоко, 3,2%, 1 литр",
                                "Молоко с жирностью 3,2% в упаковке 1 литр"),
                        new ProductV1Presentation("a396a088-172c-11ee-aa6f-4f6009552212",
                                "Кефир, 3,2%, 0,5 литра",
                                "Кефир с жирностью 3,2% в упаковке 0,5 литра")
                ));
    }

    @PostMapping
    public ResponseEntity<ProductV1Presentation> createProduct(
            @RequestBody NewProductPayloadV1 payload,
            UriComponentsBuilder uriComponentsBuilder) {
        if (payload.title == null) {
            return ResponseEntity.badRequest().build();
        }

        var id = UUID.randomUUID();
        return ResponseEntity.created(uriComponentsBuilder.pathSegment(id.toString())
                .build(Map.of()))
                .contentType(MediaType.valueOf("application/vnd.eselpo.catalogue.product.v1+json"))
                .body(new ProductV1Presentation(id.toString(), payload.title, payload.details));
    }
}
