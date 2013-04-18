package com.ciplogic.simpletunnel;

import org.apache.commons.io.IOUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Enumeration;

public class ServletFilterRedirect implements Filter {
    public static String REDIRECT_HOST = null;
    public static int REDIRECT_PORT = 80;

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (!isRedirectUrlConfigured()) {
            filterChain.doFilter(request, response);
            return;
        }

        redirectCall(request, response);
    }

    private boolean isRedirectUrlConfigured() {
        return null != REDIRECT_HOST;
    }

    private void redirectCall(ServletRequest request, ServletResponse response) throws IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        RemoteConnection remoteConnection = null;
        try {
            remoteConnection = new RemoteConnection().create();

            writeTranslatedRequest(httpRequest, remoteConnection);
            copyResponseFromRemote(response, remoteConnection.getInputStream());
        } finally {
            closeQuietly(remoteConnection);
        }
    }

    private void closeQuietly(RemoteConnection socket) {
        if (socket != null) {
            try {
                socket.getSocket().close();
            } catch (Exception e) {
                // ignored
            }
        }
    }

    private void copyResponseFromRemote(ServletResponse response, InputStream remoteInputStream) throws IOException {
        IOUtils.copy(remoteInputStream, response.getOutputStream());
    }

    private void writeTranslatedRequest(HttpServletRequest httpRequest, RemoteConnection remoteConnection) throws IOException {
        writeProtocolLine(httpRequest, remoteConnection.getOutputStream());
        writeHeaders(httpRequest, remoteConnection.getOutputStream());
        writeEmptyLine(remoteConnection.getOutputStream());
        writeContent(httpRequest, remoteConnection.getOutputStream());

        remoteConnection.getOutputStream().flush();
    }

    private void writeProtocolLine(HttpServletRequest httpRequest, OutputStream writer) throws IOException {
        StringBuilder requestLine = new StringBuilder("");

        requestLine.append(httpRequest.getMethod())
                .append(" ")
                .append(httpRequest.getRequestURI());

        if (httpRequest.getQueryString() != null && !"".equals(httpRequest.getQueryString())) {
            requestLine.append("?")
                    .append(httpRequest.getQueryString());
        }

        requestLine.append(" ")
                .append(httpRequest.getProtocol())
                .append("\r\n");

        writer.write( requestLine.toString().getBytes("UTF-8") );
    }

    private void writeHeaders(HttpServletRequest httpRequest, OutputStream writer) throws IOException {
        Enumeration headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            String headerValue = httpRequest.getHeader(headerName);

            String header = capitalize(headerName) + ": " + headerValue + "\r\n";
            writer.write(header.getBytes("UTF-8"));
        }
    }

    private String capitalize(String headerName) {
        return headerName.substring(0, 1).toUpperCase() + headerName.substring(1);
    }

    private void writeEmptyLine(OutputStream writer) throws IOException {
        writer.write("\r\n".getBytes("UTF-8"));
    }

    private void writeContent(ServletRequest request, OutputStream writer) {
        try {
            IOUtils.copy(request.getInputStream(), writer);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public void destroy() {
    }

    private class RemoteConnection {
        private Socket socket;
        private OutputStream outputStream;
        private InputStream inputStream;

        public Socket getSocket() {
            return socket;
        }

        public OutputStream getOutputStream() {
            return outputStream;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public RemoteConnection create() throws IOException {
            socket = new Socket(REDIRECT_HOST, REDIRECT_PORT);

            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            return this;
        }
    }
}
