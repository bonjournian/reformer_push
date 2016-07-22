<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
	<form action="/pushCenter/push/save" method="post">
		<textarea rows="3" cols="23" name="json"></textarea>
		<input type="submit" value="POST">
	</form>
	<hr>
	<h4>the second</h4>
	<form action="/pushCenter/push/send" method="post">
		<textarea rows="3" cols="23" name="json"></textarea>
		<input type="submit" value="POST">
	</form>
</body>
</html>