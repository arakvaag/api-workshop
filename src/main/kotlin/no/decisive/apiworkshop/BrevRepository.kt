package no.decisive.apiworkshop

import org.springframework.stereotype.Repository

@Repository
class BrevRepository {

    private val lagredeBrev = mutableMapOf<Long, Brev.State>()

    fun lagre(brev: Brev) {
        lagredeBrev[brev.id] = brev.eksporterState()
    }

    fun soekPaaFoedselsnummer(fodselsnummer: String): List<Brev> {
        return lagredeBrev.values.filter { it.foedselsnummer == fodselsnummer }.map { Brev(it) }
    }

    fun slettAlt() {
        lagredeBrev.clear()
    }

    fun hentBrevPaaId(brevid: Long): Brev {
        return Brev(lagredeBrev[brevid]
            ?: throw EntitetFinnesIkkeException("Finnes ikke noe brev med id $brevid")
        )
    }

    class EntitetFinnesIkkeException(melding: String) : RuntimeException(melding)

}

