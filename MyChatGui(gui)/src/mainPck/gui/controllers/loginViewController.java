package mainPck.gui.controllers;

import javafx.scene.control.*;
import mainPck.Main;
import javafx.fxml.*;
import mvcPck.MVC;

public class loginViewController {
	static Main mainApp;
	
	@FXML
	private Label infoLabel;
	@FXML
	private TextField ipField, portField, nicknameField;
	@FXML
	private Button loginButton, defButton;
	
	@FXML
	private void initialize(){ //самое важное - нужно аккуратно обработать всё, что выдает MVC, чтобы залогинить юзера и выдать рабочий сокет
		loginButton.setOnAction((e)->{
			goLogin();
		});//button event
		//Нажатие ентера в полях делает то же, что и нажатие кнопки
		ipField.setOnAction((e)->{goLogin();});
		portField.setOnAction((e)->{goLogin();});
		nicknameField.setOnAction((e)->{goLogin();});
		
		defButton.setOnAction((e)->{
			ipField.setText("185.159.130.98");
			portField.setText("4444");
			
		});//button event
	}//end of initialize() method
	
	void goLogin(){
		String ip = ipField.getText();
		String port = portField.getText();
		String nickname = nicknameField.getText();
		if (ip.length()<9 | port.length()<1 | nickname.length()<1)
			infoLabel.setText("Fill all the fields correctly!");
		else auth(ip, port, nickname);
	}
	
	
	//метод для аутентификации, который даст нам сокет и залогинит
	void auth(String ip, String port, String name){
		String error;
		
		error=MVC.checkIp(ip);
		if ( error!=null ) {
			infoLabel.setText(error);
			return;
		}
		
		error=MVC.checkPort(port);
		if ( error!=null ) {
			infoLabel.setText(error);
			return;
		}
		
		error=MVC.checkSocket();
		if (error!=null) {
			infoLabel.setText(error);
			return;
		}
		
		error=MVC.checkName(name);
		if( error!=null ) {
			infoLabel.setText(error);
			return;
		} 
		
		
		Main.setupStreams(MVC.getSocket());
		Main.setChatScene();
	}
}//end of loginViewController class
