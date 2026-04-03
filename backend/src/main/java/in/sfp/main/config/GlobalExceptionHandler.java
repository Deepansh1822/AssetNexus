package in.sfp.main.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(HttpServletRequest request, Exception ex) {
        String uri = request.getRequestURI();
        
        // Suppress browser and extension noise for a cleaner terminal
        boolean isNoise = uri.startsWith("/.well-known") || 
                         uri.contains("favicon.ico") || 
                         uri.endsWith(".map") || 
                         uri.startsWith("/chrome-extension");
                         
        if (!isNoise) {
            System.err.println(">>>> DETECTED ERROR AT: " + request.getRequestURL());
            System.err.println(">>>> ERROR MESSAGE: " + ex.getMessage());
        }
        
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Error");
        body.put("message", ex.getMessage());
        body.put("path", uri);
        body.put("status", HttpStatus.BAD_REQUEST.value());
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
