<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="javax.servlet.http.HttpServletRequest" %>
<%@ page import="javax.servlet.http.HttpServletResponse" %>

<html>
<head>
    <title>Xsolla Integration</title>
    <script src="https://login-sdk.xsolla.com/latest/"></script>
</head>
<body>

<fieldset>
    <legend>Create token (server side)</legend>

    <form action="client-side" method="post">
        <label>Project id <input name="project" type="text" value="<%= request.getAttribute("xsollaProjectId") %>"></label><br><br>
        <label>Login id <input name="login" type="text" value="<%= request.getAttribute("xsollaLoginId") %>"></label><br><br>
        <label>User token <input id="user" name="userToken" type="text" value="<%= request.getAttribute("userToken") != null ? request.getAttribute("userToken") : "" %>"></label> <button type="button" onclick="openWidget()">Auth</button><br><br>        <label>Item sku <input name="itemSku" type="text" value="mysku01"></label><br><br>
        <label>Webhook secret key <input name="webhookSecretKey" type="text" value="<%= request.getAttribute("xsollaWebhookSecretKey") %>"></label><br><br>
        <label>JSON body <textarea name="body" cols="90" rows="40">
{
    "sandbox": true
}
        </textarea></label><br><br>
        <input type="submit" value="Submit">
    </form>

<div id="result">
    <%
        String apiResponse = (String) request.getAttribute("apiResponse");
        if (apiResponse != null) {
    %>
        <%= apiResponse %>
    <%
        }
    %>
</div>
</fieldset>

<div id="xl_auth" style="width: 100%; height: 600px"></div>

<script>
    let xl;
    function openWidget() {
        xl.open();
    }

    let token = new URLSearchParams(window.location.search).get('token');
    if (token) {
        document.getElementById('user').value = token;
    }

    window.onload = function () {
        let loginId = document.querySelector('input[name="login"]').value;
        xl = new XsollaLogin.Widget({
            projectId: loginId,
            callbackUrl: window.location.origin + window.location.pathname,
            preferredLocale: "en_XX"
        });
        xl.mount("xl_auth");
    };
</script>

</body>
</html>