package com.base_maker.java.basemaker;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.ExcludeField;
import com.example.MakeDbFromClass;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Belichenko Anton on 08.12.16.
 * mailto: a.belichenko@gmail.com
 */

@MakeDbFromClass
public class Profile implements Parcelable {

    @SerializedName("FirstName")
    @Expose

    public String firstName;
    @SerializedName("LastName")
    @Expose
    public String lastName;
    @SerializedName("EmailAddress")
    @Expose
    public String emailAddress;
    @SerializedName("Password")
    @Expose
    public String password;
    @SerializedName("MobilePhoneNumber")
    @Expose
    public Double mobilePhoneNumber;
    @SerializedName("MarketingTokenId")
    @Expose
    public Long marketingTokenId;
    @SerializedName("Gender")
    @Expose
    public String gender;
    @SerializedName("CardNumber")
    @Expose
    public String cardNumber;
    @SerializedName("PostalCode")
    @Expose
    public String postalCode;
    @SerializedName("Birthday")
    @Expose
    public String birthday;
    @SerializedName("PmaCode")
    @Expose
    public String pmaCode;
    @SerializedName("StoreName")
    @Expose
    public String storeName;
    @SerializedName("ReceiveEmails")
    @Expose
    public boolean receiveEmails;
    @SerializedName("VerifiedNumber")
    @Expose
    public boolean verifiedNumber;

    public ServerError serverError;

    public Profile() {
    }

    public Profile(Profile profile) {
        this.firstName = profile.firstName;
        this.lastName = profile.lastName;
        this.emailAddress = profile.emailAddress;
        this.password = profile.password;
        this.mobilePhoneNumber = profile.mobilePhoneNumber;
        this.marketingTokenId = profile.marketingTokenId;
        this.gender = profile.gender;
        this.cardNumber = profile.cardNumber;
        this.postalCode = profile.postalCode;
        this.birthday = profile.birthday;
        this.pmaCode = profile.pmaCode;
        this.storeName = profile.storeName;
        this.receiveEmails = profile.receiveEmails;
        this.verifiedNumber = profile.verifiedNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.firstName);
        dest.writeString(this.lastName);
        dest.writeString(this.emailAddress);
        dest.writeString(this.password);
        dest.writeDouble(this.mobilePhoneNumber);
        dest.writeLong(this.marketingTokenId);
        dest.writeString(this.gender);
        dest.writeString(this.cardNumber);
        dest.writeString(this.postalCode);
        dest.writeString(this.birthday);
        dest.writeString(this.pmaCode);
        dest.writeString(this.storeName);
        dest.writeInt(this.receiveEmails ? 1 : 0);
        dest.writeInt(this.verifiedNumber ? 1 : 0);
    }

    protected Profile(Parcel in) {
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.emailAddress = in.readString();
        this.password = in.readString();
        this.mobilePhoneNumber = in.readDouble();
        this.marketingTokenId = in.readLong();
        this.gender = in.readString();
        this.cardNumber = in.readString();
        this.postalCode = in.readString();
        this.birthday = in.readString();
        this.pmaCode = in.readString();
        this.storeName = in.readString();
        this.receiveEmails = in.readInt() == 1;
        this.verifiedNumber = in.readInt() == 1;
    }

    @ExcludeField
    public static final Parcelable.Creator<Profile> CREATOR = new Parcelable.Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel source) {
            return new Profile(source);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };
}