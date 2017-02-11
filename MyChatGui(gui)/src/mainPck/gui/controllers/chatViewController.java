package mainPck.gui.controllers;

import javafx.concurrent.Task;
import javafx.fxml.*;
import java.io.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mainPck.Main;

public class chatViewController {
	
	@FXML
	private Button logoffButton;
	@FXML
	private TextField usertextField;
	@FXML
	private TextArea chatPlace;
	
	public static Task<Void> extraThread;//Task дл¤ потока чата-читалки из сервера
	private static PrintWriter out;
	private static BufferedReader in;
	
	public static void getStream(PrintWriter iout, BufferedReader iin){
		out=iout;
		in=iin;
	}
	
	public void logoff(){
		Main.closeStreams();
		Main.setLoginScene();
	}
	
	@FXML
	private void initialize(){
		startScanner();//запускаем сканирование чата в отдельном потоке
		//chatPlace.appendText(":::Server encoding is "+Main.ENCODING+":::\n");
		usertextField.setOnAction((e)-> {
			send(); 
			}   
		);
			
		
		logoffButton.setOnAction((e)->{
			out.println("\\exit");
			logoff();
		});
		
	}//end of initialize() method
	
	
	
	private void startScanner(){
		try {
			extraThread = new Task <Void>(){	
				protected Void call(){
					try {
						String fromserver;
						while (  (fromserver=in.readLine())!=null ){
							chatPlace.appendText(fromserver+"\n");
						}
						chatPlace.appendText("Server connection lost!");
					}catch (Exception e){
						chatPlace.appendText("Server connection lost: "+e);
					}
					return null;
				}
			};
			new Thread(extraThread).start();
		}catch (Exception e){
			System.out.println(e);
		}
	}//end of startScanner()
	
	private void send(){//ћетод дл¤ отсылки сообщений, чтобы дважды не писать в л¤мбде
		try{
			
			String message=usertextField.getText();
			
			if (message!=null){
				if (message.equals("\\exit")) {
					out.println("\\exit");
					logoff();
				}
				if (message.length()>0) out.println(message);
				usertextField.setText(null);
			}
		} catch (NullPointerException e){usertextField.setText("");}
	}//end of send()
	
	
	
	
	
}//end of chatViewController;
