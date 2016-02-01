<html>
<body>
<script>
// support V1 jssdk
var squid_api_utils = window.opener.squid_api || window.opener.squidapi.utils;
// call back api with proper token
squid_api_utils.initToken('<%=request.getAttribute("access_token") %>');
window.close();
</script>
</body>
</html>