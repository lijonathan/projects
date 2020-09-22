<!DOCTYPE html>
    <head>
            <title> Module3 storyboard</title>
    </head>
    
    <body>
     
        <p> LOGIN</p>
        <form action = "loginaction.php" method = "POST">
            Username: <input type = "text" name = "username" id = "user"><br>
            Password: <input type = "text" name = "password" id = "password"><br>
           
            <input type = "submit" value = "Login" >
            <input type = "hidden" name = "hidden" value = "login">
        </form>
        
        <p>SIGN UP FOR NEW ACCOUNT </p>
        <form action = "loginaction.php" method = "POST">
            Username: <input type = "text" name = "username" id = "user"><br>
            Password: <input type = "text" name = "password" id = "password"><br>
            Email: <input type = "text" name = "Email Address" id = "email"><br>
            
             <input type = "submit" value = "Register">
             <input type = "hidden" name = "hidden" value = "register">
        </form>
        
        <p> LOGIN AS GUEST </p>
        <form action = "guestHomePage.php" >
        <input type = "submit" value = "Guest Login">
        </form>
    </body>

</html>

