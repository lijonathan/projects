<!DOCTYPE html>
<html>
    <body>
        <form action = "Logout.php" method = "POST">
        <input type = "submit" value = "Logout">
        </form>
		
		<form action = "ProfilePage.php" method = "POST">
        <input type = "submit" value = "Go to Profile Page">
        </form>
		
<form action="storypage.php" method= "POST">
		Title: <br><input type = "text" name = "title" style = "width: 50px;">
       <br> Insert Story here: <br><input type = "text" name= "story" style= "font-size:18pt;height:100px;width:200px;">
		<input type= "submit" name= "SUBMIT STORY" value= "SUBMIT STORY">
		<input type = "hidden" name = "hidden" value = "submit story">
		</form>        



<?php

require 'database.php';

session_start();


$username = $_SESSION['username'];
$users_id = $_SESSION['userid'];
echo "Hello ".$username;
echo $users_id;
echo $_SESSION['userid'];

$stmt = $mysqli -> prepare("select title, id, users_id from stories");

if(!$stmt){
	
	printf("Query Prep Failed: %s\n", $mysqli->error);
	
	exit;
}
 
$stmt->execute();

$stmt->bind_result($title, $id, $story_author_id);

//$_SESSION['userid'] = $users_id;

//echo "<form action= 'PostsPage.php' method = 'POST'>";

while($stmt->fetch()){
	echo "<br>";
    echo "<input type = 'radio' name = 'id' value ='".$id."'>".$title."</input>"; 
	echo "<form action= 'PostsPage.php' method = 'POST'>";
	echo "<input type = 'hidden' name = 'users_id' value = '".$users_id."'/>";
	echo  "<input type = 'hidden' name = 'storyid' value = '".$id."'/>";
	echo	"<input type = 'submit' value = 'View Selected Story'/>";
echo "</form>";

	echo "<br>";
	echo "<form action= 'DeleteStory.php' method = 'POST'/>";
    echo " <input type = 'submit' name = 'Delete Story' value = 'Delete Selected Story'/>";
    echo  "<input type = 'hidden' name = 'storyid' value = '".$id."'/>";
	echo  "<input type = 'hidden' name = 'storyauthor' value = '".$story_author_id."'/>";
    echo  "</form>";
	
	echo "<form action= 'StoryEdit.php' method = 'POST'>";
    echo " <input type = 'submit' name = 'Edit Story' value = 'Edit Selected Story'/>";
    echo  "<input type = 'hidden' name = 'storyid' value = '".$id."'/>";
	echo  "<input type = 'hidden' name = 'storyauthor' value = '".$story_author_id."'/>";
    echo  "</form>";
    
}

$stmt->close();
?>
	
    </body>
</html>