package com.example.cs463mp3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ClientActivity extends Activity {

	private Socket client;
	private PrintWriter output;
	private EditText textField;
	private Button button;
	private String messsage;
	public static final String SERVER_IP = ""; /* Set your VM's server IP here */
	public static final int SERVER_PORT = 8888; /*
												 * Set your VM's server port
												 * here: Make sure the port
												 * number is sufficiently high
												 * for your program to listen on
												 * that number
												 */
	public static final String LOOKUP_CMD = "LOOKUP ";

	public static void Lookup(String keyword, PrintWriter output) {
		String documentId = "";

		// For Part-1
		// USE Local Index (cached) and find the documentId from there
		// For Part-2
		// (a) if index is present in local cache use it to find the document id
		// (otherwise) use the download functionality to get the appropriate
		// document-ids from the server
		// and use them to download the files: This would be done by
		// implementing SSE.Search functionality

		//PrintWriter.write("LOOKUP " + documentId); // write the message to
													// output stream
		// Get the output and show to the user
	}

	@Override
	    public void onCreate(Bundle savedInstanceState) {
	            super.onCreate(savedInstanceState);
	            setContentView(R.layout.main);

	            textField = (EditText) findViewById(R.id.editText1); //reference to the text field
	            button = (Button) findViewById(R.id.button1);   //reference to the send button
	            //read in stopwords file

	            //Button press event listener
	            button.setOnClickListener(new View.OnClickListener() {

	                    public void onClick(View v) {
	                        String keyword = textField.getText().toString(); //get the keyword from the textfield
	                        textField.setText("");      //Reset the text field to blank

	                        try {
	                            client = new Socket(SERVER_IP, SERVER_PORT);  //connect to server
	                            output= new PrintWriter(client.getOutputStream(),true);
	                            Lookup(keyword,output);
	                            //printwriter.flush();
	                            //printwriter.close();
	                            //client.close();   //closing the connection

	                        } catch (UnknownHostException e) {
	                            e.printStackTrace();
	                        } catch (IOException e) {
	                            e.printStackTrace();
	                        }
	                    }
	            });

	        }
}
