package com.base_maker.java.basemaker;

import com.abelichenko.ExcludeField;
import com.abelichenko.TableFromClass;

/**
 * Created by Belichenko Anton on 08.12.16.
 * mailto: a.belichenko@gmail.com
 */

@TableFromClass
public class Profile {

    //@ExcludeField
    public transient String firstName;
    public String lastName;
    public int emailAddress;
    public String password;
    public double mobilePhoneNumber;
    public Long marketingTokenId;
    @ExcludeField
    public short gender;
    public Short cardNumber;
    public long postalCode;
    public Long birthday;
    public Float pmaCode;
    public int storeName;
    public boolean receiveEmails;
    //@ExcludeField
    public boolean verifiedNumber;

    public Profile() {
    }

}