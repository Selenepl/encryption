/*
  Blind Storage Server
*/
//package mp3;

import mp3.BlindStorage;
import mp3.MP3Encryption;

import java.io.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.*;
import java.lang.Integer;
import java.lang.InterruptedException;
import java.lang.StringBuilder;
import java.lang.System;
import java.lang.Thread;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BlindStorageServer {

    public static final int SERVER_PORT = 8888;   /* Make sure the port number is sufficiently high */
    public static final String LOOKUP_CMD = "LOOKUP ";
    public static final String BYE_CMD = "BYE";
    public static final String DOWNLOAD_CMD = "DOWNLD ";
    public static final String REPLY_DATA = "REPLY ";
    public static int numBytesSent = 0;

    public static void Lookup(String documentId, PrintWriter out) {
        // THIS SHOULD BE IMPLEMENTED FOR PART-1
        /* NOTE:  The document id can be in any format you can come up with*/
        // Lookup the document-id in the encrypted documents
        // return the document contents (which are encrypted) to the client
        // out.println("REPLY "+contents);
        java.io.File file = new File("./EncryptedFiles");
        String contents;
        File[] userDirectories = file.listFiles();
        List<File> emails = new ArrayList<File>();
        for (File user : userDirectories) {
            for (File email : user.listFiles()) {
                emails.add(email);
            }
        }
        for (File email : emails) {
            if (email.getName().equals(documentId)) {

                try {
                    Scanner scanner = new Scanner(email);
                    StringBuilder builder = new StringBuilder();
                    while (scanner.hasNext()) {
                        builder.append(scanner.nextLine());
                    }
//                    System.out.println(REPLY_DATA + builder.toString() + "\n" + "\n");
//                    String temp = builder.toString();
//                    MP3Encryption enc = new MP3Encryption("illinois");
//                    byte[] tempBytes = enc.decrypt(enc.hexStringToByteArray(temp));
//                    try {
//                        String tempOutput = new String(tempBytes, "UTF-8");
//                        System.out.println(tempOutput);
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
                    out.println(REPLY_DATA + builder.toString() + "\n" + "\n");
                    out.flush();
                } catch (FileNotFoundException e) {
                    out.println(e.getMessage());
                }

            }
        }
    }

    public static void Download(String blockIndex, PrintWriter out) {
        // THIS SHOULD BE IMPLEMENTED FOR PART-2
        // NOTE:  The blockIndex can be in any format you like
        // Lookup the block-index in the encrypted documents and return it to the clients
        // out.println("REPLY "+contents);
//        System.out.println("In download function");
        Integer id = new Integer(blockIndex);
        System.out.println("Location: " + id);
        BlindStorage store = new BlindStorage(2048, false, false, "");
        Integer[] ids = new Integer[1];
        ids[0] = id;
        List<byte[]> blocks = store.getBlocks(ids);
        System.out.println(blocks.size());
//        System.out.println("Got bytes..");
//        System.out.println("Blocks:");
        if (blocks.size() == 0) {
            out.println("");
        } else {
            String temp = MP3Encryption.bytesToHex(blocks.get(0));
            numBytesSent += temp.getBytes().length;
            out.println(temp);
        }
        System.out.println("Number of Bytes Sent: " + numBytesSent);
        out.flush();
    }

    public static void main(String[] args) {
        PrintWriter out1 = new PrintWriter(System.out);
        //Lookup("fffe47d9d214fb2b9300028713ca4da0", out1);
        ServerSocket serverSocket;
        Socket clientSocket;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;

        // NOTE:  Here you need to send arguments for the encrypted store location and any additional
        // parameters you may need

        try {
            //InetAddress addrr = InetAddress.getByName("172.16.184.240");
            //InetAddress addrr = InetAddress.getByName("172.16.215.171");
            InetAddress addrr = InetAddress.getByName(args[0]);
            serverSocket = new ServerSocket(SERVER_PORT, 5, addrr);  //Server socket
            if (serverSocket.isBound()) {
                System.out.println("Server port bound: " + serverSocket.getLocalSocketAddress());
            }
        } catch (IOException e) {
            System.err.println("ERROR: Could not listen on server port: " + SERVER_PORT);
            System.err.println(e.getMessage());
            System.err.println(e.getCause());
            System.err.println(e.getStackTrace());
            return;
        }
        while (true) {
            // NOTE:  For implementation efficiency, you can handle the client connection (or parts of the client operation) as threads
            System.out.println("rgiht before try");
            try {
                clientSocket = serverSocket.accept();   //accept the client connection
                System.out.println("Clicent socket open");
                inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
                bufferedReader = new BufferedReader(inputStreamReader); //get the client message
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                String message = bufferedReader.readLine();
                System.out.println(message);
                if (message.equalsIgnoreCase(BYE_CMD)) {
                    System.out.println(BYE_CMD);
                    inputStreamReader.close();
                    clientSocket.close();
                    break;
                } else if (message.startsWith(LOOKUP_CMD)) {
                    System.out.println(LOOKUP_CMD);
                    String documentId = message.substring(LOOKUP_CMD.length());
                    Lookup(documentId, out);
                } else if (message.startsWith(DOWNLOAD_CMD)) {
                    System.out.println(DOWNLOAD_CMD);
                    String blockIndex = message.substring(DOWNLOAD_CMD.length());
                    Download(blockIndex, out);
                } else {
                    System.err.println("ERROR: Unknown command sent to server " + message);
                }

            } catch (IOException ex) {
                System.err.println("ERROR: Problem in reading a message" + ex);
            /*} catch (InterruptedException ex) {
                System.err.println("INTERRPT: caguht one"+ ex);
                return;*/
            } catch (java.lang.NullPointerException ex) {
                System.err.println("NULL: The message was null....");
            }
        }

    }
}
