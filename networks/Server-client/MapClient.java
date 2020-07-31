/*
This serves as a Client.
MapClient host_name port_number command payload(optional) payload(optional)

This sends Datagram Packets to the MapServer, and prints out the reponse
from the server.

The command is "get", "remove", or "put"
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class MapClient {
	
	public static void main(String args[]) throws IOException{
		
		if (args.length < 3) {
			System.out.print("Incorrect usage");
			return;
		}
		
		String host_name = args[0];
		InetAddress server_address = InetAddress.getByName(host_name);
		
		int port_number = Integer.parseInt(args[1]);
		String send_message = args[2];
		
		if (args.length > 3) {
			send_message = send_message + ":" + args[3];
			if(args.length > 4) {
				send_message = send_message + ":" + args[4];
			}
		}
		
		byte[] buffer = send_message.getBytes("US-ASCII");
		DatagramSocket datagram_socket = new DatagramSocket();
		DatagramPacket sent_packet = new DatagramPacket(buffer,
				buffer.length, server_address, port_number);
		datagram_socket.send(sent_packet);
		byte[] received_message_buffer = new byte[1000];
		DatagramPacket received_message = new DatagramPacket(
				received_message_buffer, 1000);
		datagram_socket.receive(received_message);
		
		String server_reply = new String(
				received_message_buffer, 0,
				received_message_buffer.length, "US-ASCII");
		
		System.out.println(server_reply);
		datagram_socket.close();
	}
}