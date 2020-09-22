<!DOCTYPE html>
<html>
    <body>
        <form action = "Logout.php" method = "POST">
        <input type = "submit" value = "Logout">
        </form>
		 <form action = "DeleteAccount.php" method = "POST">
        <input type = "submit" value = "Delete Account">
        </form>

<?php
require 'database.php';
session_start();


echo "Your Stories!";
$users_id = $_SESSION['userid'];

$stmt = $mysqli -> prepare("select title, stories, id from stories where users_id = ?");

if(!$stmt){
	
	printf("Query Prep Failed: %s\n", $mysqli->error);
	
	exit;
}
$stmt->bind_param ('i', $users_id);
$stmt->execute();

$stmt->bind_result($title, $story, $storyid);


while($stmt -> fetch()){
    echo "<br>";
    echo "<input type = 'radio' name = 'profileid' value ='".$storyid."'>".$title."</input>"; 
	echo "<form action= 'ProfilePostPage.php' method = 'POST'>";
	echo "<input type = 'hidden' name = 'storyid' value = '".$storyid."'/>";
	echo	"<input type = 'submit' value = 'View Selected Story'/>";
echo "</form>";

	echo "<br>";
	echo "<form action= 'DeleteStory.php' method = 'POST'/>";
    echo " <input type = 'submit' name = 'Delete Story' value = 'Delete Selected Story'/>";
    echo  "<input type = 'hidden' name = 'storyid' value = '".$storyid."'/>";
	echo  "<input type = 'hidden' name = 'storyauthor' value = '".$users_id."'/>";
    echo  "</form>";
	
	echo "<form action= 'StoryEdit.php' method = 'POST'>";
    echo " <input type = 'submit' name = 'Edit Story' value = 'Edit Selected Story'/>";
    echo  "<input type = 'hidden' name = 'storyid' value = '".$storyid."'/>";
	echo  "<input type = 'hidden' name = 'storyauthor' value = '".$users_id."'/>";
    echo  "</form>";
}

?>

    </body>
</html>
    