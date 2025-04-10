package com.courseServer.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors; // For basic encryption
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Service
public class SecurityService {

    private final PasswordEncoder passwordEncoder;
    private final TextEncryptor textEncryptor;
    private final SecretKey jwtSecretKey;
    private final long jwtExpirationMs;

    @Autowired
    public SecurityService(
            PasswordEncoder passwordEncoder,
            // Inject properties from application.properties
            @Value("${app.security.encryption.password}") String encryptionPassword,
            @Value("${app.security.encryption.salt}") String encryptionSalt, // Hex-encoded salt
            @Value("${app.security.jwt.secret}") String jwtSecret,
            @Value("${app.security.jwt.expirationMs}") long jwtExpirationMs) {

        this.passwordEncoder = passwordEncoder;

        // --- Basic Text Encryption Setup ---
        // Uses AES/GCM with PKCS5Padding. Requires a password and a hex-encoded salt.
        // NOTE: For highly sensitive data, consider more robust key management strategies.
        // This is a basic example suitable for non-critical data encryption within the app.
        this.textEncryptor = Encryptors.delux(encryptionPassword, encryptionSalt);

        // --- JWT Setup ---
        // Decode the base64 encoded secret from properties
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        // Ensure the key size is appropriate for the algorithm (HS256 needs >= 256 bits)
         if (keyBytes.length < 32) {
             throw new IllegalArgumentException("JWT Secret key must be at least 32 bytes (256 bits) long for HS256");
         }
        this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes); // Use HS256 signing algorithm
        this.jwtExpirationMs = jwtExpirationMs;
    }

    // --- Hashing ---

    /**
     * Hashes a plain text password using the configured PasswordEncoder (BCrypt).
     * @param rawPassword The plain text password.
     * @return The hashed password string (including salt).
     */
    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Verifies if a plain text password matches a stored hashed password.
     * @param rawPassword The plain text password attempt.
     * @param hashedPassword The stored hashed password.
     * @return true if the passwords match, false otherwise.
     */
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    // --- Basic Encryption/Decryption ---

    /**
     * Encrypts a plain text string using the configured TextEncryptor (AES/GCM).
     * @param plainText The text to encrypt.
     * @return The encrypted text (usually hex-encoded).
     */
    public String encryptText(String plainText) {
        return textEncryptor.encrypt(plainText);
    }

    /**
     * Decrypts a previously encrypted string.
     * @param encryptedText The encrypted text.
     * @return The original plain text.
     * @throws RuntimeException if decryption fails (e.g., wrong key, tampered data).
     */
    public String decryptText(String encryptedText) {
        try {
            return textEncryptor.decrypt(encryptedText);
        } catch (Exception e) {
            // Log the error appropriately in a real application
            System.err.println("Decryption failed: " + e.getMessage());
            throw new RuntimeException("Could not decrypt data", e);
        }
    }

    // --- JWT Generation ---

    /**
     * Generates a JWT for a given subject (typically username).
     * @param subject The subject to include in the token.
     * @return The generated JWT string.
     */
    public String generateJwtToken(String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(subject) // Set the subject (e.g., username)
                .issuedAt(now) // Set the issued time
                .expiration(expiryDate) // Set the expiration time
                .signWith(jwtSecretKey) // Sign with the secret key using HS256
                .compact(); // Build the token string
    }

    // --- JWT Validation/Parsing ---

    /**
     * Extracts the subject (e.g., username) from a JWT token.
     * Validates the token's signature and expiration.
     * @param token The JWT string.
     * @return The subject extracted from the token.
     * @throws io.jsonwebtoken.JwtException if the token is invalid, expired, or cannot be parsed.
     */
    public String getSubjectFromJwtToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from a JWT token.
     * @param token The JWT string.
     * @return The expiration Date.
     * @throws io.jsonwebtoken.JwtException if the token cannot be parsed.
     */
    public Date getExpirationDateFromToken(String token) {
         return getClaimFromToken(token, Claims::getExpiration);
     }

     /**
     * Checks if a JWT token is expired.
     * @param token The JWT string.
     * @return true if the token is expired, false otherwise.
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
    * Validates a JWT token: checks signature and expiration.
    * @param token The JWT string.
    * @return true if the token is valid, false otherwise.
    */
   public Boolean validateToken(String token) {
       try {
            // Jwts.parser() automatically validates signature and expiration
            Jwts.parser().verifyWith(jwtSecretKey).build().parseSignedClaims(token);
            return true;
       } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
           System.err.println("JWT validation error: " + e.getMessage());
           // Log specific errors: MalformedJwtException, ExpiredJwtException, UnsupportedJwtException, IllegalArgumentException
       }
       return false;
   }

    /**
     * Generic function to extract a specific claim from a token.
     * @param token The JWT string.
     * @param claimsResolver A function to apply to the Claims object.
     * @param <T> The type of the claim to extract.
     * @return The extracted claim.
     * @throws io.jsonwebtoken.JwtException if the token cannot be parsed.
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the token and returns all claims.
     * Note: This method performs signature validation.
     * @param token The JWT string.
     * @return The Claims object.
     * @throws io.jsonwebtoken.JwtException if the token cannot be parsed or validated.
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(jwtSecretKey) // Specify the key used for verification
                .build()
                .parseSignedClaims(token) // Parse and validate the token
                .getPayload(); // Get the claims part
    }
} 