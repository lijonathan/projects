<?php
require 'database.php';

session_start();



$storyid = $_SESSION['storyid'];
$users_id = $_SESSION['userid'];
echo $_SESSION['userid'];


if($_POST['hidden'] == "submit comment"){
$comment = $_POST['comment'];
////////////////////////////////////////////////////////////need to access comment id
$stmt= $mysqli->prepare("insert into comments (comment, stories_id, users_id) values (?, ?, ?)");

if(!$stmt){
        printf("Query Prep Failed: %s\n", $mysqli->error);
        exit;
}

$stmt->bind_param ('sss', $comment, $storyid, $users_id);

$stmt->execute();

$stmt->close();

$stmt = $mysqli -> prepare("select id from comments where comment = ?");
if(!$stmt){
        printf("Query Prep Failed: %s\n", $mysqli->error);
        exit;
}

$stmt->bind_param('s', $comment);
$stmt->execute();

$stmt -> bind_result($id);
$_SESSION['commentid'] = $id;

$stmt -> close();
header("Location: userHomePage.php");

}

?>