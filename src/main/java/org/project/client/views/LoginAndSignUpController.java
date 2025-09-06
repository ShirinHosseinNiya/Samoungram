package org.project.client.views;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.project.client.NetworkClient;
import org.project.models.Packet;
import org.project.models.PacketType;

public class LoginAndSignUpController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField profileNameField;
    @FXML private Button loginButton;
    @FXML private Button signUpButton;
    @FXML private Label statusLabel;
    @FXML private Button toggleModeButton;

    private NetworkClient networkClient;
    private boolean isSignUpMode = false;

    public void initialize() {
        networkClient = new NetworkClient();
        try {
            networkClient.connect();
        } catch (Exception e) {
            statusLabel.setText("Failed to connect to server.");
            e.printStackTrace();
        }
        updateModeUI();
    }

    private void updateModeUI() {
        if (isSignUpMode) {
            profileNameField.setVisible(true);
            profileNameField.setManaged(true);
            loginButton.setVisible(false);
            loginButton.setManaged(false);
            signUpButton.setVisible(true);
            signUpButton.setManaged(true);
            toggleModeButton.setText("Already have an account? Login");
            statusLabel.setText("");
        } else {
            profileNameField.setVisible(false);
            profileNameField.setManaged(false);
            loginButton.setVisible(true);
            loginButton.setManaged(true);
            signUpButton.setVisible(false);
            signUpButton.setManaged(false);
            toggleModeButton.setText("New User? Register");
            statusLabel.setText("");
        }
    }

    @FXML
    void toggleMode(ActionEvent event) {
        isSignUpMode = !isSignUpMode;
        updateModeUI();
    }

    @FXML
    void handleLoginButtonAction(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password cannot be empty.");
            return;
        }

        setButtonsDisabled(true);
        statusLabel.setText("Attempting to log in...");

        Task<Void> loginTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Packet loginPacket = new Packet(PacketType.LOGIN);
                loginPacket.setContent(username + ";" + password);
                networkClient.sendPacket(loginPacket);

                Packet response = networkClient.getReceivedPacket();

                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        statusLabel.setText("Login Successful!");
                        // TODO: Close login window and open main chat window
                    } else {
                        statusLabel.setText(response != null ? response.getErrorMessage() : "Login failed: No response.");
                        setButtonsDisabled(false);
                    }
                });
                return null;
            }
        };

        new Thread(loginTask).start();
    }

    @FXML
    void handleSignUpButtonAction(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String profileName = profileNameField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || profileName.isEmpty()) {
            statusLabel.setText("All fields are required for registration.");
            return;
        }

        setButtonsDisabled(true);
        statusLabel.setText("Attempting to register...");

        Task<Void> signUpTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Packet signUpPacket = new Packet(PacketType.SIGN_UP);
                signUpPacket.setContent(username + ";" + password + ";" + profileName);
                networkClient.sendPacket(signUpPacket);

                Packet response = networkClient.getReceivedPacket();

                Platform.runLater(() -> {
                    if (response != null && response.isSuccess()) {
                        statusLabel.setText("Registration Successful! Please log in.");
                        isSignUpMode = false;
                        updateModeUI();
                    } else {
                        statusLabel.setText(response != null ? response.getErrorMessage() : "Registration failed: No response.");
                        setButtonsDisabled(false);
                    }
                });
                return null;
            }
        };

        new Thread(signUpTask).start();
    }

    private void setButtonsDisabled(boolean disabled) {
        loginButton.setDisable(disabled);
        signUpButton.setDisable(disabled);
        toggleModeButton.setDisable(disabled);
        usernameField.setDisable(disabled);
        passwordField.setDisable(disabled);
        profileNameField.setDisable(disabled);
    }
}