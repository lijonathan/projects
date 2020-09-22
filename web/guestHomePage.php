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
		<br/>
<?php

require 'database.php';

session_start();
if($_SESSION['token'] !== $_POST['token']){
	die("Request forgery detected");
}

echo "Hello ,Guest User!";
//echo $_SESSION['userid'];

$stmt = $mysqli -> prepare("select title, id from stories");

if(!$stmt){
	
	printf("Query Prep Failed: %s\n", $mysqli->error);
	
	exit;
}
 
$stmt->execute();

$stmt->bind_result($title, $id);

echo "<form action= 'guestPostsPage.php' method = 'POST'>";
while($stmt->fetch()){
	
    echo "<input type = 'radio' name = 'id' value ='".$id."'>".$title."</input>"; 
	echo "<br>";
	
}

echo	"<input type = 'submit' value = 'View Selected Story'/>";
echo "</form>";


echo "</ul>\n";
$stmt->close();

?>
	</body>
</html>