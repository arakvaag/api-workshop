package no.decisive.apiworkshop

import org.springframework.stereotype.Component

@Component
class IdProvider {

    private var sistTildelteBrevid: Long = 0L

    fun hentNyBrevid(): Long {
        return ++sistTildelteBrevid
    }
}