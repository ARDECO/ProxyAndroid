# Proxy Ardeco
Service Android pour Ardeco
######################
Utilisation :

    boolean enrollWithArdeco( IRemoteListener listener, String spEnrollUrl);
	
sp�cifier l'url du fournisseur de service, ainsi qu'une fonction de callback

    void handleSpCode (
        String spCode,
        String errorMessage,
        boolean user_cancel
        );

La fonction de callback doit d�finir cette m�thode pour avoir le resultat de la requ�te