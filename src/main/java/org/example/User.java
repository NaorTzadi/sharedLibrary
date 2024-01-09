package org.example;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

public class User {
    private String username;
    private String password;
    private String salt;
    private List<File> personalData;
    private List<File> customData;
    private boolean isMember;

    public User(String username, String password, List<File> personalData, List<File> customData, boolean isMember) {
        this.username = username;
        this.salt = generateSalt();
        this.password = hashPassword(password, this.salt); // Hash the password before storing it
        this.personalData = personalData;
        this.customData = customData;
        this.isMember = isMember;
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
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

    public void setPassword(String newPassword) {this.password = hashPassword(newPassword, this.salt);}
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }

    public List<File> getPersonalData() {
        return personalData;
    }

    public void setPersonalData(List<File> personalData) {
        this.personalData = personalData;
    }

    public List<File> getCostumeData() {
        return customData;
    }

    public void setCostumeData(List<File> costumeData) {
        this.customData = costumeData;
    }

    public boolean isMember() {
        return isMember;
    }

    public void setMember(boolean member) {
        isMember = member;
    }

    public String getSalt() {return salt;}

    public void setSalt(String salt) {this.salt = salt;}
}