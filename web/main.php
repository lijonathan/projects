<?php
session_start();

$uName = $_SESSION['username'];

echo "Welcome $uName \n <br>";

echo "File Sharing Center";

if ($handle = opendir('/Module2/$uName')) {
	while( false!== ($file = readdir($handle))) {
		$thelist .= '<li><a href = "'.$file'">'.file'</a></li>';
		}
	}
closedir($handle);

?>

<html>

<br>
<h1> List of file:</h1>
<ul><?php echo $thelist; ?></ul>

<form enctype="multipart/form-data" action="upload_helper.php" method="POST">
	<p>
		<input type="hidden" name="MAX_FILE_SIZE" value="20000000" />
		<label for="uploadfile_input">Choose a file to upload:</label> <input name="uploadedfile" type="file" id="uploadfile_input" />
	</p>
	<p>
		<input type="submit" value="Upload File" />
	</p>
</form>

<form action = "logout.php" method = "POST">
	<input type = "submit" value = "Logout" />
</form>
</html>

