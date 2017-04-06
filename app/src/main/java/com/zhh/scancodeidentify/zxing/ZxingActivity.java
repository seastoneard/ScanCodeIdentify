package com.zhh.scancodeidentify.zxing;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.zhh.scancodeidentify.R;
import com.zhh.scancodeidentify.qrcode.QRCodeDecoder;
import com.zhh.scancodeidentify.qrcode.QRCodeView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Zbar二维码扫描+闪光灯+本地二维码识别
 */
public class ZxingActivity extends AppCompatActivity implements QRCodeView.Delegate {

    @Bind(R.id.zxingview)
    ZXingView mQRCodeView;

    @Bind(R.id.scancode_lamplight)
    ToggleButton toggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zxingtest_scan_layout);
        ButterKnife.bind(this);
        initLayout();
    }

    private void initLayout() {
        mQRCodeView.setDelegate(this);
        mQRCodeView.startSpotAndShowRect();//显示扫描框，并且延迟1.5秒后开始识别
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {//收藏
                    mQRCodeView.openFlashlight();
                } else {
                    mQRCodeView.closeFlashlight();
                }
            }
        });

    }

    @OnClick({R.id.line_back, R.id.scancode_localimg})
    protected void onClickBtn(View v) {
        switch (v.getId()) {
            case R.id.line_back:
                finish();
                break;
            case R.id.scancode_localimg:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 0x11);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 0x11) {
            Uri uri = data.getData();
            String path = null;
            if (!TextUtils.isEmpty(uri.getAuthority())) {
                Cursor cursor = getContentResolver().query(uri,
                        new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                if (null == cursor) {
                    Toast.makeText(ZxingActivity.this, "图片没找到", Toast.LENGTH_SHORT).show();
                    return;
                }
                cursor.moveToFirst();
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                cursor.close();
            } else {
                path = uri.getPath();
            }
            if (null != path) {
                codeDiscriminate(path);
            } else {
                Toast.makeText(ZxingActivity.this, "图片路径为空", Toast.LENGTH_SHORT).show();
                return;
            }

        }

    }


    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.i("zhh", "result:" + result);
        Toast.makeText(this, "二维码的数据：" + result, Toast.LENGTH_SHORT).show();
        vibrate();
        mQRCodeView.startSpot();
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e("zhh", "打开相机出错");
        Toast.makeText(this, "打开相机出错", Toast.LENGTH_SHORT).show();
    }

    private void codeDiscriminate(final String path) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                String result = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    result = QRCodeDecoder.syncDecodeQRCode(path);
                } else {
                    result = QRCodeDecoder.syncDecodeQRCode2(path);
                }
                Log.i("zxing_result", Build.VERSION.SDK_INT + "--->" + result);
                Message msg = mHandler.obtainMessage();
                //封装消息id
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
                if (null != msg.obj)
                    onScanQRCodeSuccess(msg.obj.toString());
            }
        }

    };


    @Override
    protected void onRestart() {
        mQRCodeView.startCamera();
        super.onRestart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mQRCodeView.startSpotAndShowRect();//显示扫描框，并且延迟1.5秒后开始识别
    }

    @Override
    protected void onStop() {
        mQRCodeView.stopCamera();
        super.onStop();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }


    @Override
    protected void onDestroy() {
        mQRCodeView.onDestroy();
        ButterKnife.unbind(this);
        super.onDestroy();
    }


}
