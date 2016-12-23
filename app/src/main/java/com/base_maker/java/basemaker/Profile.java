package com.base_maker.java.basemaker;

import com.example.ExcludeField;
import com.example.MakeDbFromClass;

/**
 * Created by Belichenko Anton on 08.12.16.
 * mailto: a.belichenko@gmail.com
 */

@MakeDbFromClass
public class Profile {

    public transient String firstName;
    public String lastName;
    public String emailAddress;
    public String password;
    public Double mobilePhoneNumber;
    public Long marketingTokenId;
    public String gender;
    public String cardNumber;
    public String postalCode;
    public String birthday;
    public String pmaCode;
    public String storeName;
    public boolean receiveEmails;
    public boolean verifiedNumber;
    @ExcludeField
    public ServerError serverError;

    public Profile() {
    }

}