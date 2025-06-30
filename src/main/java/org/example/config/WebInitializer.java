package org.example.config;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class WebInitializer implements WebApplicationInitializer {

    private static final String LOCATION = System.getProperty("java.io.tmpdir");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final long MAX_REQUEST_SIZE = 10 * 1024 * 1024;
    private static final int FILE_SIZE_THRESHOLD = 1024 * 1024;

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(SpringConfig.class);

        servletContext.setInitParameter("spring.profiles.active", "default");

        System.out.println(">> Servlet 3.0 multipart support: " + servletContext.getMajorVersion() + "." + servletContext.getMinorVersion());

        ServletRegistration.Dynamic servlet = servletContext.addServlet("dispatcher", new DispatcherServlet(context));
        servlet.setLoadOnStartup(1);
        servlet.addMapping("/");

        MultipartConfigElement multipartConfig = new MultipartConfigElement(
                LOCATION, MAX_FILE_SIZE, MAX_REQUEST_SIZE, FILE_SIZE_THRESHOLD);
        servlet.setMultipartConfig(multipartConfig);
    }
}
