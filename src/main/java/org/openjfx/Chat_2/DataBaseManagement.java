package org.openjfx.Chat_2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBaseManagement {
	private Statement statement;
	private Connection connection;
	private boolean isCreateTable=false;//Перменная которая фиксирует создалась ли таблица в Бд
	
	public DataBaseManagement() throws SQLException {
		connection = ConnectionToDataBase.CONNECT.getConnectToDataBase();
		statement = connection.createStatement();
	}
	
	public void stopConnect() throws SQLException {
		connection.close();
	}
	
	public void createTable() throws SQLException {
		if(isCreateTable==false) {//Если таблица таблицы не существует, пропускает и создает её
			statement.executeUpdate("CREATE TABLE clients ( idClients int primary key auto_increment, nameClients varchar(10), arrivalTime datetime)");
		}
		isCreateTable = true;//Фиксирует наличие созданной таблицы
	}
	
	public void addClient(ClientManagement clientManagement) throws SQLException {
		statement.executeUpdate("INSERT clients(nameClients, arrivalTime) VALUES ('"+clientManagement.getName()+"', '"+clientManagement.getLocalDateTime()+"')");
	}
	
	public String getAllClients() throws SQLException {
		ResultSet resultSet_Clients = statement.executeQuery("SELECT * from clients");
		String setClients="";
		while(resultSet_Clients.next()) {
			setClients=setClients+resultSet_Clients.getString(2)+" "+resultSet_Clients.getDate(3)+" \n";	
		}
		if(setClients=="") {
			setClients = "У Базі данних немає записів";
		}
		return "Список клиентів у Базі данних: \n"+setClients;
	}
	
	
	public void deleteClient(ClientManagement clientManagement) throws SQLException {
		statement.executeUpdate("DELETE FROM clients WHERE nameClients = '"+clientManagement.getName()+"'");
	}
	
	public void deleteTable() throws SQLException {
		if(isCreateTable==true) {//Если таблица создана тогда произведеться удаление, если нет, тогда нечего не произойдет
			statement.executeUpdate("TRUNCATE TABLE clients");
			statement.executeUpdate("DROP TABLE clients");
		}
		isCreateTable = false;//Переключает состояние переменной потому как таблица удалилась и её уже не существует, что бы можно было создать снова
	}
}
