<!DOCTYPE html>
<html>
    <body>
        
<form action="CompleteComment.php" method= "POST">
        Insert Comment here: <br><input type = "text" name= "comment" style= "font-size:18pt;height:200px;width:50px;">
		<input type= "submit" name= "SUBMIT COMMENT" value= "SUBMIT COMMENT">
		<input type="hidden" name="token" value="<?php echo $_SESSION['token'];?>" >
		<input type = "hidden" name = "hidden" value = "submit comment">
		</form>        

<form 
    </body>
</html>