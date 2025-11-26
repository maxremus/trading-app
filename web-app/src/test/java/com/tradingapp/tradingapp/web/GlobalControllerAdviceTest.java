package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.error.ProductNotFoundException;
import com.tradingapp.tradingapp.services.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GlobalControllerAdviceTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private GlobalControllerAdvice globalControllerAdvice;


    @Test
    void handleIllegalStateException_ShouldReturnErrorViewWith400Status() {

        // Given
        String errorMessage = "Customer has orders and cannot be deleted.";
        IllegalStateException exception = new IllegalStateException(errorMessage);

        // When
        ModelAndView result = globalControllerAdvice.handleIllegalStateException(exception);

        // Then
        assertNotNull(result);
        assertEquals("error", result.getViewName());
        assertEquals(400, result.getModel().get("statusCode"));
        assertEquals(errorMessage, result.getModel().get("errorMessage"));
    }

    @Test
    void handleResponseStatusException_ShouldReturnErrorViewWithCorrectStatus() {

        // Given
        String reason = "Resource not found";
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, reason);

        // When
        ModelAndView result = globalControllerAdvice.handleResponseStatus(exception);

        // Then
        assertNotNull(result);
        assertEquals("error", result.getViewName());
        assertEquals(404, result.getModel().get("statusCode"));
        assertEquals(reason, result.getModel().get("errorMessage"));
    }

    @Test
    void handleRuntimeException_ShouldReturnErrorViewWith500Status() {

        // Given
        String errorMessage = "Unexpected error occurred";
        RuntimeException exception = new RuntimeException(errorMessage);

        // When
        ModelAndView result = globalControllerAdvice.handleRuntimeException(exception);

        // Then
        assertNotNull(result);
        assertEquals("error", result.getViewName());
        assertEquals(500, result.getModel().get("statusCode"));
        assertEquals(errorMessage, result.getModel().get("errorMessage"));
    }

    @Test
    void handleProductNotFoundException_ShouldReturnErrorViewWith404Status() {

        // Given
        String errorMessage = "Product not found with id: 123";
        ProductNotFoundException exception = new ProductNotFoundException(errorMessage);

        // When
        ModelAndView result = globalControllerAdvice.handleProductNotFound(exception);

        // Then
        assertNotNull(result);
        assertEquals("error", result.getViewName());
        assertEquals(404, result.getModel().get("statusCode"));
        assertEquals(errorMessage, result.getModel().get("errorMessage"));
    }

    @Test
    void handleException_ShouldReturnErrorViewWith500Status() {

        // Given
        String errorMessage = "Generic exception occurred";
        Exception exception = new Exception(errorMessage);

        // When
        ModelAndView result = globalControllerAdvice.handleException(exception);

        // Then
        assertNotNull(result);
        assertEquals("error", result.getViewName());
        assertEquals(500, result.getModel().get("statusCode"));
        assertEquals(errorMessage, result.getModel().get("errorMessage"));
    }

    @Test
    void handleNullPointerException_ShouldBeHandledByGenericExceptionHandler() {

        // Given
        NullPointerException exception = new NullPointerException("Null pointer");

        // When
        ModelAndView result = globalControllerAdvice.handleException(exception);

        // Then
        assertNotNull(result);
        assertEquals("error", result.getViewName());
        assertEquals(500, result.getModel().get("statusCode"));
        assertEquals("Null pointer", result.getModel().get("errorMessage"));
    }

    @Test
    void getterAndSetter_ForCustomerService_ShouldWorkCorrectly() {

        // Given
        CustomerService newCustomerService = mock(CustomerService.class);

        // When
        globalControllerAdvice.setCustomerServiceImpl(newCustomerService);
        CustomerService result = globalControllerAdvice.getCustomerServiceImpl();

        // Then
        assertNotNull(result);
        assertEquals(newCustomerService, result);
    }

    @Test
    void handleResponseStatusException_WithDifferentStatus_ShouldReturnCorrectStatusCode() {

        // Given
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");

        // When
        ModelAndView result = globalControllerAdvice.handleResponseStatus(exception);

        // Then
        assertNotNull(result);
        assertEquals("error", result.getViewName());
        assertEquals(403, result.getModel().get("statusCode"));
        assertEquals("Access denied", result.getModel().get("errorMessage"));
    }

    @Test
    void handleRuntimeException_WithNullMessage_ShouldHandleGracefully() {

        // Given
        RuntimeException exception = new RuntimeException();

        // When
        ModelAndView result = globalControllerAdvice.handleRuntimeException(exception);

        // Then
        assertNotNull(result);
        assertEquals("error", result.getViewName());
        assertEquals(500, result.getModel().get("statusCode"));
        assertNull(result.getModel().get("errorMessage"));
    }

    @Test
    void handleException_WithComplexException_ShouldReturnCorrectMessage() {

        // Given
        String errorMessage = "Database connection failed";
        Exception rootCause = new RuntimeException("Connection timeout");
        Exception exception = new Exception(errorMessage, rootCause);

        // When
        ModelAndView result = globalControllerAdvice.handleException(exception);

        // Then
        assertNotNull(result);
        assertEquals("error", result.getViewName());
        assertEquals(500, result.getModel().get("statusCode"));
        assertEquals(errorMessage, result.getModel().get("errorMessage"));
    }
}
