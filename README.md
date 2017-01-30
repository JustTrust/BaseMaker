# SQLite table maker library
An annotation based library. It generate a table description for SQLite from a class fields.
This library will save you from writing a boring, monotone code.
You just need annotate your pojo class with @TableFromClass.
If you want to exclude some fields just annotate it with @ExcludeField annotation.

###### Annotated class
```java
@TableFromClass
public class ServerError{

    public String title;
    @ExcludeField
    public String message;
    public int type;
    public boolean internal;

    public ServerError(){
    }
}
```
###### Resulting generated class
```java
public class TBL {
    public static class ServerError$Table {
        public static final String TABLE_NAME = "servererror";

        public static final String CREATE = "CREATE TABLE servererror ("+
        "_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
        "title TEXT, "+
        "type INTEGER, "+
        "internal INTEGER "+
        ");";

        public static ContentValues getContentValue(ServerError servererror) {
          ContentValues result = new ContentValues();
          result.put("title", servererror.title);
          result.put("type", servererror.type);
          result.put("internal", servererror.internal);
          return result;
        }

        public static ServerError parseCursor(Cursor cursor) {
          ServerError servererror = new ServerError();
          servererror.title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
          servererror.type = cursor.getInt(cursor.getColumnIndexOrThrow("type"));
          servererror.internal = cursor.getInt(cursor.getColumnIndexOrThrow("internal")) > 0;
          return servererror;
        }

        public abstract static class Column {
          public static final String TITLE = "title";

          public static final String TYPE = "type";

          public static final String INTERNAL = "internal";
        }
    }
}
```
###### Example of using
```java
db.execSQL(TBL.ServerError$Table.CREATE)

db.createQuery(TBL.ServerError$Table.TABLE_NAME,
                "SELECT * FROM " + TBL.ServerError$Table.TABLE_NAME)
                
return TBL.ServerError$Table.parseCursor(cursor);                
```
##License

    Copyright 2016 JustTrust

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
