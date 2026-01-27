package com.crm.smart_CRM.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

}
