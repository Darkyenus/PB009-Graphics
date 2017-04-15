(UTF-8)

PB009 Introduction to Computer Graphics - Homework 1
====================================================

Návod ke spuštění:
------------------
1. Je třeba Java Development Kit (http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
2. Je třeba SBT, build systém (http://www.scala-sbt.org/download.html)
3. V této složce provést příkaz `sbt packResources run`
4. Pro opětovné spuštění stačí `sbt run`

O programu:
-----------
Obsahuje několik interaktivních demonstrací z 2D grafiky. Každá demonstrace podporuje krokování
a většina má i několik variant. Špendlíky lze pohybovat pro úpravu scény, tažením scény se dá posouvat
a kolečkem myši se dá přibližovat a oddalovat. Spodní panel obsahuje výběr variant, tlačítka a posuvník
pro krokovací animace. Varianty se dají také měnit pomocí kláves W/S nebo šipkami nahoru/dolů, pro
rychlé změny a porovnávání algoritmů, a to i během animací.

Barvy obrazců jsou voleny tak, aby co lejlépe přibližovaly vnitřní stav a logiku využitého algoritmu.



Naprogramované v Kotlinu (https://kotlinlang.org), využívá libGDX (https://libgdx.badlogicgames.com).
Jan Polák (c) 2017