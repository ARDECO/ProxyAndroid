<?php
include_once 'includes/functions.php';
include_once 'includes/dbfront.php';
setlocale(LC_ALL, 'fr_FR');
$pdo=connect();

$session_path = session_save_path() . OP_PATH;
if(!file_exists($session_path))
    mkdir($session_path);
session_save_path($session_path);

$path_info = $_SERVER['PATH_INFO'];

// delete user function, then redirect to index.php
if( isset($_GET['delete']) ) {
	$id = $_GET['delete'];
	deleteUser($pdo, $id);
	header("Location: index.php");
	return;
}

?>

<!DOCTYPE html>
<html lang="fr">
  <head>
  </head>
  <body>
<?php
// add link to enroll users
echo '<a href="'.$SP_URL.'/ardeco.php">Enroll</a><br><br>';


// get all users from database
$users = getAllUsers($pdo);
$nbUser = count($users);

// display users
if($nbUser<1) {
	echo "No users enrolled";
} else {
	for( $i=0; $i<count($users); $i++ ) {
		$user = $users[$i];
		$id   = $user['id'];
		// add a link to each user to delete from database
		echo "User : ".$user['firstName']." ".$user['lastName'].' <a href="index.php?delete='.$id.'">delete</a><br>';
	}
}

?>
  </body>
</html>
