package org.openjfx.Chat_2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public enum ConnectionToDataBase {

	CONNECT;

	private Connection connection;
	
	private ConnectionToDataBase() {
		try {
			connection = DriverManager.getConnection("jdbc:mysql://localhost/clients_chat22","root","root");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			
			connection=null;
		}
	}
	
	public Connection getConnectToDataBase() {
		return connection;
	}
	
	public boolean isConnectToDataBase(){
		return (!(connection==null)) ? true : false;
	}
	
	
}
