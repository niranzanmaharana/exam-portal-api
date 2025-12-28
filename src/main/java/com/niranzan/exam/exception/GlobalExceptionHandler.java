package com.niranzan.exam.exception;

import com.niranzan.exam.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private String getRequestPath(HttpServletRequest request) {
        return request != null ? request.getRequestURI() : null;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.error("Illegal argument exception occurred: {}", ex.getMessage());
        
        String errorMessage = ex.getMessage();
        
        // Provide user-friendly messages for common enum errors
        if (errorMessage != null && errorMessage.contains("No enum constant")) {
            errorMessage = "Invalid value provided. Please check your input.";
        }
        
        String finalMessage = errorMessage != null ? errorMessage : "Please check your input values";
        ErrorResponse errorResponse = new ErrorResponse(
                "Invalid input",
                finalMessage,
                HttpStatus.BAD_REQUEST.value(),
                getRequestPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        log.error("Runtime exception occurred: {}", ex.getMessage());
        
        String errorMessage = ex.getMessage();
        String finalMessage = errorMessage != null ? errorMessage : "Please try again";
        String finalError = errorMessage != null ? errorMessage : "An error occurred";
        
        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        // Check for specific error types
        if (errorMessage != null) {
            if (errorMessage.contains("already exists") || 
                errorMessage.contains("Username already exists") ||
                errorMessage.contains("Email already exists")) {
                status = HttpStatus.CONFLICT;
            } else if (errorMessage.contains("not found") || 
                       errorMessage.contains("User not found")) {
                status = HttpStatus.NOT_FOUND;
            }
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
                finalError,
                finalMessage,
                status.value(),
                getRequestPath(request)
        );
        
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation exception occurred: {}", ex.getMessage());
        
        String errorMessage = ex.getMessage();
        
        if (errorMessage != null) {
            if (errorMessage.contains("Duplicate entry") || errorMessage.contains("unique constraint")) {
                errorMessage = "A record with this information already exists.";
            } else if (errorMessage.contains("foreign key constraint")) {
                errorMessage = "Invalid reference. The referenced record does not exist.";
            } else if (errorMessage.contains("cannot be null")) {
                errorMessage = "Required field is missing. Please fill all required fields.";
            } else {
                errorMessage = "Data validation failed. Please check your input values.";
            }
        } else {
            errorMessage = "Database error occurred. Please try again.";
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
                "Data integrity violation",
                errorMessage,
                HttpStatus.BAD_REQUEST.value(),
                getRequestPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(
            DataAccessException ex, HttpServletRequest request) {
        log.error("Data access exception occurred: {}", ex.getMessage());
        
        String errorMessage = ex.getMessage();
        
        // Check if it's a SQL exception wrapped in DataAccessException
        Throwable rootCause = ex.getRootCause();
        if (rootCause instanceof SQLException) {
            SQLException sqlEx = (SQLException) rootCause;
            errorMessage = sqlEx.getMessage();
            
            if (errorMessage != null) {
                if (errorMessage.contains("Data truncated")) {
                    errorMessage = "Invalid data format. Please check your input values.";
                } else if (errorMessage.contains("Duplicate entry")) {
                    errorMessage = "A record with this information already exists.";
                } else if (errorMessage.contains("Cannot add or update a child row")) {
                    errorMessage = "Invalid reference. The referenced record does not exist.";
                } else if (errorMessage.contains("Column") && errorMessage.contains("cannot be null")) {
                    errorMessage = "Required field is missing. Please fill all required fields.";
                }
            }
        }
        
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = "Database error occurred. Please try again.";
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
                "Database error",
                errorMessage,
                HttpStatus.BAD_REQUEST.value(),
                getRequestPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> handleSQLException(
            SQLException ex, HttpServletRequest request) {
        log.error("SQL exception occurred: {}", ex.getMessage());
        
        // Extract user-friendly error message
        String errorMessage = ex.getMessage();
        if (errorMessage != null) {
            // Simplify common SQL errors
            if (errorMessage.contains("Data truncated")) {
                errorMessage = "Invalid data format. Please check your input values.";
            } else if (errorMessage.contains("Duplicate entry")) {
                errorMessage = "A record with this information already exists.";
            } else if (errorMessage.contains("Cannot add or update a child row")) {
                errorMessage = "Invalid reference. The referenced record does not exist.";
            } else if (errorMessage.contains("Column") && errorMessage.contains("cannot be null")) {
                errorMessage = "Required field is missing. Please fill all required fields.";
            }
        } else {
            errorMessage = "Database error occurred. Please try again.";
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
                "Database error",
                errorMessage,
                HttpStatus.BAD_REQUEST.value(),
                getRequestPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("Validation exception occurred: {}", ex.getMessage());
        
        StringBuilder errors = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            if (errors.length() > 0) {
                errors.append(", ");
            }
            errors.append(fieldName).append(": ").append(errorMessage);
        });
        
        String errorDetails = errors.length() > 0 ? errors.toString() : "Please check the input fields";
        
        ErrorResponse errorResponse = new ErrorResponse(
                "Validation failed",
                errorDetails,
                HttpStatus.BAD_REQUEST.value(),
                getRequestPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        log.error("Authentication exception occurred: {}", ex.getMessage());
        
        String message = ex.getMessage() != null ? ex.getMessage() : "Invalid credentials";
        ErrorResponse errorResponse = new ErrorResponse(
                "Authentication failed",
                message,
                HttpStatus.UNAUTHORIZED.value(),
                getRequestPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {
        log.error("Bad credentials exception occurred: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                "Invalid credentials",
                "Username or password is incorrect",
                HttpStatus.UNAUTHORIZED.value(),
                getRequestPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected exception occurred: {}", ex.getMessage());
        
        String message = ex.getMessage() != null ? ex.getMessage() : "Please try again later";
        ErrorResponse errorResponse = new ErrorResponse(
                "An unexpected error occurred",
                message,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                getRequestPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

