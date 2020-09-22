<!DOCTYPE html>
    <head>
    </head>
	<body>
 <?php
 if($_SESSION['token'] !== $_POST['token']){
	die("Request forgery detected");
}
else{
	require 'database.php';
	session_start();
	$storyid = $_POST['storyid'];
	$storyauthor = $_POST['storyauthor'];
	

	$stmt = $mysqli -> prepare("select title, stories from stories where id= ?");

if(!$stmt){
	
	printf("Query Prep Failed: %s\n", $mysqli->error);
	
	exit;
}
	
 
$stmt -> bind_param('s', $storyid);

$stmt->execute();

$stmt->bind_result($oldtitle, $oldstory);
while($stmt-> fetch()){
echo $oldtitle;
echo $oldstory;	

}
$stmt ->close();
	
}
?>
<form action="EditStory.php" method= "POST">
    Edit Title here: <br><input type = "text" default = "<?php echo $title; ?> " name= "editedtitle" style= "font-size:18pt;height:100px;width:50px;">
    <br/> Edit Story here: <br><input type = "text" default = "<?php echo $story; ?> " name= "editstory" style= "font-size:18pt;height:200px;width:50px;">
		<input type= "submit" name= "EDIT STORY" value= "EDIT STORY">
		<input type = 'hidden' name = 'storyid' value = "<?php echo $storyid; ?>" />
	<input type = 'hidden' name = 'storyauthorid' value = "<?php echo $storyauthor; ?> "/>
	<input type="hidden" name="token" value="<?php echo $_SESSION['token'];?>" />
		</form>

	</body>