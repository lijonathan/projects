<?php
require 'database.php';

session_start();
if($_SESSION['token'] !== $_POST['token']){
	die("Request forgery detected");
}

//$storyid= $_POST['id'];
//echo $storyid;
//echo $users_id;
$users_id = $_SESSION['userid'];
$commentid = $_POST['comments'];
$comment_author_id = $_POST['comment_author_id'];
$storyid= $_SESSION['storyid'];
$comment= $_POST['editcomment'];
echo $comment;
//$id = $_SESSION['commentid'];
////////////////////need to access comment id
if($users_id == $comment_author_id){
$stmt= $mysqli->prepare("delete from comments WHERE id = ?");

$stmt->bind_param("i", $commentid);


if(!$stmt){
        printf("Query Prep Failed: %s\n", $mysqli->error);
    exit;
}

//$stmt->bind_param ('sss', $comment, $storyid, $users_id);

$stmt->execute();

$stmt->close();

$stmt= $mysqli->prepare("insert into comments (comment, stories_id, users_id) values (?, ?, ?)");

if(!$stmt){
        printf("Query Prep Failed: %s\n", $mysqli->error);
        exit;
}

$stmt->bind_param ('sss', $comment, $storyid, $users_id);

$stmt->execute();

$stmt->close();


header("Location: userHomePage.php");

}
else{
        echo "Can't delete comment that's not yours";
        header("Location: userHomePage.php");
}


?>