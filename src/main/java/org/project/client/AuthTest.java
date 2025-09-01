package org.project.client;

import org.project.models.User;
import org.project.server.db.UserDAO;

import java.util.Scanner;

public class AuthTest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserDAO userDAO = new UserDAO();

        System.out.println("📲 Welcome to Telegram System");
        System.out.println("=============================");
        System.out.println("1) Login");
        System.out.println("2) Sign Up");
        System.out.print("Choose option: ");
        int choice = Integer.parseInt(scanner.nextLine());

        switch (choice) {
            case 1:
                System.out.print("Username: ");
                String username = scanner.nextLine();

                System.out.print("Password: ");
                String password = scanner.nextLine();

                User loggedIn = userDAO.login(username, password);
                if (loggedIn != null) {
                    System.out.println("✅ Login successful. Welcome " + loggedIn.getUsername() + "!");
                    System.out.println("Display name: " + loggedIn.getProfileName());
                } else {
                    System.out.println("❌ Login failed. Wrong credentials.");
                }
                break;

            case 2:
                System.out.print("Choose username: ");
                String newUsername = scanner.nextLine();

                if (userDAO.usernameExists(newUsername)) {
                    System.out.println("⚠️ Username already exists. Try another one.");
                    break;
                }

                System.out.print("Choose password: ");
                String newPassword = scanner.nextLine();

                System.out.print("Enter your display name: ");
                String profileName = scanner.nextLine();

                User newUser = new User(newUsername, newPassword, profileName);
                boolean registered = userDAO.registerUser(newUser);

                if (registered) {
                    System.out.println("✅ Sign Up successful! You can now login.");
                } else {
                    System.out.println("❌ Sign Up failed. Please try again later.");
                }
                break;

            default:
                System.out.println("🚫 Invalid option.");
        }

        scanner.close();
    }
}
