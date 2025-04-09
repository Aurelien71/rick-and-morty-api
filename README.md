# Rick and Morty API

## Architecture du Projet

Le projet repose sur une clean architecture afin de faciliter l’évolution et la maintenance du code.

### Structure des Dossiers

- **/commonMain**  
  Contient le code source principal.  
  - **/data** : Couche gérant les intéractions avec les données (local/API).
  - **/domain** : Couche contenant les objets métiers avec les interfaces associés.
  - **/shared** : Contient les **Manager** permettant gestion de platforme (OS).
  - **/ui** : Couche contenant toutes les interfaces utilisateurs.
 
- **/androidMain**  
  Contient le code spécifique à android.  
  - **/data** : Couche gérant les intéractions avec les données (local/API).
  - **/shared** : Contient les **Manager** permettant gestion du code spécifique à Android.
  - **/ui** :  Couche contenant toutes les interfaces utilisateurs (Android).
 
- **/desktopMain**  
  Contient le code spécifique au Desktop.  
  - **/data** : Couche gérant les intéractions avec les données (local/API).
  - **/shared** : Contient les **Manager** permettant gestion du code spécifique au Desktop.
