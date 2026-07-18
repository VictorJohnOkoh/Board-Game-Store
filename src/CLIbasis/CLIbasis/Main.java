package CLIbasis.CLIbasis;

import Inventory.JavaPythonBridge;
import Users.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
	private final static List<User> userList = loadUsers();



	static void main(String[] args) throws IOException {

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
					continue;
				}
			}

			
			if (selection == 0) {
				JavaPythonBridge.run(JavaPythonBridge.CLOSE_CONNECTION);
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
						Customer customer = (Customer) userList.get(selection-1);
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

	/** Loads the User accounts in the database into a list of User objects that can be accessed*/
	@SuppressWarnings({"ConstantValue", "DataFlowIssue"})
    private static List<User> loadUsers(){
		List<User> userList = new ArrayList<>(List.of());
		String unparsedData = JavaPythonBridge.run_result(JavaPythonBridge.GET_USER_DETAILS);
		if (unparsedData.isEmpty() || unparsedData == null){
			System.out.println("Failed to load users");
			throw new NullPointerException();
		}
        List<String> userDataList = List.of(unparsedData.split(","));
		for (String userData : userDataList){
			userList.add(User.buildUser(userData));
		}
		return userList;
	}

    private static String readLine(Scanner consoleInput) {
        if (!consoleInput.hasNextLine()) {
            return null;
        }
        return consoleInput.nextLine();
    }
}