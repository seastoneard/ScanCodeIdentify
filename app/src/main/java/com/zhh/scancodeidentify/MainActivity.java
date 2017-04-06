package com.zhh.scancodeidentify;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.zhh.scancodeidentify.generate.QrcodeActivity;
import com.zhh.scancodeidentify.zbar.ZbarActivity;
import com.zhh.scancodeidentify.zxing.ZxingActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        super.onDestroy();

    }

    @OnClick({R.id.btn_zxing, R.id.btn_zbar, R.id.btn_qroce})
    protected void onClickBtn(View v) {
        switch (v.getId()) {
            case R.id.btn_zxing:
                openIntent(ZxingActivity.class);
                break;
            case R.id.btn_zbar:
                openIntent(ZbarActivity.class);
                break;
            case R.id.btn_qroce:
                openIntent(QrcodeActivity.class);
            default:
                break;
        }
    }

    private void openIntent(Class<?> mClass) {
        startActivity(new Intent(this, mClass));
    }
}
