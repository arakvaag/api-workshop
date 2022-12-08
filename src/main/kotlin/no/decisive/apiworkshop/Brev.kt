package no.decisive.apiworkshop

import java.time.LocalDateTime

class Brev {

    constructor(idProvider: IdProvider, fodselsnummer: String, tittel: String?, brodtekst: String?) {
        this.id = idProvider.hentNyBrevid()
        this.version = 1
        this.status = Status.GJORES_KLAR_FOR_SENDING
        this.fodselsnummer = fodselsnummer
        this.tittel = tittel
        this.brodtekst = brodtekst
        this.datoSendt = null
    }

    constructor(state: State) {
        this.id = state.id
        this.version = state.version
        this.status = state.status
        this.fodselsnummer = state.fodselsnummer
        this.tittel = state.tittel
        this.brodtekst = state.brodtekst
        this.datoSendt = state.datoSendt
    }

    @Suppress("unused")
    enum class Status { GJORES_KLAR_FOR_SENDING, SENDT }

    val id: Long
    var version: Int
        private set
    var status: Status
        private set
    val fodselsnummer: String
    var tittel: String?
        private set
    var brodtekst: String?
        private set
    var datoSendt: LocalDateTime?
        private set

    fun endreData(endringer: BrevService.Brevendringer) {
        if (endringer.harNyVerdiForTittel) {
            tittel = endringer.nyVerdiForTittel
        }
        if (endringer.harNyVerdiForBrodtekst) {
            brodtekst = endringer.nyVerdiForBrodtekst
        }
        version++
    }

    fun send() {
        check(status == Status.GJORES_KLAR_FOR_SENDING) {
            "Egenandel med id $id har ikke status ${Status.GJORES_KLAR_FOR_SENDING.name} og kan derfor ikke sendes"
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
        return State(fodselsnummer, id, version, status, tittel, brodtekst, datoSendt)
    }
    data class State(
        val fodselsnummer: String,
        val id: Long,
        val version: Int,
        val status: Status,
        val tittel: String?,
        val brodtekst: String?,
        val datoSendt: LocalDateTime?
    )
}