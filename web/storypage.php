<?php
require "database.php";

session_start();


if($_POST['hidden'] == "submit story"){

$title = $_POST['title'];
$stories = $_POST['story'];
$users_id = $_SESSION['userid'];

//$users_id = $_SESSION['userid'];


$stmt= $mysqli->prepare("insert into stories (stories, title, users_id) values (?, ?, ?)");

if(!$stmt){
        printf("Query Prep Failed: %s\n", $mysqli->error);
        exit;
}

$stmt->bind_param ('sss', $stories, $title, $users_id);

$stmt->execute();

$stmt->close();


header("Location: userHomePage.php");

}

?>
