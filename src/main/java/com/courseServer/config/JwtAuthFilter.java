package com.courseServer.config; // Place in config package

import com.courseServer.services.SecurityService;
import com.courseServer.services.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Custom filter that intercepts requests once per request to validate JWT tokens.
 * If a valid JWT is found in the Authorization header, it sets the authentication
 * context for Spring Security.
 */
@Component // Make it a Spring bean so it can be injected/autowired
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private SecurityService securityService; // Service to validate/parse JWT

    @Autowired
    private UserDetailsServiceImpl userDetailsService; // Service to load UserDetails

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && securityService.validateToken(jwt)) {
                String username = securityService.getSubjectFromJwtToken(jwt);

                // Load user details from the database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Create an Authentication object
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails,
                                                                null, // Credentials are null for JWT based auth
                                                                userDetails.getAuthorities()); // Get roles/permissions

                // Set details from the request (e.g., IP address, session ID)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the authentication object in Spring Security's context
                // This tells Spring Security that the user is authenticated for this request
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                 // Optional: Log if JWT is missing or invalid, but don't block the request here.
                 // If SecurityContext remains null/unauthenticated, subsequent security checks will deny access if required.
                 // logger.debug("JWT Token is missing, invalid, or expired.");
            }
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            // You might want to send a specific 401 response here if needed immediately
            // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // response.getWriter().write("{\"error\": \"JWT token expired\"}");
            // return;
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        // Continue the filter chain regardless of JWT validation outcome
        // Spring Security will handle authorization based on the SecurityContext
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header (Bearer type).
     * @param request The incoming HTTP request.
     * @return The JWT string or null if not found or not Bearer type.
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Extract token part after "Bearer "
        }

        return null;
    }
} 