<!DOCTYPE html>
<html>
<head>
	<title> Calculator </title>
</head>
<body>

<form action="<?php echo htmlentities($_SERVER['PHP_SELF']); ?>" method="GET">
<input type = "number" name = "number1"><br>
<input type = "number" name = "number2"><br>
<input type ="submit"> 

	<input type="radio" name="Operation" value="Divide" checked> /
	<br>
	<input type="radio" name="Operation" value="Multiply"> *
	<br>
	<input type="radio" name="Operation" value="Addtion">+
	<br>
	<input type="radio" name="Operation" value="Subtraction">-
</form>

<?php
function add($x, $y){
	echo $x + $y;
}

function multiply($x, $y){
	echo $x * $y;
}

function subtract($x, $y){
	echo $x - $y;
}
function divide($x, $y){
	if($y != 0){
		echo $x / $y;
	}
	else{
		echo "error, cannot divide by zero";
	}
}
if(empty($_GET)){

}
else{
	$number1 = $_GET['number1'];
	$number2 = $_GET['number2'];
	$Operation = $_GET['Operation'];

	if($Operation == Divide){
		divide($number1, $number2);
	}
	else if($Operation == Multiply){
		multiply($number1, $number2);
	}
	else if($Operation == Addition){
		add($number1, $number2);
	}
	else{
		subtract($number1, $number2);
	}
}
?>

</body>
</html>