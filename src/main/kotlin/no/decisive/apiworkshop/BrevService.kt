package no.decisive.apiworkshop

import org.springframework.stereotype.Service

@Service
class BrevService(
    private val brevRepository: BrevRepository,
    private val idProvider: IdProvider
) {

    fun opprettBrev(opprettBrevRequest: OpprettBrevRequest): Brev {
        val brev = Brev(
            idProvider,
            opprettBrevRequest.foedselsnummer,
            opprettBrevRequest.tittel,
            opprettBrevRequest.broedtekst
        )

        brevRepository.lagre(brev)

        return brev
    }

    fun hentBrevPaaId(brevid: Long): Brev {
        return brevRepository.hentBrevPaaId(brevid)
    }

    fun endreDataIBrev(brevid: Long, brevendringer: Brevendringer, forventetVersion: Int) {
        require(brevendringer.harNyVerdiForTittel || brevendringer.harNyVerdiForBroedtekst) {
            "Ingen endringer spesifisert"
        }
        val brev = brevRepository.hentBrevPaaId(brevid)
        brev.endreData(endringer = brevendringer, forventetVersion = forventetVersion)
        brevRepository.lagre(brev)
    }

    fun sendBrev(brevid: Long, forventetVersion: Int) {
        val brev = brevRepository.hentBrevPaaId(brevid)
        brev.send(forventetVersion = forventetVersion)
        brevRepository.lagre(brev)
    }

    fun soekEtterBrevPaaFodselsnummer(foedselsnummer: String): List<Brev> {
        return brevRepository.soekPaaFoedselsnummer(foedselsnummer)
    }

    data class OpprettBrevRequest(
        val foedselsnummer: String,
        val tittel: String?,
        val broedtekst: String?
    )

    class Brevendringer {
        var nyVerdiForTittel: String? = null
            private set
        var nyVerdiForBroedtekst: String? = null
            private set

        var harNyVerdiForTittel: Boolean = false
            private set
        var harNyVerdiForBroedtekst: Boolean = false
            private set

        fun medNyTittel(nyTittel: String): Brevendringer {
            harNyVerdiForTittel = true
            nyVerdiForTittel = nyTittel
            return this
        }

        fun medNyBroedtekst(nyBroedtekst: String): Brevendringer {
            harNyVerdiForBroedtekst = true
            nyVerdiForBroedtekst = nyBroedtekst
            return this
        }
    }
}


