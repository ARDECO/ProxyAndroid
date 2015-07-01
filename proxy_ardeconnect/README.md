# Proxy Ardeco
Service Android pour Ardeco
######################
Utilisation :

    boolean enrollWithArdeco( IRemoteListener listener, String spEnrollUrl);
	
spécifier l'url du fournisseur de service, ainsi qu'une fonction de callback

    void handleSpCode (
        String spCode,
        String errorMessage,
        boolean user_cancel
        );

La fonction de callback doit définir cette méthode pour avoir le resultat de la requête