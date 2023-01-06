# martinBMW
The BMW parts finder

## What does Martin do?
Martin is part search aggregator and part search engine. It takes in a list of BMW part numbers, then looks up those parts in BMW's catalogue. There it'll find which makes and model were eqipped with that part. Next, it'll look at BMW cars at nearby parts yards (Pick n Pull) and give you a list of which cars at the yard will have which parts.

## Technologies
* Java
* Custom comparators
* Change tracking + monitoring
* REST API (martinServer)
* HTML parsing + scraping
* SMTP Mailing
* GitHub Releases + VCS
* GitHub Secrets

## martinServer
There exists an add-on project to this one called [martinServer](https://github.com/rudydelorenzo/martinServer), which contains a Java Tomcat backend to host martin as a fully fledged webapp. 
