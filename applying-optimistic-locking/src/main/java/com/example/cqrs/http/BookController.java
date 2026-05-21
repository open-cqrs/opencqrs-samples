package com.example.cqrs.http;

import com.example.cqrs.domain.BookCatalogProjection;
import com.example.cqrs.domain.api.command.EditBookDetailsCommand;
import com.example.cqrs.domain.api.command.PurchaseBookCommand;
import com.opencqrs.framework.command.CommandRouter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final CommandRouter commandRouter;
    private final BookCatalogProjection projection;

    public BookController(CommandRouter commandRouter, BookCatalogProjection projection) {
        this.commandRouter = commandRouter;
        this.projection = projection;
    }

    @PostMapping
    public ResponseEntity<Void> purchase(@RequestBody PurchaseBookCommand command) {
        commandRouter.send(command);
        return ResponseEntity.created(URI.create("/books/" + command.isbn())).build();
    }

    @PutMapping
    public ResponseEntity<Void> editDetails(@RequestBody EditBookDetailsCommand body) {
        commandRouter.send(body);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{isbn}")
    public ResponseEntity<BookCatalogProjection.BookView> getBook(@PathVariable String isbn) {
        return projection.findByIsbn(isbn)
                .map(book -> ResponseEntity.ok().eTag(book.version()).body(book))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<BookCatalogProjection.BookView>> getAllBooks() {
        return ResponseEntity.ok(projection.findAll().stream().toList());
    }
}