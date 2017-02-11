package mainPck;
import java.util.Scanner;
import clientPck.Client;
import serverPck.Server;
import java.io.IOException;

public class MainClass {
	public static void main (String[] args) throws IOException{

		Scanner sc = new Scanner (System.in);
		String menu="";
		System.out.println("Input 1 for Client, 2 for Server and 0 for exit:");
		while (true){
			String menuS = sc.nextLine();
			int menuI=Integer.parseInt(menuS);
			if (menuI==1){
				new Client();
			}

			if (menuI==2) {
				new Server();
			}

			if (menuI==0){
				System.exit(0);
			}
		}
	}//end of main method
}//end of class