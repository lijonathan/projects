/*
This is a TcpMapServer that stores (key, value) strings.
The port number defaults to 30123.
The server recieves commands that store, retrieves, and modifies key
value pairs within the server map. 

The four commands are get, put, remove, and get all.
*/


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class TcpMapServer {
	
	public static void main(String args[]) throws IOException {

		InetAddress address = null;
		int port_number = 30123;
		HashMap<String,String> server_map = new HashMap<String,String>();
		
		//default port_number and address if none provided
		if (args.length > 0) {
			address = InetAddress.getByName(args[0]);
			if (args.length > 1) {
				port_number = Integer.parseInt(args[1]);
			}
		}

		ServerSocket server_socket = new ServerSocket(port_number, 0, address);
		//continually runs the server, waits for incoming connections
		while (true) {
			//accepts incoming connections
			Socket conncting_socket = server_socket.accept();
			BufferedReader input_reader = new BufferedReader(
				new InputStreamReader(conncting_socket.getInputStream()));
			BufferedWriter output_writer = new BufferedWriter(
				new OutputStreamWriter(conncting_socket.getOutputStream()));
			String input = ""; 
			String send = "";
			while ((input = input_reader.readLine()) != null) {
				String[] split_input = input.split(":",2);
				String operation = split_input[0];
				String key = "";
				//error checking for properly formatted commands
				if(split_input.length != 2 && !split_input[0].equals("get all")){
					send = "error:unrecognizable input:" + input;
				}
				else{
					//responds apropriately to each command
					if (operation.equals("get all")) {
						String temp = "";
						for (String key_iterator : server_map.keySet()) {
							temp += (key_iterator + ":" +
								server_map.get(key_iterator) + "::");
						}
						if (temp.length() > 0){
							send = temp.substring(0, temp.length() - 2);
						}	
					}
					else if (operation.equals("get")) {
							key = split_input[1];
							String value = server_map.get(key);
							if(server_map.containsKey(key)) {
								send = "ok:" + value;
							}
							else {
								send = "no match";
							}
					}
					else if(operation.equals("remove")) {
						key = split_input[1];
						if(server_map.containsKey(key)) {
							send = "ok";
						}
						else {
							send = "no match";
						}
						
						server_map.remove(key);			
					}
					else if (operation.equals("put")) {
						String[] put_parts = split_input[1].split(":",2);
						key = put_parts[0];
						String value = put_parts[1];
						if(server_map.containsKey(key)) {
							send = "updated:" + key;
						}
						else {
							send = "ok";
						}
						server_map.put(key, value);
						
					} 
					else {
						send = "error:unrecognizable input:" + input;
					}
				}
				//sends the response
				System.out.println(send);
				output_writer.write(send); 
				output_writer.newLine(); 
				output_writer.flush();
			}
		}
	}
}