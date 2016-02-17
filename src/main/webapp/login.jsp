<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>

<%@page import="java.net.URLEncoder"%><html lang="en">
  <head>
    <meta charset="utf-8">
    <title>Bouquet Sign-in</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="shortcut icon" href="favicon.ico"></link>
    
    <!-- 
    version : ${applicationScope.version}
     -->
    

    <!-- Le styles -->
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.0/css/bootstrap.min.css">

    <link type="text/css" rel="stylesheet" href="squid.css">

    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="../assets/js/html5shiv.js"></script>
    <![endif]-->
    
                               
  </head>

  <body>

<c:set var="application_mode">
	${param.application_mode}
</c:set>
<c:if test="${!empty application_mode}">
<c:set var="application_mode">
	${application_mode}
</c:set>
</c:if>
<c:if test="${empty application_mode}">
<c:set var="application_mode">
	index.html
</c:set>
</c:if>

<c:set var="rurl">
	${param.redirect_uri}
</c:set>
<c:if test="${!empty redirect_uri}">
<c:set var="rurl">
	${redirect_uri}
</c:set>
</c:if>

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

<c:set var="rurl">
	${param.redirect_uri}
</c:set>
<c:if test="${!empty redirect_uri}">
<c:set var="rurl">
	${redirect_uri}
</c:set>
</c:if>

<div class="container">
<br></br>
</div>

<form class="sq-form acc_req_form" action="./oauth" method="post">
	<img src="bouquet-logo.png">

	<h2 class="form-signin-heading">Please sign in</h2>
	
	<c:if test="${!empty requestScope.response_type}">
	    <input type="hidden" name="response_type" value="${requestScope.response_type}"></input>
	</c:if>
  
	<c:if test="${!empty application_mode}">
	    <input type="hidden" name="application_mode" value="${application_mode}"></input>
	</c:if>
	
	<c:if test="${!empty rurl}">
	    <input type="hidden" name="redirect_uri" value="${rurl}"></input>
	</c:if>

	<c:if test="${error != null}">
		<c:if test="${error == 'true'}">
        <p class="text-danger">Error : invalid login/password.</p>
        </c:if>
        <c:if test="${error != 'true'}">
        <p class="text-danger">Error : ${error}.</p>
        </c:if>
    </c:if>
    <c:if test="${krakenUnavailable != null}">
        <p class="text-danger">Authentication server unavailable, please retry later.</p>
    </c:if>
    <c:if test="${duplicateUser != null}">
        <p class="text-danger">Please select a customer.</p>
    </c:if>
    
    <script>
    	var formSubmit = function(customerId) {
    	    $("#customerId").val(customerId);
    	    return true;
    	}
    </script>

	<c:if test="${!empty duplicateUser}">
		<div class="form-actions">
		<c:forEach var="customer" items="${customers}">
			<div style="padding: 5px;">
				<button class="btn btn-select" name="submit" type="submit"
					onClick="formSubmit('${customer.id}');">
					<c:if test="${!empty customer.name}">
	               ${customer.name}
	               </c:if>
					<c:if test="${empty customer.name}">
	               #${customer.id}
	               </c:if>
				</button>
			</div>
		</c:forEach>
		</div>
		<input type="hidden" name="step" value="customerSelection"></input>
	</c:if>

	<input type="hidden" name="customerId" value="${cid}" id="customerId"></input>
    
   	<c:if test="${!empty clid}">
		<input type="hidden" name="client_id" value="${clid}"></input>
	</c:if>
	
	<c:if test="${empty duplicateUser}">
		<c:if test="${empty clid}">
			<input type="text" name="client_id" class="input-block-level" placeholder="Client ID">
		</c:if>

		<input type="text" name="login" class="input-block-level" placeholder="Login" value="${param.login}">
		
		<input type="password" name="password" class="input-block-level" placeholder="Password" autocomplete="off">
	
		<div class="form-actions">
			<button class="btn btn-primary" name="submit" type="submit">Sign in</button>
		</div>
		<span class="help-block">Forgot your password :
		<c:choose>
		    <c:when test="${!empty cid}">
		       <a href="./lost?customerId=${cid}">password reset</a>
	        </c:when>
	        <c:otherwise>
		       <a href="./lost">password reset</a>
		    </c:otherwise>
		</c:choose>
		</span>
	</c:if>
</form>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.0/js/bootstrap.min.js"></script>
        
</body>
</html>

