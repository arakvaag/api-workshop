Øvingen består av å implementere REST-endepunkter som støtter noen oppgitte scenarioer. 

Dere skal kun implementere en rest-controller, og evt hjelpeklasser/DTOer som trengs for den.

For å bruke tiden best mulig, skal dere ikke skrive tester i denne øvingen. Dere skal i stedet bruke Postman eller
Insomnia og teste manuelt. Hvis dere hadde gjort disse utviklingsoppgavene på det vanlige oppdraget deres, så burde dere
kjøre testdreven utvikling (TDD) og laget tester som kjørte REST-kall mot endepunktene.

Følgende liste er prioritert rekkefølge av scenarioer dere skal starte å implementere
- Opprette brev
- Hente brev på id
- Prøve å opprette et brev, men fødselsnummer mangler i request
- Endre på et felt i et brev
- Kjøre kommandoen "send" på et brev
- Prøve å endre et felt på et brev, men det oppgis en id som ikke finnes
- Prøve å kjøre kommandoen "send" på et brev som allerede er sendt
- Prøve å endre et felt i brev, men oppgir version ulik version på brevet i DB
- Søke etter brev på fødselsnummer. Her kan dere velge en av de to variantene under
  - Klassisk med query-params
  - Personvern-compliant med RPC

Scenarioene finnere dere igjen i BrevServiceTest, og disse testene er også fasit for hva som er korrekt 
funksjonalitet for de forskjellige scenarioene. Scenarioet "Prøve å opprette et brev, men fødselsnummer mangler i 
request" er unntaket, siden det ikke er mulig å implementere service-test for dette scenarioet (vil ikke kompilere).

NB! Dere skal ikke endre på noe av den eksisterende koden.