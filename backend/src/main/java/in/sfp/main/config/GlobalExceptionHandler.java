package in.sfp.main.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Object handleException(HttpServletRequest request, Exception ex) {
        System.err.println(">>>> DETECTED ERROR AT: " + request.getRequestURL());
        System.err.println(">>>> ERROR MESSAGE: " + ex.getMessage());
        // ex.printStackTrace(); // Optional: Uncomment for dev debugging
        
        // Check if it's an API call
        if (request.getRequestURI().startsWith("/api/")) {
            Map<String, Object> body = new HashMap<>();
            body.put("error", "Error");
            body.put("message", ex.getMessage()); // Pass the exception message to frontend
            body.put("path", request.getRequestURI());
            // Using BAD_REQUEST (400) because these are likely validation errors
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", ex);
        mav.addObject("url", request.getRequestURL());
        mav.setViewName("error");
        return mav;
    }
}
