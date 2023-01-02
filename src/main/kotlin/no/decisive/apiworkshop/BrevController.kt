package no.decisive.apiworkshop

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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

    @PostMapping
    fun oppretteBrev(@RequestBody body: String?): ResponseEntity<String> {
        if (body == null) {
            return ResponseEntity(""" { "feilmelding": "Body i request var tom" } """, HttpStatus.BAD_REQUEST)
        }

        val opprettBrevRequest = try {
            objectMapper.readValue<BrevService.OpprettBrevRequest>(body)
        } catch (e: JsonProcessingException) {
            return ResponseEntity(
                """ { "feilmelding": "JSON i request-body var ikke gyldig" } """,
                HttpStatus.BAD_REQUEST
            )
        }

        val brev = brevService.opprettBrev(opprettBrevRequest)

        return ResponseEntity(objectMapper.writeValueAsString(brev), HttpStatus.CREATED)
    }

    @GetMapping("/{id}")
    fun henteBrev(@PathVariable id: Long): ResponseEntity<String> {
        val brev = try {
            brevService.hentBrevPaaId(id)
        } catch (e: BrevRepository.EntitetFinnesIkkeException) {
            return ResponseEntity(""" { "feilmelding": "Finnes ikke et brev med id $id" } """, HttpStatus.NOT_FOUND)
        }
        return ResponseEntity(objectMapper.writeValueAsString(brev), HttpStatus.OK)
    }

    @PatchMapping("/{id}")
    fun endreBrev(
        @PathVariable id: Long,
        @RequestBody body: String?,
        @RequestHeader("Brev-Version") version: String?
    ): ResponseEntity<String> {

        if (body == null) {
            return ResponseEntity(""" { "feilmelding": "Body i request var tom." } """, HttpStatus.BAD_REQUEST)
        }

        if (version == null || version.toIntOrNull() == null) {
            return ResponseEntity(
                """ { "feilmelding": "Request hadde ikke gyldig header Brev-Version " } """,
                HttpStatus.BAD_REQUEST
            )
        }

        val brevendringerJSONNode = try {
            objectMapper.readTree(body)
        } catch (e: JsonProcessingException) {
            return ResponseEntity(
                """ { "feilmelding": "JSON i request-body var ikke gyldig." } """,
                HttpStatus.BAD_REQUEST
            )
        }

        if (brevendringerJSONNode["nyTittel"] == null && brevendringerJSONNode["nyBroedtekst"] == null) {
            return ResponseEntity(
                """ { "feilmelding": "Ingen endringer angitt i request-body." } """,
                HttpStatus.BAD_REQUEST
            )
        }

        val brevendringer = BrevService.Brevendringer()
        if (brevendringerJSONNode["nyTittel"] != null) {
            brevendringer.medNyTittel(brevendringerJSONNode["nyTittel"].textValue())
        }
        if (brevendringerJSONNode["nyBroedtekst"] != null) {
            brevendringer.medNyBroedtekst(brevendringerJSONNode["nyBroedtekst"].textValue())
        }

        try {
            brevService.endreDataIBrev(brevid = id, brevendringer = brevendringer, forventetVersion = version.toInt())
        } catch (e: BrevRepository.EntitetFinnesIkkeException) {
            return ResponseEntity(""" { "feilmelding": "Finnes ikke et brev med id $id." } """, HttpStatus.NOT_FOUND)
        } catch (e: IllegalStateException) {
            return ResponseEntity(""" { "feilmelding": "${e.message}" } """, HttpStatus.BAD_REQUEST)
        } catch (e: Brev.OptimistiskLaasingFeiletException) {
            return ResponseEntity(
                """ { "feilmelding": "Oppgitt Brev-Version var ulik eksisterende version i database, optimistisk låsing feilet." } """,
                HttpStatus.PRECONDITION_FAILED
            )
        }

        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/{id}/send")
    fun sendBrev(
        @PathVariable id: Long,
        @RequestHeader("Brev-Version") version: String?
    ): ResponseEntity<String> {
        if (version == null || version.toIntOrNull() == null) {
            return ResponseEntity(
                """ { "feilmelding": "Request hadde ikke gyldig header Brev-Version " } """,
                HttpStatus.BAD_REQUEST
            )
        }

        try {
            brevService.sendBrev(brevid = id, forventetVersion = version.toInt())
        } catch (e: BrevRepository.EntitetFinnesIkkeException) {
            return ResponseEntity(""" { "feilmelding": "Finnes ikke et brev med id $id." } """, HttpStatus.NOT_FOUND)
        } catch (e: IllegalStateException) {
            return ResponseEntity(""" { "feilmelding": "${e.message}" } """, HttpStatus.BAD_REQUEST)
        } catch (e: Brev.OptimistiskLaasingFeiletException) {
            return ResponseEntity(
                """ { "feilmelding": "Oppgitt Brev-Version var ulik eksisterende version i database, optimistisk låsing feilet." } """,
                HttpStatus.PRECONDITION_FAILED
            )
        }

        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping("/soek")
    fun soekEtterBrev(@RequestBody body: String?): ResponseEntity<String> {
        if (body == null) {
            return ResponseEntity(""" { "feilmelding": "Body i request var tom" } """, HttpStatus.BAD_REQUEST)
        }
        val foedselsnummer = objectMapper.readTree(body)["foedselsnummer"]?.textValue()
            ?: return ResponseEntity(""" { "feilmelding": "Body i request manglet foedselsnummer" } """, HttpStatus.BAD_REQUEST)

        val brev = brevService.soekEtterBrevPaaFodselsnummer(foedselsnummer)

        return ResponseEntity(objectMapper.writeValueAsString(brev), HttpStatus.OK)
    }


}

