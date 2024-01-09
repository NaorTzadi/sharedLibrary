package org.example;
import com.google.gson.Gson;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class UserManager {
    private static UserManager instance = new UserManager();
    private final List<User> users; // כנראה נוריד אם שומרים על הפרטים ב-server
    //נשאיר רק אובייקט משתמש אחד וזה של המשתמש הנוכחי בכדי לא כל פעם להביא אותו מה-server
    private User currentUser=null;

    private UserManager() {
        this.users = new ArrayList<>();
    }

    public static UserManager getInstance() {
        return instance;
    }

    public synchronized void addUser(String username, String password) {
        List<File> personalFiles = new ArrayList<>();
        List<File> customFiles = new ArrayList<>();
        boolean isMember = false;

        // Hashing the password before sending it to the server
        User newUser = new User(username, password, personalFiles, customFiles, isMember);
        currentUser=newUser;
        users.add(newUser);

        sendUserCredentialsToServer(username, password);
        sendAdditionalUserDataToServer(personalFiles, customFiles, isMember);
    }

    public synchronized boolean checkIfUsernameAlreadyExists(String username) {
        for (User existingUser : users) {
            if (existingUser.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean confirmUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                String salt = user.getSalt();  // Assume User class now has this method
                String hashedPassword = hashPassword(password, salt);  // Salt is included
                if (user.getPassword().equals(hashedPassword)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest((password + salt).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    private void sendUserCredentialsToServer(String username, String password) {
        String credentials = "CREDENTIALS:" + username + ":" + password;
        sendDataToServer(credentials);
    }
    private void sendDataToServer(String data) {
        try (Socket socket = new Socket("localhost", 5050);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            out.writeUTF(data);

        } catch (IOException e) {
            e.printStackTrace();
            // Add more robust error handling here
        }
    }


    private void sendAdditionalUserDataToServer(List<File> personalData, List<File> customData, boolean isMember) {
        Gson gson = new Gson();
        String personalDataStr = gson.toJson(personalData);
        String customDataStr = gson.toJson(customData);
        String isMemberStr = Boolean.toString(isMember);

        String additionalData = "ADDITIONAL_DATA:" + personalDataStr + ":" + customDataStr + ":" + isMemberStr;
        sendDataToServer(additionalData);
    }


    public User getUser(String username) {
        for (User user : this.users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }


}
