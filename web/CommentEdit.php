
<!DOCTYPE html>
<html>
    <body>      
      

<?php

	require 'database.php';
	session_start();
	if($_SESSION['token'] !== $_POST['token']){
	die("Request forgery detected");
}
	$commentid = $_POST['comments'];
	$comment_author_id = $_POST['comment_author_id'];
	
if($_POST['hidden'] == "edit comment"){
	//$commentid = $_POST['commentid'];
	$stmt = $mysqli -> prepare("select comment from comments where id= ?");
	
if(!$stmt){
	
	printf("Query Prep Failed: %s\n", $mysqli->error);
	
	exit;
}
	
 
$stmt -> bind_param('s', $commentid);

$stmt->execute();

$stmt->bind_result($comment);
while($stmt-> fetch()){
	echo "Your Previous Comment: ".$comment;
}
$stmt ->close();
	

}
?>

<form action="EditComment.php" method= "POST">
        Edit Comment here: <br><input type = "text" default = "<?php echo $comment; ?> " name= "editcomment" style= "font-size:18pt;height: 50px;width:200px;">
		<input type= "submit" name= "EDIT COMMENT" value= "EDIT COMMENT">
		<input type = "hidden" name = "hidden" value = "edit comment">
		<input type = 'hidden' name = 'comments' value = "<?php echo $commentid; ?>" />
	<input type = 'hidden' name = 'comment_author_id' value = "<?php echo $comment_author_id; ?> "/>
	<input type="hidden" name="token" value="<?php echo $_SESSION['token'];?>" />
		</form>  
    </body>
</html>

