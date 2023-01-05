package no.decisive.apiworkshop

import no.decisive.apiworkshop.BrevService.Brevendringer
import no.decisive.apiworkshop.BrevService.OpprettBrevRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BrevServiceTest : SpringTestParent() {

    @Test
    fun `opprette brev`() {
        val fodselsnummer = "11223312345"
        val tittel = "tittelen"
        val brodtekst = "brødteksten"

        //ACT
        val brev = brevService.opprettBrev(
            OpprettBrevRequest(
                foedselsnummer = fodselsnummer,
                tittel = tittel,
                broedtekst = brodtekst
            )
        )

        //ASSERT
        assertEquals(fodselsnummer, brev.foedselsnummer)
        assertEquals(tittel, brev.tittel)
        assertEquals(brodtekst, brev.broedtekst)
        assertEquals(Brev.Status.GJOERES_KLAR_FOR_SENDING, brev.status)
        assertNull(brev.datoSendt)
    }

    @Test
    fun `hente brev paa id`() {
        val fodselsnummer = "11223312345"
        val tittel = "tittelen"
        val brodtekst = "brødteksten"
        val nyOpprettetBrev = brevService.opprettBrev(
            OpprettBrevRequest(
                foedselsnummer = fodselsnummer,
                tittel = tittel,
                broedtekst = brodtekst
            )
        )

        //ACT
        val hentetBrev = brevService.hentBrevPaaId(nyOpprettetBrev.id)

        //ASSERT
        assertEquals(nyOpprettetBrev.id, hentetBrev.id)
        assertEquals(nyOpprettetBrev.version, hentetBrev.version)
        assertEquals(nyOpprettetBrev.foedselsnummer, hentetBrev.foedselsnummer)
        assertEquals(nyOpprettetBrev.tittel, hentetBrev.tittel)
        assertEquals(nyOpprettetBrev.broedtekst, hentetBrev.broedtekst)
        assertEquals(nyOpprettetBrev.status, hentetBrev.status)
        assertEquals(nyOpprettetBrev.datoSendt, hentetBrev.datoSendt)
    }

    @Test
    fun `endre tittel paa et brev`() {
        val nyTittel = "den nye tittelen"

        //ARRANGE
        val nyopprettetBrev = brevService.opprettBrev(
            OpprettBrevRequest(
                foedselsnummer = "11223312345",
                tittel = "tittelen",
                broedtekst = "brødteksten"
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
    fun `endre broedtekst paa et brev`() {
        val nyBroedtekst = "den nye brødteksten"

        //ARRANGE
        val nyopprettetBrev = brevService.opprettBrev(
            OpprettBrevRequest(
                foedselsnummer = "11223312345",
                tittel = "tittelen",
                broedtekst = "brødteksten"
            )
        )

        //ACT
        brevService.endreDataIBrev(
            brevid = nyopprettetBrev.id,
            brevendringer = Brevendringer().medNyBroedtekst(nyBroedtekst),
            forventetVersion = nyopprettetBrev.version
        )

        //ASSERT
        val oppdatertBrev = brevService.hentBrevPaaId(nyopprettetBrev.id)
        assertEquals(nyBroedtekst, oppdatertBrev.broedtekst)
        assertTrue(oppdatertBrev.version > nyopprettetBrev.version)
    }

    @Test
    fun `kjoere kommandoen send paa et brev`() {
        //ARRANGE
        val nyopprettetBrev = brevService.opprettBrev(
            OpprettBrevRequest(
                foedselsnummer = "11223312345",
                tittel = "tittelen",
                broedtekst = "brødteksten"
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
    fun `proeve aa endre et felt paa et brev, men det oppgis en id som ikke finnes`() {
        val nyopprettetBrev = brevService.opprettBrev(
            OpprettBrevRequest(
                foedselsnummer = "11223312345",
                tittel = "tittelen",
                broedtekst = "brødteksten"
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
    fun `proeve aa kjoere kommandoen send paa et brev som allerede er sendt`() {
        val nyopprettetBrev = brevService.opprettBrev(
            OpprettBrevRequest(
                foedselsnummer = "11223312345",
                tittel = "tittelen",
                broedtekst = "brødteksten"
            )
        )
        val brevid = nyopprettetBrev.id

        brevService.sendBrev(brevid = brevid, forventetVersion = nyopprettetBrev.version)
        val alleredeSendtBrev = brevService.hentBrevPaaId(brevid)

        //Prøver aa sende brevet paa nytt
        assertThrows(IllegalStateException::class.java) {
            brevService.sendBrev(brevid = brevid, forventetVersion = alleredeSendtBrev.version)
        }
    }

    @Test
    fun `proeve aa endre et felt i brev, men oppgir version ulik version paa brevet i DB`() {
        val nyopprettetBrev = brevService.opprettBrev(
            OpprettBrevRequest(
                foedselsnummer = "11223312345",
                tittel = "tittelen",
                broedtekst = "brødteksten"
            )
        )

        assertThrows(Brev.OptimistiskLaasingFeiletException::class.java) {
            brevService.endreDataIBrev(
                brevid = nyopprettetBrev.id,
                brevendringer = Brevendringer().medNyTittel("den nye tittelen"),
                forventetVersion = nyopprettetBrev.version + 1000 //Legger bevisst inn feil version
            )
        }
    }

    @Test
    fun `proeve aa kjoere endre-kall uten aa oppgi noen endringer`() {
        val nyopprettetBrev = brevService.opprettBrev(
            OpprettBrevRequest(
                foedselsnummer = "11223312345",
                tittel = "tittelen",
                broedtekst = "brødteksten"
            )
        )

        assertThrows(IllegalArgumentException::class.java) {
            brevService.endreDataIBrev(
                brevid = nyopprettetBrev.id,
                brevendringer = Brevendringer(),
                forventetVersion = nyopprettetBrev.version
            )
        }
    }

    @Test
    fun `soeke etter brev paa foedselsnummer`() {
        val foedselsnummer = "11223312345"
        val tittelBrev2 = "tittelen 2"
        val tittelBrev3 = "tittelen 3"

        //ARRANGE
        brevService.opprettBrev(
            OpprettBrevRequest(foedselsnummer = "22334412345", tittel = "tittelen 1", broedtekst = "brødteksten")
        )
        brevService.opprettBrev(
            OpprettBrevRequest(foedselsnummer = foedselsnummer, tittel = tittelBrev2, broedtekst = "brødteksten")
        )
        brevService.opprettBrev(
            OpprettBrevRequest(foedselsnummer = foedselsnummer, tittel = tittelBrev3, broedtekst = "brødteksten")
        )
        brevService.opprettBrev(
            OpprettBrevRequest(foedselsnummer = "33445512345", tittel = "tittelen 4", broedtekst = "brødteksten")
        )

        //ACT
        val soeketreff = brevService.soekEtterBrevPaaFodselsnummer(
            foedselsnummer = foedselsnummer
        )

        //ASSERT
        assertEquals(2, soeketreff.size)
        assertNotNull(soeketreff.firstOrNull { it.tittel == tittelBrev2 })
        assertNotNull(soeketreff.firstOrNull { it.tittel == tittelBrev3 })
    }

}