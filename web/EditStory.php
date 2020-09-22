
<!DOCTYPE html>
<html>
    <body>      
      
<?php
	require 'database.php';
	session_start();
	if($_SESSION['token'] !== $_POST['token']){
	die("Request forgery detected");
}
   
	$storyid = $_POST['storyid'];
	$story_author = $_POST['storyauthorid'];
    $users_id = $_SESSION['userid'];
    $title = $_POST['editedtitle'];
    $story = $_POST['editstory'];


if($users_id == $story_author){

 $stmt= $mysqli->prepare("delete from stories WHERE id = ?");

$stmt->bind_param("i", $storyid);

if(!$stmt){
        printf("Query Prep Failed: %s\n", $mysqli->error);
    exit;
}

$stmt->execute();

$stmt->close();

$stmt= $mysqli->prepare("insert into stories (title, stories, users_id) values (?, ?, ?)");

if(!$stmt){
        printf("Query Prep Failed: %s\n", $mysqli->error);
        exit;
}

$stmt->bind_param ('sss', $title, $story, $users_id);

$stmt->execute();

$stmt->close();


header("Location: userHomePage.php");

}
else{
        echo "Can't delete story. That's not yours";
        header("Location: userHomePage.php");
}


$stmt ->close();

?>
    
    </body>
</html>

