package no.decisive.apiworkshop

import no.decisive.apiworkshop.BrevService.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@Suppress("DANGEROUS_CHARACTERS", "NonAsciiCharacters")
class BrevServiceTest : SpringTestParent() {

    @Test
    fun `opprette brev`() {
        val fodselsnummer = "11223312345"
        val tittel = "tittelen"
        val brodtekst = "brødteksten"

        //ACT
        val brev = brevService.opprettBrev(
            OpprettBrevRequest(
                fodselsnummer = fodselsnummer,
                tittel = tittel,
                brodtekst = brodtekst
            )
        )

        //ASSERT
        assertEquals(fodselsnummer, brev.fodselsnummer)
        assertEquals(tittel, brev.tittel)
        assertEquals(brodtekst, brev.brodtekst)
        assertEquals(Brev.Status.GJORES_KLAR_FOR_SENDING, brev.status)
        assertNull(brev.datoSendt)
    }

    @Test
    fun `hente brev på id`() {
        val fodselsnummer = "11223312345"
        val tittel = "tittelen"
        val brodtekst = "brødteksten"
        val nyOpprettetBrev = brevService.opprettBrev(
            OpprettBrevRequest(
                fodselsnummer = fodselsnummer,
                tittel = tittel,
                brodtekst = brodtekst
            )
        )

        //ACT
        val hentetBrev = brevService.hentBrevPaaId(nyOpprettetBrev.id)

        //ASSERT
        assertEquals(nyOpprettetBrev.id, hentetBrev.id)
        assertEquals(nyOpprettetBrev.version, hentetBrev.version)
        assertEquals(nyOpprettetBrev.fodselsnummer, hentetBrev.fodselsnummer)
        assertEquals(nyOpprettetBrev.tittel, hentetBrev.tittel)
        assertEquals(nyOpprettetBrev.brodtekst, hentetBrev.brodtekst)
        assertEquals(nyOpprettetBrev.status, hentetBrev.status)
        assertEquals(nyOpprettetBrev.datoSendt, hentetBrev.datoSendt)
    }

    @Test
    fun `endre på et felt i et brev`() {
        val nyTittel = "den nye tittelen"

        //ARRANGE
        val nyopprettetBrev = brevService.opprettBrev(
            OpprettBrevRequest(
                fodselsnummer = "11223312345",
                tittel = "tittelen",
                brodtekst = "brødteksten"
            )
        )

        //ACT
        brevService.endreDataIBrev(
            brevid = nyopprettetBrev.id,
            brevendringer = Brevendringer().medNyTittel(nyTittel),
            forventetVersion = nyopprettetBrev.version
        )

        //ASSERT
        val oppdatertBrev = brevService.hentBrevPaaId(nyopprettetBrev.id)
        assertEquals(nyTittel, oppdatertBrev.tittel)
        assertTrue(oppdatertBrev.version > nyopprettetBrev.version)
    }

    @Test
    fun `kjøre kommandoen "send" på et brev`() {
        //ARRANGE
        val nyopprettetBrev = brevService.opprettBrev(
            OpprettBrevRequest(
                fodselsnummer = "11223312345",
                tittel = "tittelen",
                brodtekst = "brødteksten"
            )
        )

        //ACT
        brevService.sendBrev(
            brevid = nyopprettetBrev.id,
            forventetVersion = nyopprettetBrev.version
        )

        //ASSERT
        val sendtBrev = brevService.hentBrevPaaId(nyopprettetBrev.id)
        assertEquals(Brev.Status.SENDT, sendtBrev.status)
        assertNotNull(sendtBrev.datoSendt)
        assertTrue(sendtBrev.version > nyopprettetBrev.version)
    }

    @Test
    fun `prøve å endre et felt på et brev, men det oppgis en id som ikke finnes`() {
        val nyopprettetBrev = brevService.opprettBrev(
            OpprettBrevRequest(
                fodselsnummer = "11223312345",
                tittel = "tittelen",
                brodtekst = "brødteksten"
            )
        )

        assertThrows(BrevRepository.EntitetFinnesIkkeException::class.java) {
            brevService.endreDataIBrev(
                brevid = nyopprettetBrev.id + 1000, //Legger bevisst inn feil id
                brevendringer = Brevendringer().medNyTittel("den nye tittelen"),
                forventetVersion = nyopprettetBrev.version
            )
        }
    }

    @Test
    fun `prøve å kjøre kommandoen "send" på et brev som allerede er sendt`() {
        val nyopprettetBrev = brevService.opprettBrev(
            OpprettBrevRequest(
                fodselsnummer = "11223312345",
                tittel = "tittelen",
                brodtekst = "brødteksten"
            )
        )
        val brevid = nyopprettetBrev.id

        brevService.sendBrev(brevid = brevid, forventetVersion = nyopprettetBrev.version)
        val alleredeSendtBrev = brevService.hentBrevPaaId(brevid)

        //Prøver å sende brevet på nytt
        assertThrows(IllegalStateException::class.java) {
            brevService.sendBrev(brevid = brevid, forventetVersion = alleredeSendtBrev.version)
        }
    }

    @Test
    fun `prøve å endre et felt i brev, men oppgir version ulik version på brevet i DB`() {
        val nyopprettetBrev = brevService.opprettBrev(
            OpprettBrevRequest(
                fodselsnummer = "11223312345",
                tittel = "tittelen",
                brodtekst = "brødteksten"
            )
        )

        assertThrows(OptimistiskLaasingFeiletException::class.java) {
            brevService.endreDataIBrev(
                brevid = nyopprettetBrev.id,
                brevendringer = Brevendringer().medNyTittel("den nye tittelen"),
                forventetVersion = nyopprettetBrev.version + 1000 //Legger bevisst inn feil version
            )
        }
    }

    @Test
    fun `søke etter brev på fødselsnummer`() {
        val fodselsnummer = "11223312345"
        val tittelBrev2 = "tittelen 2"
        val tittelBrev3 = "tittelen 3"

        //ARRANGE
        brevService.opprettBrev(
            OpprettBrevRequest(fodselsnummer = "22334412345", tittel = "tittelen 1", brodtekst = "brødteksten")
        )
        brevService.opprettBrev(
            OpprettBrevRequest(fodselsnummer = fodselsnummer, tittel = tittelBrev2, brodtekst = "brødteksten")
        )
        brevService.opprettBrev(
            OpprettBrevRequest(fodselsnummer = fodselsnummer, tittel = tittelBrev3, brodtekst = "brødteksten")
        )
        brevService.opprettBrev(
            OpprettBrevRequest(fodselsnummer = "33445512345", tittel = "tittelen 4", brodtekst = "brødteksten")
        )

        //ACT
        val soeketreff = brevService.soekEtterBrevPaaFodselsnummer(
            fodselsnummer = fodselsnummer
        )

        //ASSERT
        assertEquals(2, soeketreff.size)
        assertNotNull(soeketreff.firstOrNull { it.tittel == tittelBrev2 })
        assertNotNull(soeketreff.firstOrNull { it.tittel == tittelBrev3 })
    }

}