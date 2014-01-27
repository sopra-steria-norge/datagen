Datagenerator for elhub
=======================

Avhengigheter:
 - java sdk 1.7
 - play framework: http://www.playframework.com/  version 2.2.1 (basert pa scala 2.10.2 og sbt 0.13.0)
 - koden er i scala (og java) https://github.com/steria/datagen
 - du trenger � sende json meldinger til datagen, et bra verkt�y er Postman (plugin til chrome). Husk � sette contentType headeren til "application/json"


Etter at du installert play s� starter du serveren med "play run" og den vil da vare tilgjengelig p� http://localhost:9000
 
 
Der er flere m�ter � bruke serveren p�, og hvis du trenger en annen m�te s� bare leg den ved! Det vanlige patternet er at man f�rst gj�r et kall til "init" for � configurere serveren, og etter det gj�r et nytt kall for � sende data. Alle init kall tar parametene:
 - councilFilter (String) kommaseparert liste p� kommunenummer, f.eks. "1151" gir lite data og "" gir all data (default: "")
 - startDate (String) dato for f�rste m�lingen, f.eks. 2013-01-01 (default: current time)
 - batchSize (Int) hvor mye data skal sendes til serveren i hver melding (default: 10000)
 - measurementFrequencyMin (Int) hvor ofte skal m�linger gj�res (dvs. hvor mye skal timestamp oppdateres for hver melding, i minutter) (default: 15)
 - parallel (Int) hvor mange meldinger tr�der skal fors�ke sende meldinger til serveren (default: 1)
 
 Elasticsearch
 =============
 * installere elasticsearch
 * oppdatere project/Build.scala med riktig elasticsearch versjon, merk at det m� vare samme p� datagen som serveren
 * send en POST til http://localhost:9000/elastic/init med parametere som {"councilFilter":"1151", "clusterName":"myCluster", "multicast":false, "hosts":"1.2.3.4"}. Parameterene tillegg til de som st�ttes av alle init er
  - clusterName (String) obligatorisk, navnet p� ditt elasticsearch cluster
  - multicast (String) skal multicast st�ttes (default: true)
  - hosts (String) obligatorisk dersom multicast er false. Kommasepararert liste p� ip addresser der der elasticsearch clusteret kj�rer
  - indexName (String) indekset som skal skrives til i elasticsearch (default: "mydb")
 * send en GET til http://localhost:9000/elastic/chunk. Dette vil f� serven til � skedulere arbeid for � laste in m�ledata fra de m�lepunkter som er satt i init for ett tidspunkt, og siden oppdatere tidspunktet slik at du kan gj�re et nytt kall og f� mer data  

Hvis du fors�ker kj�re elasticsearch p� AWS kan du ikke bruke multicast, og du trenger da dette i din elasticsearch.yaml:
```
discovery.zen.ping.multicast.enabled: false
discovery.zen.ping.unicast.hosts: ["172.31.31.127:9300"] # ip her serveren sin ip, og hvis du har flere noder m� du ha en komma separert liste her
```

MongoDB
=======
* installere mongodb
* start mongod med "mongod --replSet rep". Hvis du ikke endrer noe starter denne p� port 27017
* send en POST til http://localhost:9000/mongo/init med parametere som {"councilFilter":"1151", "uri":"mongodb://localhost:27017/?replicaSet=rep"}. Parameterene i tillegg til de som st�ttes av alle init er
  - uri (String) obligatorisk, en connectionstring til mongodb
  - database (String) databasen som skal brukes (default: "mydb")
  - collection (String) collection som skal skrives til i mongodb (default: "myCol")
* send en GET til http://localhost:9000/mongo/chunk. Dette vil f� serven til � skedulere arbeid for � laste in m�ledata fra de m�lepunkter som er satt i init for ett tidspunkt, og siden oppdatere tidspunktet slik at du kan gj�re et nytt kall og f� mer data  

Data som blir generert
======================
Der finns en konfigurasjonsfil application.conf med kommune og antal m�lestasjoner. I tillegg s� genererer den noen data patterns som du kan bruke for analyse:
* tid p� dagen
* m�ned i �ret, mer forbruk om vinteren og foretninger er lukket i Juli)
* ulike hushold typer (i ulike proportioner i store og sm� kommuner)
* sm� og store foretninger (i st�rre kommuner)
* koden er i scala/github, s� fritt frem og endre i filene MeasurementSource.scala og Generator.scala

TODO
====
* fjerne ubrukt (duplikat) kode i datagen.controllers
* st�tte hente data via wget (og elastic river etc)
* st�tte hente data i csv (via wget)
* st�tte andre teknologier (Cassandra?)
   