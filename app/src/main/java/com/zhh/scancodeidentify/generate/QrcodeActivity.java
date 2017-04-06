package com.zhh.scancodeidentify.generate;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.zhh.scancodeidentify.R;
import com.zhh.scancodeidentify.qrcode.QRCodeEncoder;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zhh on 2017/4/6.
 */

public class QrcodeActivity extends AppCompatActivity {


    @Bind(R.id.img_qroce)
    ImageView imgView;

    @Bind(R.id.et_qroce)
    EditText et;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qroce_generate_layout);
        ButterKnife.bind(this);
    }


    @OnClick(R.id.btn_qroce)
    protected void onClickBtn(View v) {
        if (v.getId() == R.id.btn_qroce) {
            String result = et.getText().toString();
            if (TextUtils.isEmpty(result)) {
                Toast.makeText(this, "要生成的二维码内容不能为空~", Toast.LENGTH_SHORT).show();
            } else {
                qrcodeGenerate(result);
            }
        }
    }

    private void qrcodeGenerate(final String content) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                Bitmap result = QRCodeEncoder.syncEncodeQRCode(content, 150, R.color.colorAccent);
                Message msg = mHandler.obtainMessage();
                msg.what = 1;//作为标示，便于接收消息
                msg.obj = result;
                mHandler.sendMessage(msg);//发送消息
            }
        }).start();
    }


    //创建一个Hander局部类对象，通过handleMessage()钩子方法来更新UI控件
    Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            //得到封装消息的id进行匹配
            if (1 == msg.what) {
                if (null != msg.obj) {
                    Bitmap bitmap = (Bitmap) msg.obj;
                    imgView.setImageBitmap(bitmap);
                }
            }
        }

    };

    @Override
    protected void onDestroy() {
        ButterKnife.unbind(this);
        super.onDestroy();
    }
}
