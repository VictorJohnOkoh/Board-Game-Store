package CLIbasis.CLIbasis;

import Users.Address;
import Users.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
	private final static ArrayList<User> userList;

	static {
		try {
			userList = loadUsers();
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
				selection = Integer.parseInt(line.trim());
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
	private static ArrayList<User> loadUsers() throws IOException {
		File userFile = new File("UserAccount.txt");
		List<String> lines = Files.readAllLines(userFile.toPath());
		ArrayList<List<String>> splitlines = new ArrayList<>();
		for (String line : lines) {
			splitlines.add(List.of(line.split(";")));
		}
		ArrayList<User> listedUsers = new ArrayList<>();
		for (List<String> line : splitlines) {
			Address address = new Address(Integer.parseInt(line.get(2).trim()), line.get(3).trim(), line.get(4).trim());
			User newUser;
			if (line.getLast().trim().equals("admin")) {
				newUser = new Admin(Integer.parseInt(line.getFirst().trim()), line.get(1).trim(), address);

			} else {
				newUser = new Customer(Integer.parseInt(line.getFirst().trim()), line.get(1).trim(), address);
			}
			listedUsers.add(newUser);
		}
		return listedUsers;
	}
}