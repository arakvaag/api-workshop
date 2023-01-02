package no.decisive.apiworkshop

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    path = ["/api/brev"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class BrevController {

    @GetMapping("/ping")
    fun ping(): ResponseEntity<String> {
        return ResponseEntity.ok(""" { "ping": "pong" } """)
    }

}

