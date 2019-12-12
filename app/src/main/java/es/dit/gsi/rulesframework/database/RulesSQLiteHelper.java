package es.dit.gsi.rulesframework.database;

import android.app.admin.DeviceAdminReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import es.dit.gsi.rulesframework.model.Rule;

/**
 * Created by afernandez on 21/12/15.
 */
public class RulesSQLiteHelper extends SQLiteOpenHelper {


    //Sentencia SQL para crear la tabla de Usuarios
    String sqlCreate = "CREATE TABLE Rules (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, ifElement TEXT, ifAction TEXT, doElement TEXT, doAction TEXT,ifParameter TEXT, doParameter TEXT, description TEXT, place TEXT)";
    private static final String DATABASE_NAME = "Rules";
    private static final int DATABASE_VERSION = 1;

    public RulesSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Se ejecuta la sentencia SQL de creación de la tabla
        db.execSQL(sqlCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnterior, int versionNueva) {
        //NOTA: Por simplicidad del ejemplo aquí utilizamos directamente la opción de
        //      eliminar la tabla anterior y crearla de nuevo vacía con el nuevo formato.
        //      Sin embargo lo normal será que haya que migrar datos de la tabla antigua
        //      a la nueva, por lo que este método debería ser más elaborado.

        //Se elimina la versión anterior de la tabla
        db.execSQL("DROP TABLE IF EXISTS Rules");

        //Se crea la nueva versión de la tabla
        db.execSQL(sqlCreate);
    }

    public void addRule(Rule rule){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        String[] e = {""};
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("name", rule.getRuleName());
        values.put("ifElement", rule.getIfElement());
        values.put("ifAction", rule.getIfAction());
        values.put("doElement", rule.getDoElement());
        values.put("doAction", rule.getDoAction());
        values.put("ifParameter", convertArrayToString(rule.getIfParameter().toArray(new String[rule.getIfParameter().size()])));
        Log.i("SQLite", convertArrayToString(rule.getIfParameter().toArray(new String[rule.getIfParameter().size()])));
        values.put("doParameter", convertArrayToString(rule.getDoParameter().toArray(new String[rule.getDoParameter().size()])));
        Log.i("SQLite", convertArrayToString(rule.getDoParameter().toArray(new String[rule.getDoParameter().size()])));
        values.put("description", rule.getDescription());
        values.put("place", rule.getPlace());




        // 3. insert
        db.insert("Rules", // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public static String strSeparator = "__,__";
    public static String convertArrayToString(String[] array){
        String str = "";
        for (int i = 0;i<array.length; i++) {
            str = str+array[i];
            // Do not append comma at the end of last element
            if(i<array.length-1){
                str = str+strSeparator;
            }
        }
        return str;
    }
    public static List<String> convertStringToList(String str){
        String[] arr = str.split(strSeparator);
        Arrays.asList(arr);
        return Arrays.asList(arr);
    }

    public List<Rule> getAllRules(){
        List<Rule> list = new LinkedList<Rule>();
        // 1. build the query
        String query = "SELECT  * FROM " + "Rules";

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        Rule rule = null;
        if (cursor.moveToFirst()) {
            do {
                rule = new Rule();
                rule.setId(Integer.parseInt(cursor.getString(0)));
                rule.setRuleName(cursor.getString(1));
                rule.setIfElement(cursor.getString(2));
                rule.setIfAction(cursor.getString(3));
                rule.setDoElement(cursor.getString(4));
                rule.setDoAction(cursor.getString(5));
                rule.setIfParameter(convertStringToList(cursor.getString(6)));
                rule.setDoParameter(convertStringToList(cursor.getString(7)));
                rule.setDescription(cursor.getString(8));
                rule.setPlace(cursor.getString(9));

                list.add(rule);
            } while (cursor.moveToNext());
        }

        Log.d("getAllRules()", "getRules");

        return list;
    }

    public void deleteAllRules(){
        Log.i("Database","Delete All Rules");
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DATABASE_NAME,null,null);
        db.close();
    }

}
