<?php
// Content of database.php
 
$mysqli = new mysqli('localhost', 'root', 'Wustl@2017', 'module3');
 
if($mysqli->connect_errno) {
	printf("Connection Failed: %s\n", $mysqli->connect_error);
	exit;
}
?>