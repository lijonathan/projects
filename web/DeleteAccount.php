<?php
require 'database.php';

session_start();

$userid = $_SESSION['userid'];

$stmt= $mysqli->prepare("delete from comments WHERE users_id = ?");

$stmt->bind_param("i", $userid);


if(!$stmt){
        printf("Query Prep Failed: %s\n", $mysqli->error);
    exit;
}


$stmt->execute();

$stmt->close();

$stmt= $mysqli->prepare("delete from stories WHERE users_id = ?");

$stmt->bind_param("i", $userid);


if(!$stmt){
        printf("Query Prep Failed: %s\n", $mysqli->error);
    exit;
}


$stmt->execute();

$stmt->close();

$stmt= $mysqli->prepare("delete from users WHERE id = ?");

$stmt->bind_param("i", $userid);


if(!$stmt){
        printf("Query Prep Failed: %s\n", $mysqli->error);
    exit;
}


$stmt->execute();

$stmt->close();
header("Location: accountdeletesuccess.php");



?>