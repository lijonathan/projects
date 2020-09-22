<!DOCTYPE html>
<html>
    <head></head>
    <body>
      
	<form action= 'AddComment.php' method = 'POST'>
	<input type = 'hidden' name = 'users_id' value = '".$users_id."'/>
	<input type="hidden" name="token" value="<?php echo $_SESSION['token'];?>" />
	<input type = 'submit' value = 'Add Comment'/>
</form>

<?php
if($_SESSION['token'] !== $_POST['token']){
	die("Request forgery detected");
}
else{
require 'database.php';

session_start();



echo "<br>";

$id= $_POST['storyid'];

$users_id = $_SESSION['userid'];
$_SESSION['storyid'] = $id;

//echo $users_id;
echo "<br/>";
$stmt = $mysqli->prepare("select stories, title from stories where id = ?");
if(!$stmt){
	printf("Query Prep Failed: %s\n", $mysqli->error);
	echo "fails";
    exit;
    
}

$stmt -> bind_param('s', $id);

$stmt->execute();

$stmt->bind_result($stories, $title);

echo $stories;
echo $title;
$stmt->fetch();


echo "Story Title:".$title."\n";
echo "<br/>";
echo "Story:".$stories;

$stmt->close();
//////////////////////////////////////////////////////////////////
$stmt = $mysqli -> prepare("select comment, stories_id, id, users_id from comments where stories_id= ?");

if(!$stmt){
	
	printf("Query Prep Failed: %s\n", $mysqli->error);
	
	exit;
}
 
$stmt -> bind_param('s', $id);

$stmt->execute();

$stmt->bind_result($comment, $id, $commentid, $comment_author_id);





while($stmt->fetch()){
	echo "<p name = 'comments' value ='".$commentid."'>".$comment."</p>"; 

	echo "<form action= 'DeleteComment.php' method = 'POST'>";
    echo " <input type = 'submit' name = 'Delete Comment' value = 'Delete Selected Comment'/>";
    echo  "<input type = 'hidden' name = 'comments' value = '".$commentid."'/>";
	echo "<input type = 'hidden' name = 'comment_author_id' value = '".$comment_author_id."'/>";
	echo "<input type='hidden' name='token' value='".$_SESSION['token']."' />";
    echo  "</form>";
	
	echo "<form action= 'CommentEdit.php' method = 'POST'>";
    echo " <input type = 'submit' name = 'Edit Comment' value = 'Edit Selected Comment'/>";
    echo  "<input type = 'hidden' name = 'comments' value = '".$commentid."'/>";
	echo "<input type = 'hidden' name = 'comment_author_id' value = '".$comment_author_id."'/>";
	echo "<input type='hidden' name='token' value='".$_SESSION['token']."' />";
	echo "<input type = 'hidden' name = 'hidden' value = 'edit comment'>";
    echo  "</form>";
}




echo "</ul>\n";

$stmt->close();
}
?>
     
    
   </body> 
</html>   
