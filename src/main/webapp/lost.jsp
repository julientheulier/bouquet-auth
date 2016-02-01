<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>

<%@page import="java.net.URLEncoder"%><html lang="en">
  <head>
    <meta charset="utf-8">
    <title>Squid Analytics Lost Password</title>
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

<c:set var="cid">
	${param.customerId}
</c:set>
<c:if test="${!empty customerId}">
<c:set var="cid">
	${customerId}
</c:set>
</c:if>

<c:set var="clid">
	${param.client_id}
</c:set>
<c:if test="${!empty client_id}">
<c:set var="clid">
	${client_id}
</c:set>
</c:if>

<div class="container">
<br></br>
</div>

<form class="sq-form acc_req_form" action="./lost" method="post">
	<img src="bouquet-logo.png">

	<h2 class="form-signin-heading">Reset Password</h2>

	<c:if test="${error != null}">
        <p class="text-danger">Error : ${error}</p>
    </c:if>
    
    <c:if test="${krakenUnavailable != null}">
        <p class="text-danger">Authentication server unavailable, please retry later.</p>
    </c:if>
    
    <c:if test="${message != null}">
        <p class="text-warning">${message}</p>
    </c:if>

	<c:if test="${message == null}">
		<input type="hidden" name="customerId" value="${cid}" id="customerId"></input>
	    
	   	<c:if test="${!empty clid}">
			<input type="hidden" name="client_id" value="${clid}"></input>
		</c:if>
		
	
		<input type="email" name="email" class="input-block-level" placeholder="Email">
	
		<div class="form-actions">
			<button class="btn btn-primary" name="submit" type="submit">Send reset password request</button>
		</div>
	</c:if>
		
</form>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.0/js/bootstrap.min.js"></script>
        
</body>
</html>

