package com.anish.apiGateway.service.ResponseModels;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationResponseModel {

    private String mobileNumber;
    private String userId;
    private String message;
    private String error;
}
