Application affichage des resultats Kayak sur tableau numerique

Installation
==================================

 + monter un disque partagé en ecriture sur le PC pilote du tableau (u:\)
 + sur le pc course monter ce disque et lancer le script ffcanoe/synchro/synchro.bat u:\xxx\tableau
 + sur le pc pilote du tableau lancer l'interface : java -jar bretagne.jar -djava.lib.path=xxx\tableau\src\lib
 + lancer un navigateur sur l'adresse du pc pilote:8181
 + bon courage
 
TODO :
======

 + faire une simul complete avec du volume (senior) et 2 juges
 + prevoir le necessaire pour une install from scratch du pilote (jdk, ffcanoe, + bretagne)
 + prévoir un environnement de dev/recette
 + Preparer le deploiement automatique en course
 + prevoir un mode deconnecté pour les essais de message
 + si pb de perf sur l'import des donnees par manche, ajouter le critere du dossard et passer en semi manuel (quoi que)