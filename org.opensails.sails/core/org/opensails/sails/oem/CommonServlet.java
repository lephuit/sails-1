package org.opensails.sails.oem;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Answers common Sails resources, like scripts and images, which are shared and
 * used by SailsApplications.
 */
public class CommonServlet extends HttpServlet {
	protected ClasspathResourceResolver resolver = new ClasspathResourceResolver("common");

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pathInfo = request.getPathInfo();
		InputStream stream = resolver.resolve(pathInfo);
		if (stream == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.getWriter().write("Not found");
		} else {
			byte data = -1;
			ServletOutputStream outputStream = response.getOutputStream();
			while ((data = (byte) stream.read()) != -1)
				outputStream.write(data);
		}
	}
}
