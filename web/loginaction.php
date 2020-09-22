<?php

require "database.php";    

session_start();

$username = $_POST['username'];
$password = $_POST['password'];
$hashedPassword = md5($password);


$username = filter_var($username);
$password = filter_var($password);

if($_POST['hidden']=="register"){

$stmt = $mysqli->prepare("insert into users (username, password) values (?, ?)");

if(!$stmt){
	printf("Failed to register", $mysqli->error);
	header("Location: login.php");
	exit;
}

	
	$stmt->bind_param('ss', $username, $hashedPassword);
	$stmt->execute();
	
/*
$message = "Hey, Welcome to our news website!\r\nYou have successfully made a new account.\r\nThanks for joining!";

$message = wordwrap($message, 70, "\r\n");

$mailaddress= $_POST['Email Address'];
mail($mailaddress, 'J&R News Website', $message);


	*/
	$_SESSION['token'] = substr(md5(rand()), 0, 10);
	$_SESSION['userid'] = $userid;   
	header("Location: userHomePage.php");
	
$stmt->close();
}

if($_POST['hidden'] == "login"){
 
$stmt = $mysqli->prepare("select username, password, id from users WHERE username = ?");
if(!$stmt){
	printf("Query Prep Failed: %s\n", $mysqli->error);
	exit;
}
$stmt -> bind_param('s', $username); 
//remove the dollar sign in front of the 's'
//print as much as possible
//copy and paste mysql commans into mysql
$stmt->execute();
 
$stmt->bind_result($username, $hashedPassword, $userid);
 //session_start();


 //$_SESSION['username'] = $username;
//echo "Before fetch";
if($stmt->fetch()){
	$_SESSION['userid'] = $userid;
	$_SESSION['username'] = $username;
	$_SESSION['token'] = substr(md5(rand()), 0, 10);
	header("Location: userHomePage.php");
	


 
}
else{
   echo "Wrong username or password";
   header("Location: login.php");
}
$stmt->close();
}


 
?>
