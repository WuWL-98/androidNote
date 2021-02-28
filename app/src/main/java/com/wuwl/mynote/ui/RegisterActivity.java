package com.wuwl.mynote.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.wuwl.mynote.R;
import com.wuwl.mynote.db.NoteDbReader;

import javax.xml.transform.Source;

public class RegisterActivity extends BaseActivity {
    private EditText password;
    private EditText rePassword;
    private Button register;
    private String mainSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        SQLiteDatabase dbreader = NoteDbReader.getDbreader(this);
        boolean isFirst = NoteDbReader.selectPaswd(dbreader);
        mainSource = getIntent().getStringExtra("source");
        if (!isFirst && mainSource == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        init();

        if (mainSource != null) {
            if (mainSource.equals("MainActivity")) {
                LinearLayout ll = findViewById(R.id.ll_goback);
                ll.setVisibility(View.VISIBLE);
                password.setHint("输入四位数字新密码");
            }
        }
    }

    private void init() {
        password = findViewById(R.id.edit_password);
        rePassword = findViewById(R.id.edit_re_password);
        register = findViewById(R.id.register);
    }

    protected void onResume() {
        super.onResume();
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "请输入四位数数字密码", Toast.LENGTH_SHORT).show();
                } else if (!password.getText().toString().equals(rePassword.getText().toString())) {
                    Toast.makeText(RegisterActivity.this, "输入两次密码不同！", Toast.LENGTH_SHORT).show();
                } else if (password.getText().toString().length() != 4) {
                    Toast.makeText(RegisterActivity.this, "密码长度必须为4位数字", Toast.LENGTH_SHORT).show();
                } else {
                    if (mainSource==null) {
                        registerPwd();
                    } else if (mainSource != null && mainSource.equals("MainActivity")) {
                        registerPwd2();
                    }
                }

            }
        });
    }

    private void registerPwd2() {
        SQLiteDatabase dbwriter = NoteDbReader.getDbwriter(this);
        NoteDbReader.deletePaswd(dbwriter);
        registerPwd();
    }

    private void registerPwd() {
        SQLiteDatabase dbwriter = NoteDbReader.getDbwriter(this);
        boolean success = NoteDbReader.registerPaswd(dbwriter, password.getText().toString());
        if (success) {
            if (mainSource != null && mainSource.equals("MainActivity")) {
                Toast.makeText(this, "修改密码成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
            }
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            if (mainSource != null && mainSource.equals("MainActivity")) {
                Toast.makeText(this, "修改密码失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void goBack(View view) {
        finish();
    }
}
