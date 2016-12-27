package com.base_maker.java.basemaker;

import com.abelichenko.ExcludeField;
import com.abelichenko.TableFromClass;

import java.io.Serializable;

/**
 * Created by Belichenko Anton on 08.12.16.
 * mailto: a.belichenko@gmail.com
 */

@TableFromClass
public class ServerError implements Serializable{

    public String title;
    @ExcludeField
    public String message;
    public int type;
    public boolean internal;

    public ServerError(){

    }
}
