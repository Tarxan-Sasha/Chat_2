package org.openjfx.Chat_2;

public class ClientOne {
	
	public static void main(String[] args) {

		ClientManagement clientManagement = new ClientManagement("Alina");
		Thread t = new Thread(clientManagement);
		t.start();
			
		
	}
}
