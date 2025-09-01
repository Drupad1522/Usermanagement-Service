package com.userservice.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.regex.Pattern;

@Component
public class PasswordUtil {
    
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;
    
    // Password validation patterns
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    
    public PasswordUtil() {
        this.passwordEncoder = new BCryptPasswordEncoder(12);
        this.secureRandom = new SecureRandom();
    }
    
    public String encodePassword(String rawPassword) {
        validatePassword(rawPassword);
        return passwordEncoder.encode(rawPassword);
    }
    
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    public void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        
        if (password.length() > 128) {
            throw new IllegalArgumentException("Password must be less than 128 characters");
        }
        
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
        
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
    }
    
    public String generateRandomPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8");
        }
        
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        String allChars = uppercase + lowercase + digits + specialChars;
        
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one character from each category
        password.append(uppercase.charAt(secureRandom.nextInt(uppercase.length())));
        password.append(lowercase.charAt(secureRandom.nextInt(lowercase.length())));
        password.append(digits.charAt(secureRandom.nextInt(digits.length())));
        password.append(specialChars.charAt(secureRandom.nextInt(specialChars.length())));
        
        // Fill the rest randomly
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(secureRandom.nextInt(allChars.length())));
        }
        
        // Shuffle the password
        return shuffleString(password.toString());
    }
    
    private String shuffleString(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
    
    public boolean isPasswordStrong(String password) {
        try {
            validatePassword(password);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}