package no.decisive.apiworkshop

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    path = ["/brev"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class BrevController(
    private val objectMapper: ObjectMapper,
    private val brevService: BrevService
) {

    @GetMapping("/ping")
    fun ping(): ResponseEntity<String> {
        return ResponseEntity.ok(""" { "ping": "pong" } """)
    }

}

