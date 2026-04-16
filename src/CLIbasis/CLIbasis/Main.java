package CLIbasis.CLIbasis;

import Users.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
	private final static ArrayList<User> userList;

	static {
		try {
			userList = User.loadUsers();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	public static void main(String[] args) throws IOException {

        Scanner consoleInput = new Scanner(System.in);


		System.out.println("WELCOME");
        
        while (true) {
            printWelcomeMenu();

            String line = readLine(consoleInput);
			int selection = 0;
			// Checks if the user has entered anything before parsing their input
			if (line == null){
				System.out.println("Please enter a valid option");
			} else {
				try {
					selection = Integer.parseInt(line.trim());
				} catch (NumberFormatException e){
					System.out.println("Please enter a valid option");
					selection = -1;
				}
			}

			if (selection == -1){
				continue;
			}
			if (selection == 0) {
				System.out.println("Goodbye");
				System.out.println("Closing program...");
				System.out.println();
				consoleInput.close();
				return;
			} else {
				if (selection <= userList.size() && selection > 0) {
					String userRole = userList.get(selection-1).getRole();
					if (userRole.equalsIgnoreCase("admin")) {
						Admin admin = (Admin) userList.get(selection-1);
						AdminCLI.run(consoleInput, admin);
					} else {
						Customer customer = (Customer) userList.get(selection);
						CustomerCLI.run(consoleInput, customer);
					}
				} else {
					System.out.println("Invalid selection");
				}
			}
        }
    }

    private static void printWelcomeMenu() {

		//
        System.out.println("PLEASE SELECT USER BY INPUTTING THE CORRESPONDING NUMBER (or 0 for exit)");
		int i = 1;
		for  (User user : userList) {
			String line = String.format("%d) %s", i, user.toString());
			System.out.println(line);
			i++;
		}


        System.out.println("0) Exit");
    }

    private static String readLine(Scanner consoleInput) {
        if (!consoleInput.hasNextLine()) {
            return null;
        }
        return consoleInput.nextLine();
    }
}