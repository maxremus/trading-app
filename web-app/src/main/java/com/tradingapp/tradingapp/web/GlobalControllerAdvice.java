package com.tradingapp.tradingapp.web;


import com.tradingapp.tradingapp.error.ProductNotFoundException;
import com.tradingapp.tradingapp.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private CustomerService customerService;

    @ExceptionHandler(IllegalStateException.class)
    public ModelAndView handleIllegalStateException(IllegalStateException ex) {

        ModelAndView mv = new ModelAndView("error");
        mv.addObject("statusCode", 400);
        mv.addObject("errorMessage", ex.getMessage());

        return mv;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ModelAndView handleResponseStatus(ResponseStatusException ex) {

        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorMessage", ex.getReason());
        mav.addObject("statusCode", ex.getStatusCode().value());

        return mav;
    }

    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleRuntimeException(RuntimeException ex) {

        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("statusCode", 500);

        return mav;
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ModelAndView handleProductNotFound(ProductNotFoundException ex) {

        ModelAndView mav = new ModelAndView("error");
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("statusCode", 404);

        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex) {

        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("errorMessage", ex.getMessage());
        modelAndView.addObject("statusCode", 500);

        return modelAndView;
    }
}
