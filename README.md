Datagenerator for elhub
=======================

Avhengigheter:
 - java sdk 1.7
 - play framework: http://www.playframework.com/  version 2.2.1 (basert pa scala 2.10.2 og sbt 0.13.0)
 - koden er i scala (og java) https://github.com/steria/datagen
 - du trenger å sende json meldinger til datagen, et bra verktøy er Postman (plugin til chrome). Husk å sette contentType headeren til "application/json"


Etter at du installert play så starter du serveren med "play run" og den vil da vare tilgjengelig på http://localhost:9000
 
 
Der er flere måter å bruke serveren på, og hvis du trenger en annen måte så bare leg den ved! Det vanlige patternet er at man først gjør et kall til "init" for å configurere serveren, og etter det gjør et nytt kall for å sende data. Alle init kall tar parametene:
 - councilFilter (String) kommaseparert liste på kommunenummer, f.eks. "1151" gir lite data og "" gir all data (default: "")
 - startDate (String) dato for første målingen, f.eks. 2013-01-01 (default: current time)
 - batchSize (Int) hvor mye data skal sendes til serveren i hver melding (default: 10000)
 - measurementFrequencyMin (Int) hvor ofte skal målinger gjøres (dvs. hvor mye skal timestamp oppdateres for hver melding, i minutter) (default: 15)
 - parallel (Int) hvor mange meldinger tråder skal forsøke sende meldinger til serveren (default: 1)
 
 Elasticsearch
 =============
 * installere elasticsearch
 * oppdatere project/Build.scala med riktig elasticsearch versjon, merk at det må vare samme på datagen som serveren
 * send en POST til http://localhost:9000/elastic/init med parametere som {"councilFilter":"1151", "clusterName":"myCluster", "multicast":false, "hosts":"1.2.3.4"}. Parameterene tillegg til de som støttes av alle init er
  - clusterName (String) obligatorisk, navnet på ditt elasticsearch cluster
  - multicast (String) skal multicast støttes (default: true)
  - hosts (String) obligatorisk dersom multicast er false. Kommasepararert liste på ip addresser der der elasticsearch clusteret kjører
  - indexName (String) indekset som skal skrives til i elasticsearch (default: "mydb")
 * send en GET til http://localhost:9000/elastic/chunk. Dette vil få serven til å skedulere arbeid for å laste in måledata fra de målepunkter som er satt i init for ett tidspunkt, og siden oppdatere tidspunktet slik at du kan gjøre et nytt kall og få mer data  

Hvis du forsøker kjøre elasticsearch på AWS kan du ikke bruke multicast, og du trenger da dette i din elasticsearch.yaml:
```
discovery.zen.ping.multicast.enabled: false
discovery.zen.ping.unicast.hosts: ["172.31.31.127:9300"] # ip her serveren sin ip, og hvis du har flere noder må du ha en komma separert liste her
```

MongoDB
=======
* installere mongodb
* start mongod med "mongod --replSet rep". Hvis du ikke endrer noe starter denne på port 27017
* send en POST til http://localhost:9000/mongo/init med parametere som {"councilFilter":"1151", "uri":"mongodb://localhost:27017/?replicaSet=rep"}. Parameterene i tillegg til de som støttes av alle init er
  - uri (String) obligatorisk, en connectionstring til mongodb
  - database (String) databasen som skal brukes (default: "mydb")
  - collection (String) collection som skal skrives til i mongodb (default: "myCol")
* send en GET til http://localhost:9000/mongo/chunk. Dette vil få serven til å skedulere arbeid for å laste in måledata fra de målepunkter som er satt i init for ett tidspunkt, og siden oppdatere tidspunktet slik at du kan gjøre et nytt kall og få mer data  

Data som blir generert
======================
Der finns en konfigurasjonsfil application.conf med kommune og antal målestasjoner. I tillegg så genererer den noen data patterns som du kan bruke for analyse:
* tid på dagen
* måned i året, mer forbruk om vinteren og foretninger er lukket i Juli)
* ulike hushold typer (i ulike proportioner i store og små kommuner)
* små og store foretninger (i større kommuner)
* koden er i scala/github, så fritt frem og endre i filene MeasurementSource.scala og Generator.scala

TODO
====
* fjerne ubrukt (duplikat) kode i datagen.controllers
* støtte hente data via wget (og elastic river etc)
* støtte hente data i csv (via wget)
* støtte andre teknologier (Cassandra?)
   