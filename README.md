# SQLite table maker library
=============
An annotation based library. It generate a table description for SQLite from a class fields.
This library will save you from writing a boring, monotone code.
You just need annotate your pojo class with @TableFromClass.
If you want to exclude some fields just annotate it with @ExcludeField annotation.

###### Sample of using

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

Will give this result

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

