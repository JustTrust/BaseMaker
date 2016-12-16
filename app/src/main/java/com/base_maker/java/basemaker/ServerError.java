package com.base_maker.java.basemaker;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.ExcludeField;
import com.example.MakeDbFromClass;

/**
 * Created by Belichenko Anton on 08.12.16.
 * mailto: a.belichenko@gmail.com
 */

@MakeDbFromClass
public class ServerError implements Parcelable {

    public String title;
    public String message;
    public int type;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.message);
        dest.writeInt(this.type);
    }

    public ServerError() {

    }

    protected ServerError(Parcel in) {
        this.title = in.readString();
        this.message = in.readString();
        this.type = in.readInt();
    }

    @ExcludeField
    public static final Parcelable.Creator<ServerError> CREATOR = new Parcelable.Creator<ServerError>() {
        @Override
        public ServerError createFromParcel(Parcel source) {
            return new ServerError(source);
        }

        @Override
        public ServerError[] newArray(int size) {
            return new ServerError[size];
        }
    };
}
