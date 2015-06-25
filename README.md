# ProxyAndroid
ARDECO Android Service
######################
Module lib
######################
Release 1.0.0
---------------------------------
HCE card Emulation / Rf interface

Application AID : a00000054541726465636F

@see com.dejamobile.ardeco.hce.ContactlessEntryPoint

Séquence APDUs type :

>>> 3B 88 80 01 00 00 00 00 00 81 71 00 F9
# select applet, pas de df ni d'ef
-> 00 a4 04 00 0b a0 00 00 05 45 41 72 64 65 63 6f
<- 6f 06 83 02 3f 00 85 00  90 00
-> 00 a4 00 00 02 3f 00
<- 6f 06 83 02 3f 00 85 00  90 00
# creation fichier 0002
-> 00 e0 00 01 0f 00 1d 00 02 02 00 00 00 00 01 00 00 00 00 1d
<-   90 00
# selection EF 0002
-> 00 a4 00 00 02 00 02
<- 6f 04 83 02 00 02  90 00
-> 00 dc 00 00 1d 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28
<-   90 00
# selection 3F00 -> contient un EF 0002
-> 00 a4 00 00 02 3f 00
<- 6f 0a 83 02 3f 00 85 04 83 02 00 02  90 00
# creation 3f1c
-> 00 e0 00 01 0f 01 9a 3f 1c 01 00 00 00 00 01 00 00 00 00 00
<-   90 00
-> 00 a4 00 00 02 3f 00
<- 6f 0e 83 02 3f 00 85 08 83 02 00 02 83 02 3f 1c  90 00
# creation 3f 05
-> 00 e0 00 01 0f 10 00 3f 05 01 00 00 00 00 01 00 00 00 00 00
<-   90 00
-> 00 a4 00 00 02 3f 00
<- 6f 12 83 02 3f 00 85 0c 83 02 00 02 83 02 3f 1c 83 02 3f 05  90 00
# creation 3100
-> 00 e0 00 00 0f 00 00 31 00 08 00 00 00 00 01 00 00 00 00 00
<-   90 00
-> 00 a4 00 00 02 3f 00
<- 6f 16 83 02 3f 00 85 10 83 02 00 02 83 02 3f 1c 83 02 3f 05 83 02 31 00  90 00
# creation 3200
-> 00 e0 00 00 0f 00 00 32 00 08 00 00 00 00 01 00 00 00 00 00
<-   90 00
-> 00 a4 00 00 02 3f 00
<- 6f 1a 83 02 3f 00 85 14 83 02 00 02 83 02 3f 1c 83 02 3f 05 83 02 31 00 83 02 32 00  90 00
# creation 3300
-> 00 e0 00 00 0f 00 00 33 00 08 00 00 00 00 01 00 00 00 00 00
<-   90 00
# selection 3F00  -> contient 0002, 3f1c, 3f05, 3100, 3200, 3300
-> 00 a4 00 00 02 3f 00
<- 6f 1e 83 02 3f 00 85 18 83 02 00 02 83 02 3f 1c 83 02 3f 05 83 02 31 00 83 02 32 00 83 02 33 00  90 00
-> 00 a4 00 00 02 00 02
<- 6f 04 83 02 00 02  90 00
-> 00 b2 01 04 1d
<- 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28  90 00


------------------------------------------------------
HCE Service / Secure Element simulé / AIDL interface

@see com.dejamobile.ardeco.lib.ServiceEntryPoint
@see IServiceEntryPoint.aidl
------------------------------------------------------
Pour des exemples d'invocation des points d'entrées de l'interface se référer à MainActivity.java
dans le module app



