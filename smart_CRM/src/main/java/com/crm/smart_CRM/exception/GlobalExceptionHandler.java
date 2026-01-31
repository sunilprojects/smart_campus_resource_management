package com.crm.smart_CRM.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.crm.smart_CRM.dto.response.ApiResponse;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	public ResponseEntity<ErrorResponse> handleBookingConflict(BookingConflictException ex){
	     ErrorResponse error = new ErrorResponse(
	                HttpStatus.CONFLICT.value(),
	                ex.getMessage(),
	                LocalDateTime.now()
	        );
	     return new ResponseEntity<>(error, HttpStatus.CONFLICT);
	}
	
	 @ExceptionHandler(ValidationException.class)
	    public ResponseEntity<ApiResponse<Object>> handleValidationException(
	            ValidationException ex) {

	        ApiResponse<Object> response = new ApiResponse<>(
	                false,
	                ex.getMessage(),
	                null
	        );

	        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	    }

	    // fallback
	 @ExceptionHandler(Exception.class)
	 public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception ex) {

	     log.error("Unhandled exception occurred", ex); 

	     ApiResponse<Object> response = new ApiResponse<>(
	             false,
	             ex.getMessage(),   // 👈 actual message
	             null
	     );

	     return ResponseEntity
	             .status(HttpStatus.INTERNAL_SERVER_ERROR)
	             .body(response);
	 }


}
