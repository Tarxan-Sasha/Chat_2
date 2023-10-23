package org.openjfx.Chat_2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
/**
 * JavaFX App
 * 
 * 
 * 
 */
public class AppServer extends Application implements Runnable{

	public static int PORT = 8000;
	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	public static List<ServerManagment> listOfClients = new ArrayList<ServerManagment>();
	private static TextArea textArea;
	private static Connection connection;
	private static Thread fxThread;//Поток который будет отвечать за визуал 
	private static Thread tAppServer;//Поток который будет отвечать за сервер
	public static volatile int count = 0;
	
    public static void main(String[] args) {
//    	AppServer appServer = new AppServer();
//		Thread tAppServer = new Thread(appServer);
//    	tAppServer.start();
//		appServer.startServer();

    	launch();
    }
    
    
  @Override
    public void start(Stage stage) {
	  
	  fxThread = Thread.currentThread();
	  
	  Button btn1 = new Button("Старт Сервера");
	  btn1.setPrefWidth(200);
	  btn1.setPrefHeight(100);
	  Button btn2 = new Button("Изменить цвет");
	  btn2.setPrefWidth(200);
	  btn2.setPrefHeight(100);
	  Button btn3 = new Button("Остановить Сервер");
	  btn3.setPrefWidth(200);
	  btn3.setPrefHeight(100);

	  //TilePane tpBTNS = new TilePane(Orientation.VERTICAL,btn1,btn2,btn3);
	  //tpBTNS.setVgap(20);	  
	  FlowPane fpBTNS = new FlowPane(btn1,btn2,btn3);//FlowPane с кнопочками
	  fpBTNS.setOrientation(Orientation.VERTICAL);//Устанавливает вертикальную ориентацию
	  fpBTNS.setVgap(30);//Устанавливает вертикальные отступы между элементами
	  FlowPane.setMargin(fpBTNS, new Insets(50,0,0,4));//Отступ
	  
	  Label label = new Label("Сервер");
	  label.setMaxSize(50, 50);//Максимальный развер label первое значение ширина, второе значение высота
	  textArea = new TextArea();
	  textArea.setPrefWidth(300);//Устанавливает предпочитаемую ширину
	  textArea.setPrefHeight(420);//Устанавливает предпочитаемую висоту
	  textArea.setEditable(false);//Устанавливает можно ли писать в поле или нет
	  textArea.setWrapText(true);//Текст переноситься на новую строчку, после того как доходит края, а не идет и идет и идет...	  
	  
	  FlowPane fpLabel_TextArea = new FlowPane(label, textArea);
	  fpLabel_TextArea.setOrientation(Orientation.VERTICAL);
	  FlowPane.setMargin(fpLabel_TextArea, new Insets(1,0,0,4));//Отступ
	  
	  FlowPane flowPane = new FlowPane(fpLabel_TextArea, fpBTNS);
	  flowPane.setHgap(50);
	  flowPane.setPrefWidth(300);
	  flowPane.setPrefHeight(900);
	  flowPane.setAlignment(Pos.TOP_LEFT);
	  
	  Scene scene = new Scene(flowPane);
	  scene.setFill(Color.AQUA);
	  
	  /*
	   * Запуск сервера
	   */
	  btn1.setOnAction(e -> {
		  AppServer appServer = new AppServer();
		  tAppServer = new Thread(appServer);
		  tAppServer.start();
		  try{
			  connection = DriverManager.getConnection("jdbc:mysql://localhost/clients_chat2","root","root");
			  DataBaseManagement dataBaseManagement = new DataBaseManagement(connection);
			  textArea.appendText("Система хранения данных: База данных \n");
		  }catch( SQLException exc){
			  textArea.appendText("Система хранения данных: Список \n");
		  }
		  
	  });
	  
	  btn2.setOnAction(e -> {

		  
	  });
	  //Нажатие на эту кнопку останавливает сервер
	  btn3.setOnAction(e -> {
		  tAppServer.interrupt();
		  ClientManagement clientManagement = new ClientManagement(true);
		  Thread t = new Thread(clientManagement);
		  t.start();	  
		  
	  });
	  
	  stage.setScene(scene);
	  stage.setTitle("Chat_2");
	  stage.setMaxWidth(700);
	  stage.setMaxHeight(500);
	  stage.setMinWidth(700);
	  stage.setMinHeight(500);
	  
	  stage.show();
	  
	  stage.setOnCloseRequest(e ->{//Что произойдет если нажать на крестик
		  System.exit(0);//Этот метод завершает программу
	  });
	  
    }
  
	@Override
	public void run() {
		 startServer();
	}
  
	public void startServer() {
		try {
			serverSocket = new ServerSocket(PORT);// Встановили порт
			textArea.appendText("Сервер запущений\n");
			textArea.appendText("Чекаю підключення\n");
			while (!(tAppServer.isInterrupted())) {//Проверяет на прерывание потока сервера
				clientSocket = serverSocket.accept();// Чекає підключення
				if(!(tAppServer.isInterrupted())) {//Проверяет на прерывание потока сервера
					try {
						listOfClients.add(new ServerManagment(clientSocket));//Добавляет в колекцию, временная замена БД
						textArea.appendText("Підключень " + ++count + "\n");//Вывод на экран сервера количество подключенных клиентов
						listOfClients.forEach(x -> textArea.appendText(x.getName()+ "\n"));//Вывод на экран сервера всей колекции
					} catch (IOException e) {
						clientSocket.close();
					}
				}	

			}
			textArea.appendText("Сервер закрыт\n");//Отправляет на экран сервера сообщение о его закрытии
			//Отправляет всем клиентам "Exit" что заставляет их остановить диалог
			listOfClients.forEach(x -> {
				try {
					x.send("Exit");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			listOfClients.forEach(x -> listOfClients.remove(x));//чистит колекцию, заменить на БД потом
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				clientSocket.close();
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private static class ServerManagment extends Thread {

		private Socket socket;
		private BufferedReader reader;
		private BufferedWriter writer;

		public ServerManagment(Socket socket) throws IOException {
			this.socket = socket;
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			start();
		}

		@Override
		public void run() {
			String request;
			while (true) {
				try {
					request = reader.readLine();
					//Если приходящее сообщение это "Вихід" и "Exit", тогда сервер отправляет сообщение обратно и вычеркивая клиента из списка, закрывает соединение
					if (request.equals("Вихід") || request.equals("Exit")) {
						send(request);
						textArea.appendText("Закрываемо діалог з клієнтом\n");
						listOfClients.remove(this);
						textArea.appendText("Підключень " + --count +"\n");
						System.out.println(listOfClients);
						stopServer();
						break;
					}
					/*
					 * Если приходящее сообщение не то что выше, тогда сервер отправляет всем пользователям сообщение, 
					 * вместе с именем которое автоматически даеться при написании сообщения в ClientManagement
					 */
					sendToAll(request);
					
				} catch (IOException e) {
					stopServer();
					e.printStackTrace();
				}
			}

		}

		
		//Отправляет сообщение всем
		public void sendToAll(String request)  {
			listOfClients.forEach(x -> {
				try {
					x.send(request);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		}
		
		
		//Отправляет сообщение
		public void send(String request) throws IOException {	
			writer.write(request + "\n");
			writer.flush();
		}

		//Остановка сервера
		private void stopServer() {
			try {
				reader.close();
				writer.close();
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

  
}



