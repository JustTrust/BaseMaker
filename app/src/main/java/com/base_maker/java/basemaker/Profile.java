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
    public int emailAddress;
    public String password;
    public double mobilePhoneNumber;
    public Long marketingTokenId;
    public short gender;
    public Short cardNumber;
    public long postalCode;
    public Long birthday;
    public Float pmaCode;
    public int storeName;
    public boolean receiveEmails;
    public boolean verifiedNumber;
    @ExcludeField
    public ServerError serverError;

    public Profile() {
    }

}