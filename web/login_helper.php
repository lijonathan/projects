<?php

session_start();

$username1 = $_POST['username1'];

$h = fopen("users.txt" , "r");

while(!feof($h) ){
	$this_line = fgets($h);
	$this_name = trim($this_line);
	if ($this_name == $username1){
		$_SESSION['username'] = $username1;

		ob_start();
		ob_end_flush();
		
		header("refresh: 1; url = main.php");
		exit; 
	}
}		

header("refresh: 1; url = login.html");
exit;

?>
