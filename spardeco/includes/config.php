<?php

date_default_timezone_set("Europe/Paris");

/**
 * These are the database login details
 */
define("HOST", "localhost");     // The host you want to connect to.
define("USER", "spardeco");    // The database username.
define("PASSWORD", "user_password");    // The database password.
define("DATABASE", "spardeco");    // The database name.

define("CAN_REGISTER", "any");
define("DEFAULT_ROLE", "member");

$SP_URL="sp_server_url";
$IDP_URL="idp_server_url";

$REDIRECT_URI=$SP_URL."/ardecoback.php";

$CLIENT_ID="spardeco";
$CLIENT_SECRET="spsecret";

$RESPONSE_TYPE="code";
$SCOPE="identity";

?>
