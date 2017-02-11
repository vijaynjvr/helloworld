import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
     // Step 1: Extend HttpServlet.
public class ServletSample  extends HttpServlet {

     // Step 2: Override the required methods.
public void doGet (HttpServletRequest request, HttpServletResponse response) 	
       throws ServletException, IOException     {

     // Step 3: Get the HTTP request information, if any.
     Enumeration keys;
     String key;
     String myName = "";
     keys = request.getParameterNames();
     while (keys.hasMoreElements())
     {
     	key = (String) keys.nextElement();
     	if (key.equalsIgnoreCase("myName"))
     		myName = request.getParameter(key);	
     }
     System.out.println("Name = ");
     if (myName == "")
     		myName = "Hello";
     // Step 4: Create the HTTP response.
     //response.setContentType("text/html"); 
     //response.setHeader("Pragma", "No-cache");
     //response.setDateHeader("Expires", 0);
     //response.setHeader("Cache-Control", "no-cache");
     //PrintWriter out = response.getWriter(); 
     //out.println("<html>"); 	 
     //out.println("<head><title>Just a basic servlet</title></head>");
     //out.println("<body>");
     //out.println("<h1>Just a basic servlet</h1>");
     //out.println ("<p>" + myName +  ", this is a very basic servlet that writes an HTML page. The source code for more //interesting sample servlets is in the Application Server samples/ directory.");
     //out.println ("<p>For instructions on running those samples on your Application Server, open the page:");
     //out.println("<pre>http://<em>your.server.name</em>/IBMWebAs/samples/index.html</pre>");
     //out.println("where <em>your.server.name</em> is the hostname of your Application Server.");    
     //out.println("</body></html>");    
     //out.flush();    
	//RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("samples.html");
	//dispatcher.include(request,response);
	response.sendRedirect("sampleh.html");

  }
}
