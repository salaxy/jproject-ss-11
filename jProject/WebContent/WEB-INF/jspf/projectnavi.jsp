<%-- 
    Document   : projectnavi
    Created on : 09.06.2011, 21:07:47
    Author     : MacYser
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<a class="buttoncontent" href="JProjectServlet?do=ShowProject&projectName=${sessionScope.aktProject}"><h1>Project</h1></a>
<a class="buttoncontent" href="JProjectServlet?do=ShowAllSource&projectName=${sessionScope.aktProject}"><h1>Sourcecode</h1></a>
<a class="buttoncontent" href="JProjectServlet?do=ShowAllDocu&projectName=${sessionScope.aktProject}"><h1>Documents</h1></a>
<a class="buttoncontent" href="JProjectServlet?do=ShowAllTasks&projectName=${sessionScope.aktProject}"><h1>Tasks</h1></a>
<h1>&nbsp;</h1>

