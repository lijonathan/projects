<?php

require 'database.php';

session_start();


$users_id = $_SESSION['userid'];
$storyid= $_POST['storyid'];
$storyauthor= $_POST['storyauthor'];



$stmt = $mysqli->prepare("select users_id from stories where id = ?");
if(!$stmt){
	printf("Query Prep Failed: %s\n", $mysqli->error);
	echo "fails";
    exit;
 
}

$stmt -> bind_param('i', $storyid);

$stmt->execute();

$stmt->bind_result($author_id);

$stmt->fetch();
$stmt->close();


if($users_id == $author_id){

$stmt= $mysqli->prepare("delete from stories WHERE id = ?");

$stmt->bind_param("i", $storyid);


if(!$stmt){
	
        printf("Query Prep Failed: %s\n", $mysqli->error);
    exit;
}


$stmt->execute();

$stmt->close();

header("Location: userHomePage.php");

}
else{
	
        echo "Can't delete a story that's not yours";
       header("Location: userHomePage.php");
}

?>