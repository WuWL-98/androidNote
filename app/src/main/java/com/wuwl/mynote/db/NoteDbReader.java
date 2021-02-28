package com.wuwl.mynote.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wuwl.mynote.bean.Note;

public class NoteDbReader {
    private static SQLiteDatabase dbreader;
    private static SQLiteDatabase dbwriter;

    public static SQLiteDatabase getDbreader(Context context) {
        NoteDB noteDB = new NoteDB(context);
        dbreader = noteDB.getReadableDatabase();
        return dbreader;
    }

    public static SQLiteDatabase getDbwriter(Context context) {
        NoteDB noteDB = new NoteDB(context);
        dbwriter = noteDB.getWritableDatabase();
        return dbwriter;
    }

    public static Cursor selectAll(SQLiteDatabase dbreader) {
        Cursor cursor = dbreader.query(NoteDB.TABLE_NAME, null, null, null, null, null, null);
        return cursor;
    }

    public static Cursor select(SQLiteDatabase dbreader, String find) {
        Cursor cursor = dbreader.rawQuery("select * from " + NoteDB.TABLE_NAME +
                " where " + NoteDB.CONTENT + " like '%" + find + "%'", null);
        return cursor;
    }

    public static int delete(int id, SQLiteDatabase dbwriter) {
        int delete = dbwriter.delete(NoteDB.TABLE_NAME, NoteDB.ID + "=" + id, null);
        return delete;
    }

    public static void save(Note note, SQLiteDatabase dbwriter) {
        ContentValues cv = new ContentValues();
        boolean type = true;
        //如果是新加的文本，那么就加入时间，并且用insert语句
        if (note.getnId() == 0) {
            cv.put(NoteDB.TIME, note.getnTime());
        } else {
            type = false;
        }
        cv.put(NoteDB.CONTENT, note.getnContent());
        if (!type) {
            dbwriter.update(NoteDB.TABLE_NAME, cv, NoteDB.ID + "=" + note.getnId(), null);
        } else {
            dbwriter.insert(NoteDB.TABLE_NAME, null, cv);
        }

    }


    public static void deletePaswd(SQLiteDatabase dbwriter){
        dbwriter.delete(NoteDB.TABLE_NAME_PWD,null,null);
    }
    public static String getPasswd(SQLiteDatabase dbreader){
        Cursor cursor = dbreader.rawQuery("select * from "+NoteDB.TABLE_NAME_PWD, null);
        cursor.moveToNext();
        String password = cursor.getString(cursor.getColumnIndex(NoteDB.PWD));
        return password;
    }
    public static boolean selectPaswd(SQLiteDatabase dbreader) {
        Cursor cursor = dbreader.rawQuery("select * from "+NoteDB.TABLE_NAME_PWD, null);
        return !cursor.moveToNext();
    }
    public static boolean registerPaswd(SQLiteDatabase dbwriter,String passwd){
        ContentValues cv = new ContentValues();
        cv.put(NoteDB.PWD,passwd);
        long insert = dbwriter.insert(NoteDB.TABLE_NAME_PWD, null, cv);
        if (insert>0){
            return true;
        }
        return false;
    }
}
