(UTF-8)

PB009 Introduction to Computer Graphics - Homework 2
====================================================

Návod ke spuštění:
------------------
1. Je třeba Java Development Kit (http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
2. Je třeba SBT, build systém (http://www.scala-sbt.org/download.html)
3. V této složce provést příkaz `sbt packResources run`
4. Pro opětovné spuštění stačí `sbt run`

O programu:
-----------
Vykresluje 3D objekty z voxelů. V dolním panelu je možné zvolit úroveň sub/super-samplingu (sub- pro pomalejší počítače,
super pro lépe vypadající obraz), úroveň shadingu (jednoduché, Phongovo a Phongovo se stíny) a 3D útvar.
Vykresleným útvarem je možné otáčet myší a přibližovat oddalovat kolečkem myši.

Útvar je vykreslovaný přes ray-trace shader, který pro každý pixel vytvoří paprsek od pozorovatele do scény
a krokuje ho pomocí modifikovaného DDA algoritmu. Když paprsek narazí na další voxel mřížky, podívá se, jestli
je zaplněný a pokud ano, ukončí krokování a bodu přiřadí barvu.
(2D verze tohoto DDA je v 2D sekci programu, jako varianta vykreslování čar)

Stíny jsou generovány také pomocí vrhání paprsku, tentokrát ale od zdroje bodového světla.


Dále program obsahuje algoritmy 2D vykreslování z předchozího úkolu:

Obsahuje několik interaktivních demonstrací z 2D grafiky. Každá demonstrace podporuje krokování
a většina má i několik variant. Špendlíky lze pohybovat pro úpravu scény, tažením scény se dá posouvat
a kolečkem myši se dá přibližovat a oddalovat. Spodní panel obsahuje výběr variant, tlačítka a posuvník
pro krokovací animace. Varianty se dají také měnit pomocí kláves W/S nebo šipkami nahoru/dolů, pro
rychlé změny a porovnávání algoritmů, a to i během animací.

Barvy obrazců jsou voleny tak, aby co lejlépe přibližovaly vnitřní stav a logiku využitého algoritmu.



Naprogramováno v jazyku Kotlin (https://kotlinlang.org), využívá libGDX (https://libgdx.badlogicgames.com).
Některé části převzaté či inspirované z https://www.shadertoy.com/view/Xds3zN pod MIT License (voxel_fragment.glsl).
Jan Polák (c) 2017