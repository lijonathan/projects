<!DOCTYPE html>
<html>
    <body>

<p>SIGN UP FOR NEW ACCOUNT </p>
        <form action = "loginaction.php" method = "POST">
            Username: <input type = "text" name = "username" id = "user"><br>
            Password: <input type = "text" name = "password" id = "password"><br>
			<input type="hidden" name="token" value="<?php echo $_SESSION['token'];?>" />
             <input type = "submit" value = "Register">
             <input type = "hidden" name = "hidden" value = "register">
        </form>
<?php
if($_SESSION['token'] !== $_POST['token']){
	die("Request forgery detected");
}
else{
require 'database.php';

session_start();

$id= $_POST['id'];//story id
//$_SESSION['storyid'] = $id;

$stmt = $mysqli->prepare("select stories, title from stories where id = ?");
if(!$stmt){
	printf("Query Prep Failed: %s\n", $mysqli->error);
	echo "fails";
    exit;
    
}

$stmt -> bind_param('s', $id);

$stmt->execute();

$stmt->bind_result($stories, $title);

$stmt->fetch();



$stmt->close();
//////////////////////////////////////////////////////////////////

$stmt = $mysqli -> prepare("select comment, stories_id, id from comments where stories_id= ?");

if(!$stmt){
	
	printf("Query Prep Failed: %s\n", $mysqli->error);
	
	exit;
}
 
$stmt -> bind_param('s', $id);

$stmt->execute();

$stmt->bind_result($comment, $id, $commentid);




echo "Story Title:".$title."\n";
echo "<br/>";
echo "Story:".$stories;
echo "<br/>";
echo "Comments:".$comment;
echo "<br/>";
while($stmt->fetch()){
	printf("<br/>". $comment );
	
}

$stmt->close();
}
?>

    </body>
</html>
