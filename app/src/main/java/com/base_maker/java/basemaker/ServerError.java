package com.base_maker.java.basemaker;

import com.example.MakeDbFromClass;

import java.io.Serializable;

/**
 * Created by Belichenko Anton on 08.12.16.
 * mailto: a.belichenko@gmail.com
 */

@MakeDbFromClass
public class ServerError implements Serializable{

    public String title;
    public String message;
    public int type;

    public ServerError(){

    }
}
