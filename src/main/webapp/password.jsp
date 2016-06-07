<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>

<%@page import="java.net.URLEncoder"%><html lang="en">
  <head>
    <meta charset="utf-8">
    <title>Bouquet password update</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- Le styles -->
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.0/css/bootstrap.min.css">

    <link type="text/css" rel="stylesheet" href="squid.css">

    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="../assets/js/html5shiv.js"></script>
    <![endif]-->
                               
  </head>

  <body>

<div class="container">
<br></br>
</div>


	<img src="bouquet-logo.png">

	<h2 class="form-signin-heading">Change Password</h2>

	<c:if test="${error != null}">
        <p class="text-danger">Error : ${error}</p>
    </c:if>
    
    <c:if test="${krakenUnavailable != null}">
        <p class="text-danger">Server unavailable, please retry later.</p>
    </c:if>
    
    <c:if test="${message != null}">
        <p class="text-warning">${message}</p>
    </c:if>

	<c:if test="${(message == null)}">
	<form class="sq-form acc_req_form" action="./password" method="post">
		<input type="hidden" name="access_token" value="${access_token}" id="access_token"></input>
		<div>
		Please set a new password for the account : <b>${user.login}</b>
		</div>
		<input type="password" name="password" class="input-block-level" placeholder="New password">
		<p class="text-muted">
		Note : password should be at least 8 characters long and contain at least a capital letter or a digit
		</p>
		<div class="form-actions">
			<button class="btn btn-primary" name="submit" type="submit">Submit</button>
		</div>
		
	</form>
	</c:if>
		


<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.0/js/bootstrap.min.js"></script>
        
</body>
</html>

