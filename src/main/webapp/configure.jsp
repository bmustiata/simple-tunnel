<%@ page import="com.ciplogic.simpletunnel.ServletFilterRedirect" %><%

    String redirectPort = request.getParameter("redirectPort");

    ServletFilterRedirect.REDIRECT_HOST =  request.getParameter("redirectHost");
    ServletFilterRedirect.REDIRECT_PORT = redirectPort == null ? 80 : Integer.parseInt(redirectPort);
%>
Configured.