package com.wuwl.mynote.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.wuwl.mynote.R;
import com.wuwl.mynote.bean.Note;
import com.wuwl.mynote.db.NoteDbReader;
import com.wuwl.mynote.util.SDCardUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.qzb.richeditor.RichEditor;

public class NotesActivity extends BaseActivity/* implements PermissionsUtil.IPermissionsCallback*/ {
    private String imagePath = "";
//    private PermissionsUtil permissionsUtil;
    private RichEditor re;

    private TextView time;
    private ImageView ibt_delete;
    private ImageView ibt_save;

    private Note mNote;
    private int mId;

    private Handler mHandler;
    private Runnable runnable;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        init();

        ibt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save(true);
            }
        });
        ibt_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete();
            }
        });
    }


    private void init() {
        re = findViewById(R.id.editor);
        time = findViewById(R.id.showtime);
        ibt_delete = findViewById(R.id.ibt_delete);
        ibt_save = findViewById(R.id.ibt_save);
        mNote = (Note) getIntent().getSerializableExtra("nontInfo");

        if (mNote == null) {
            re.setPlaceholder("input text here........");
            ibt_delete.setVisibility(View.GONE);
            time.setVisibility(View.GONE);
        } else {
            ibt_delete.setVisibility(View.VISIBLE);
            time.setVisibility(View.VISIBLE);
            time.setTextColor(this.getResources().getColor(R.color.colorBlack));
            time.setText(mNote.getnTime());
            mId = mNote.getnId();
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    re.setHtml(mNote.getnContent());
                }
            });
            thread.start();
            thread.interrupt();
        }

        re.setPadding(10, 10, 10, 10);
        re.setTextBackgroundColor(this.getResources().getColor(R.color.colorTransprent));
        re.setBackgroundColor(this.getResources().getColor(R.color.colorTransprent));
    }

    public void addPic(View view) {
        if (!(ActivityCompat.checkSelfPermission(NotesActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                || !(ActivityCompat.checkSelfPermission(NotesActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            //没有权限，申请权限
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            //申请权限，其中RC_PERMISSION是权限申请码，用来标志权限申请的
            ActivityCompat.requestPermissions(NotesActivity.this, permissions, 3);
        } else {
            //拥有权限
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 1);
        }

//        permissionsUtil = PermissionsUtil
//                .with(this)
//                .requestCode(2)
//                .isDebug(true)//开启log
//                .permissions(
//                        PermissionsUtil.Permission.Storage.READ_EXTERNAL_STORAGE,
//                        PermissionsUtil.Permission.Storage.WRITE_EXTERNAL_STORAGE
//                )
//                .request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 3 && grantResults.length == 2
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.e("TAG", "权限申请成功");
        } else {
            Log.e("TAG", "权限申请失败");
        }
    }

    public void save(boolean saveType) {
        if (re.getHtml() == null) {
            return;
        }
        SQLiteDatabase dbwriter = NoteDbReader.getDbwriter(this);
        Log.d("html", re.getHtml());
        Log.d("html", getTime());
        mNote = new Note();
        mNote.setnContent(re.getHtml());

        if (time.getVisibility() == View.GONE) {
            mNote.setnTime(getTime());
        } else {
            mNote.setnId(mId);

        }
        NoteDbReader.save(mNote, dbwriter);
        if (saveType) {
            finish();
        }
    }

    private void delete() {
        SQLiteDatabase dbwriter = NoteDbReader.getDbwriter(this);
        NoteDbReader.delete(mNote.getnId(), dbwriter);
        finish();
    }

    public String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        Date date = new Date();
        String str = sdf.format(date);
        return str;
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        //需要调用onRequestPermissionsResult
//        permissionsUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        permissionsUtil.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                try {
                    Uri uri = data.getData();
                    ContentResolver contentResolver = NotesActivity.this.getContentResolver();
                    Bitmap bitmap;
                    bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri));
                    imagePath = SDCardUtil.saveMyBitmap(bitmap, System.currentTimeMillis() + "");
//                    re.focusEditor();
                    re.insertImage(imagePath, "IMG", 100);
//                    re.insertImageWrapWidth(imgPath, "IMG");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println(e);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

//    @Override
//    public void onPermissionsGranted(int requestCode, String... permission) {
//
//        if (requestCode == 2) {
//
//        }
//    }

//    @Override
//    public void onPermissionsDenied(int requestCode, String... permission) {
//
//    }

    public void goBack(View view) {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (time.getVisibility() != View.GONE) {
            mHandler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    mHandler.postDelayed(this, 10000);
                    Toast.makeText(NotesActivity.this, "自动保存成功", Toast.LENGTH_SHORT).show();
                    save(false);
                }
            };
            mHandler.postDelayed(runnable, 10000);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mHandler != null) {
            mHandler.removeCallbacks(runnable);
        }
    }
}
