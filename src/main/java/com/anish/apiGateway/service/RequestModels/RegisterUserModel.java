package com.anish.apiGateway.service.RequestModels;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserModel {

    private String userName;
    private String firstName;
    private String lastName;
    private String emailId;
    private String dateOfBirth;
    private String mobileNumber;
    private String password;
}
