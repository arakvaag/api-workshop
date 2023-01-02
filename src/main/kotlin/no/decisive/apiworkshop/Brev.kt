package no.decisive.apiworkshop

import java.time.LocalDateTime

class Brev {

    constructor(idProvider: IdProvider, foedselsnummer: String, tittel: String?, broedtekst: String?) {
        this.id = idProvider.hentNyBrevid()
        this.version = 1
        this.status = Status.GJOERES_KLAR_FOR_SENDING
        this.foedselsnummer = foedselsnummer
        this.tittel = tittel
        this.broedtekst = broedtekst
        this.datoSendt = null
    }

    constructor(state: State) {
        this.id = state.id
        this.version = state.version
        this.status = state.status
        this.foedselsnummer = state.foedselsnummer
        this.tittel = state.tittel
        this.broedtekst = state.broedtekst
        this.datoSendt = state.datoSendt
    }

    enum class Status { GJOERES_KLAR_FOR_SENDING, SENDT }

    val id: Long
    var version: Int
        private set
    var status: Status
        private set
    val foedselsnummer: String
    var tittel: String?
        private set
    var broedtekst: String?
        private set
    var datoSendt: LocalDateTime?
        private set

    fun endreData(endringer: BrevService.Brevendringer, forventetVersion: Int) {
        check(status == Status.GJOERES_KLAR_FOR_SENDING) {
            """Kun brev med status ${Status.GJOERES_KLAR_FOR_SENDING} kan endres. Dette brevet har status ${status.name}."""
        }
        if (forventetVersion != version) {
            throw OptimistiskLaasingFeiletException("Forventet version var $forventetVersion, mens faktisk version var $version.")
        }

        if (endringer.harNyVerdiForTittel) {
            tittel = endringer.nyVerdiForTittel
        }
        if (endringer.harNyVerdiForBroedtekst) {
            broedtekst = endringer.nyVerdiForBroedtekst
        }
        version++
    }

    fun send(forventetVersion: Int) {
        check(status == Status.GJOERES_KLAR_FOR_SENDING) {
            """Kun brev med status ${Status.GJOERES_KLAR_FOR_SENDING} kan endres. Dette brevet har status ${status.name}."""
        }
        if (forventetVersion != version) {
            throw OptimistiskLaasingFeiletException("Forventet version var $forventetVersion, mens faktisk version var $version.")
        }

        status = Status.SENDT
        datoSendt = LocalDateTime.now()
        version++
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Brev

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun eksporterState(): State {
        return State(foedselsnummer, id, version, status, tittel, broedtekst, datoSendt)
    }

    data class State(
        val foedselsnummer: String,
        val id: Long,
        val version: Int,
        val status: Status,
        val tittel: String?,
        val broedtekst: String?,
        val datoSendt: LocalDateTime?
    )

    class OptimistiskLaasingFeiletException(melding: String) : RuntimeException(melding)

}