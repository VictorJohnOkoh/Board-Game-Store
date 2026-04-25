package CLIbasis.CLIbasis;

import Users.Admin;

import java.io.IOException;
import java.util.Scanner;

public class AdminCLI {
	public final static String INVALID = "Invalid input";

    public static void run(Scanner consoleInput, Admin admin) throws IOException {
    	System.out.println("ADMIN VIEW");


        
        while (true) {
        	printAdminMenu();

        	int selection = Integer.parseInt(consoleInput.nextLine().trim());
        	
        	switch (selection) {
        		case 1:
					System.out.println("ProdID; Prod Category; Type; Name; Price; Stock; Purchase Cost; Extra");
        			System.out.println(admin.viewProducts());
        			System.out.println();
        			break;
        		
        		case 2:
					System.out.println();
					System.out.println("1) Add a board game");
					System.out.println("2) Add an accessory");
					System.out.println("PRESS ANY OTHER NUMBER BUTTON TO CANCEL");
					int choice = Integer.parseInt(consoleInput.nextLine());
					switch (choice) {
						case 1:
							System.out.println();
							admin.addBoardGame(consoleInput);
							break;
						case 2:
							System.out.println();
							admin.addAccessory(consoleInput);
							break;
						default:
							break;

					}
        			System.out.println();
        			break;
        			
        		case 0:
        			return;
        			
        		default:
        			System.out.println(INVALID);
        			System.out.println();
        	}
        }
    }
    
    
    private static void printAdminMenu() {
        System.out.println("PLEASE SELECT ACTION BY INPUTTING THE CORRESPONDING NUMBER (or 0 for logout)");
        System.out.println("1) View all products");
        System.out.println("2) Add new product");
        

        System.out.println("0) Log out");
    }
}