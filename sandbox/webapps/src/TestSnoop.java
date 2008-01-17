/*
 *  Copyright(c) 2007 Red Hat Middleware, LLC,
 *  and individual contributors as indicated by the @authors tag.
 *  See the copyright.txt in the distribution for a
 *  full listing of individual contributors.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library in the file COPYING.LIB;
 *  if not, write to the Free Software Foundation, Inc.,
 *  59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
 *
 * @author Jean-Frederic Clere
 * @version $Revision: 420067 $, $Date: 2006-07-08 09:16:58 +0200 (sub, 08 srp 2006) $
 */

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;



/**
 * Snoop servlet...:
 * Dump all the headers and parameters.
 *
 */

public class TestSnoop extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<body bgcolor=\"white\">");
        out.println("<head>");

        String title = "src/TestSnoop.java";
        out.println("<title>" + title + "</title>");
        out.println("</head>");
        out.println("<body>");

        out.println("<h3>" + title + "</h3>");

        HttpSession session = request.getSession(false);
        if (session == null) {
          // Create it.
          out.println("create");
          session = request.getSession(true);
        }
        out.println("sessions.id " + session.getId());
        out.println("<br>");
        out.println("sessions.created ");
        out.println(new Date(session.getCreationTime()) + "<br>");
        out.println("sessions.lastaccessed ");
        out.println(new Date(session.getLastAccessedTime()));
        System.out.println("Created: " + new Date(session.getLastAccessedTime()));

        Enumeration e = request.getParameterNames();
        for ( ; e.hasMoreElements() ;) {
            String name = (String) e.nextElement();
            String value = request.getParameter(name);
            out.println("<P>");
            out.println("name: "  + name + " value: " + value);
            out.println("<P>");
            System.out.println("name: " + name + " value: " + value);
         }
        out.println("<br>contextPath: " + request.getContextPath());
        out.println("<br>servletPath: " + request.getServletPath());
        out.println("<br>pathInfo: " + request.getPathInfo());
        out.println("<br>RequestURI: " + request.getRequestURI());
        out.println("<br>RequestURL: " + request.getRequestURL());
        out.println("<br>QueryString: " + request.getQueryString());


    }

    public void doPost(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        doGet(request, response);
    }

}
