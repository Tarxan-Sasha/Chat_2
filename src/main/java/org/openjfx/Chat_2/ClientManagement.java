package org.openjfx.Chat_2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;


public class ClientManagement extends Application implements Runnable{
	private static Socket clientSocket;
	private static BufferedReader reader;
	private static BufferedWriter writer;
	private static String name;
	private static TextArea textAreaOutput;
	private static TextField textFieldInput;
	private static Button btn1;
	private static boolean isShadowClient;//Теневой ли клиент
	private static boolean isExit;//Вышел ли клиент
	//Переменные для работы с БД
	private static DataBaseManagement dataBaseManagement;
	private static LocalTime localTime;
	
	public ClientManagement() {
	}
	public ClientManagement(String name) {
		this.name = name;
	}
	public ClientManagement(boolean shadowClient) {
		this.isShadowClient = shadowClient;
	}
	
	@Override
	public void run() {
		if(isShadowClient==true) {
			startClient();
		}else {
			launch();
		}
	}
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		textAreaOutput = new TextArea();
		textAreaOutput.setMaxWidth(300);
		textAreaOutput.setMaxHeight(300);
		textAreaOutput.setWrapText(true);
		textAreaOutput.setEditable(false);
		
		FlowPane fpTextArea = new FlowPane(textAreaOutput);//Первый блок с полем вывода
		FlowPane.setMargin(fpTextArea, new Insets(5, 0, 0, 20));
		
		textFieldInput = new TextField();
		textFieldInput.setPrefWidth(215);
		btn1 = new Button("Отправить");
		
		FlowPane fpInputFields = new FlowPane(Orientation.HORIZONTAL,10,0,textFieldInput, btn1);//Второй Блок с кнопкой и поелм для ввода
		FlowPane.setMargin(fpInputFields, new Insets(5, 0, 0, 20));
		
		FlowPane fpTextArea_InputFields = new FlowPane(fpTextArea, fpInputFields);//Соединение блоков в один
		
		Scene scene = new Scene(fpTextArea_InputFields);
		
		primaryStage.setScene(scene);
		primaryStage.setTitle(name);
		primaryStage.setMinWidth(500);
		primaryStage.setMinHeight(300);
		primaryStage.setMaxWidth(500);
		primaryStage.setMaxHeight(300);
		
		primaryStage.show();
		
		primaryStage.setOnCloseRequest(e ->{//Что произойдет если нажать на крестик
			if(isExit == false) {
				writeToServer("Exit");
			}
			stopClient();
		});
		
		startClient();
		
	}
	
	private void startClient() {
		try {
			clientSocket = new Socket("localhost", 8000);
			System.out.println(name + ", впишіть будь яке речення чи слово");
			if(isShadowClient==true) {//Проверяет являеться ли клиент теневым, если ДА сразу вырубает соединение не создавая нечего лишнего
				clientSocket.close();
			}else {
				//Проверяет есть ли соединение и теневой ли клиент, если подключение есть и клиент НЕ теневой, тогда он добавляеться в БД
				if(ConnectionToDataBase.CONNECT.isConnectToDataBase()) {
					dataBaseManagement = new DataBaseManagement();
					dataBaseManagement.addClient(this);//Добавление клиента в БД
					dataBaseManagement.stopConnect();//остановвка соединения с БД
				}
				
				reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
	
				WriteMsg wm = new WriteMsg();
				wm.start();// Запуск процесса записи
				ReadMsg rm = new ReadMsg();
				rm.start();// Запуск процесса чтения
			}
		} catch (IOException | SQLException e) {
			stopClient();
			e.printStackTrace();
		}
	}	

	// Метод що зупняє Клиент
	private static void stopClient() {
		try {
			reader.close();
			writer.close();
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// Метод отправляет сообщение
	private void writeToServer(String message) {
		try {
			//Если сообщение это "Вихід" или "Exit" тогда отправляет только это сообщение, нечего лишнего не отправляет
			if (message.equals("Вихід") || message.equals("Exit")) {
				writer.write(message+"\n");
				writer.flush();
			//Если сообщение не соответсвует тому сверху, тогда к обычному сообщению добавляетсья еще и имя ионо спокойно отправляеться
			} else {
				writer.write(name + ": " + message + "\n");
				writer.flush();
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public String getName() {
		return name;
	}
	//Метод предает локальное время час:минута:секунда для БД
	public String getLocalDateTime() {
		LocalDateTime localDateTime = LocalDateTime.now();
		return localDateTime.getYear()+"-"+localDateTime.getMonthValue()+"-"+localDateTime.getDayOfMonth()+" "+localDateTime.getHour()+":"+localDateTime.getMinute()+":"+localDateTime.getSecond();
	}
	
	// внутрішній клас для читання за серверу(з чату)
	private class ReadMsg extends Thread {
		private String msg;
		private String lastMsg = "Початок діалогу ";

		@Override
		public void run() {

			while (true) {
				try {
					msg = reader.readLine();
					localTime = LocalTime.now();//Получаем текущее время час минута и секунда
					lastMsg = lastMsg + "\n"+msg+"   "+localTime.getHour()+":"+localTime.getMinute();//собираем все в кучу
					textAreaOutput.setText(lastMsg);
					// System.out.println(msg); //отображение на консоле
					if (msg.equals("Сервер закончил работу")) {//Получает от сервера "Exit", это сообщение должно отличатсья от "Вы вышли", для того что бы сработал код снизу и клиент отправил "Exit" обратно.
						writeToServer("Exit");//Отправляю обратно для того что бы в случае если сервер закрылся и посылает Exit, клиент его возвращает и срабатывает закрытие.
						break;
					}
					if(msg.equals("Вы вышли")) {//Это уже то что сервер посылает окончательно после выхода из сервера
						break;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

	}

	// Внутренний класс который записывает сообщение на сервер
	private class WriteMsg extends Thread {
		private String words;//Слова которые пишутся клиентом и отправляються
		@Override
		public void run() throws NullPointerException {
			
			while (true) {
				btn1.setOnAction( e -> {//Действие кнопки "Отправить"					
					words = textFieldInput.getText();// получение с поля во фрейме
					textFieldInput.deleteText(0, words.length());
					writeToServer(words);

				});
				if ((words.equals("Вихід") || words.equals("Exit"))) {
						System.out.println(12);
						stopClient();
						break;
				}
			}

		}
		
	}

}
