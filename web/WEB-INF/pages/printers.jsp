<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <%@ include file="/includes.jsp" %>
</head>
<body id="page-top" data-spy="scroll" data-target=".navbar-fixed-top">
<nav class="navbar navbar-inverse navbar-fixed-top">
    <div class="container">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand page-scroll" href="${pageContext.request.contextPath}/">Robostorm Name Tag Auto
                Printing</a>
        </div>
        <div id="navbar" class="collapse navbar-collapse">
            <ul class="nav navbar-nav">
                <li><a href="${pageContext.request.contextPath}/ntap/manager">Back</a></li>
            </ul>
        </div>
    </div>
</nav>
<div class="container main">
    <form:form method="post" action="/ntap/manager/printers" modelAttribute="printerWrapper">
        <table class="table table-striped">
            <thead>
            <tr>
                <th><label class="control-label">Name</label></th>
                <th><label class="control-label">IP</label></th>
                <th><label class="control-label">Port</label></th>
                <th><label class="control-label">API-Key</label></th>
                <th><label class="control-label">Printing</label></th>
                <th><label class="control-label">Active</label></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="printer" items="${printerWrapper.printers}" varStatus="status">
                <tr class="form-inline">
                    <form:hidden path="printers[${status.index}].id" value="${printer.id}"/>
                    <td>
                        <form:input path="printers[${status.index}].name" cssClass="form-control"/>
                    </td>
                    <td>
                        <form:input path="printers[${status.index}].ip" cssClass="form-control"/>
                    </td>
                    <td>
                        <form:input path="printers[${status.index}].port" cssClass="form-control"/>
                    </td>
                    <td>
                        <form:input path="printers[${status.index}].apiKey" cssClass="form-control"/>
                    </td>
                    <td>
                        <form:checkbox path="printers[${status.index}].printing"/>
                    </td>
                    <td>
                        <form:checkbox path="printers[${status.index}].active"/>
                    </td>
                    <form:hidden path="printers[${status.index}].configFile" value="${printer.configFile}"/>
                </tr>
            </c:forEach>
            <tr>
                <td colspan="6"><input type="submit" value="Save" class="btn btn-success"/></td>
            </tr>
            </tbody>
        </table>
    </form:form>
</div>
</body>
</html>
