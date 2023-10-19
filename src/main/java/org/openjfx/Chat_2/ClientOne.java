package org.openjfx.Chat_2;

public class ClientOne{
	private static String name = "Alina";

	public static void main(String[] args) {

		ClientManagement clientManagement = new ClientManagement();
		Thread t = new Thread(clientManagement);
		t.start();
			
		
	}

	
}
