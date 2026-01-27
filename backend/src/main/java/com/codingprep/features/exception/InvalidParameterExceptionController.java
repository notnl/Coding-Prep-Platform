package com.codingprep.features.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

 @ResponseStatus(value=HttpStatus.NOT_FOUND, reason="No such Parameter exists")  
 public class InvalidParameterExceptionController extends RuntimeException {

   public InvalidParameterExceptionController() {


   }



 }

