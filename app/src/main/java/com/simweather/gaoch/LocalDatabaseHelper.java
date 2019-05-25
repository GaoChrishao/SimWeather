package com.simweather.gaoch;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import android.util.Log;

public class LocalDatabaseHelper extends SQLiteOpenHelper {
    public static final String tableName="weathers";
    public static final String citycode="citycode";
    public static final String content="content";
    private Context context;
    private final int INITIAL_VERSION = 1 ; // 初始版本号
    public static int NEW_VERSION = 1 ;       // 最新的版本号 ,增加Focus表
    public static final String CREATE_TABLE_WEATHERS="create table weathers("
            +"id integer primary key autoincrement,"
            +"citycode varchar,"
            + "content text)";


    public LocalDatabaseHelper(Context context,String name,SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_WEATHERS);
        Log.e("GGG","本地数据库创建成功");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //判断用户当前安装的本是不是1.0版本
//        if(oldVersion == INITIAL_VERSION &&newVersion==2){
//            db.execSQL(CREATE_TABLE_WEATHERS);
//            oldVersion++;
//        }
//        Toast.makeText(context, "数据库升级", Toast.LENGTH_SHORT).show();
    }
}
