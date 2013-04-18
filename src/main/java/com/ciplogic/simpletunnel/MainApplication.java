package com.ciplogic.simpletunnel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainApplication {
    private static int localPort;
    private static String remoteHost;
    private static int remotePort;

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            printUsage();
            return;
        }

        parsePrameters(args);
        runServer();
    }

    private static void parsePrameters(String[] args) {
        localPort = Integer.parseInt(args[0]);
        remoteHost = args[1];
        remotePort = Integer.parseInt(args[2]);
    }

    private static void runServer() throws IOException {
        ServerSocket server = new ServerSocket(localPort, 10);
        do {
            processConnection(server.accept());
        } while (true);
    }

    private static void processConnection(Socket fromSocket) throws IOException {
        try {
            Socket toSocket = new Socket(remoteHost, remotePort);

            new Thread(new CopyStream(fromSocket.getInputStream(), toSocket.getOutputStream())).start();
            new Thread(new CopyStream(toSocket.getInputStream(), fromSocket.getOutputStream())).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar tunnel.jar LISTEN_PORT REMOTE_HOST REMOTE_PORT");
    }
}
