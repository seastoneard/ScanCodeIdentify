# ScanCodeIdentify

<!-- Baidu Button BEGIN -->

<div id="article_content" class="article_content">

<p><br>
</p>
<p><span style="font-size:14px"><strong>前言</strong>：该项目主要介绍了二维码扫描、闪光灯开启、本地二维码图片识别、二维码生成。分别是zxing和zbar(网&#26684;二维码)分别实现，具体效果运行项目apk...</span></p>
<p><span style="font-size:14px"><br>
</span></p>
<p><span style="font-size:14px"><strong><span style="color:rgb(51,51,51); font-family:宋体; text-align:center; background-color:rgb(249,249,249)"><span style="font-family:&quot;Microsoft YaHei&quot;,Arial">开发环境：AndroidStudio2.2.1&#43;gradle-2.14.1</span></span></strong></span></p>
<p><br>
</p>
<p><strong><span style="font-size:14px">涉及知识：</span></strong><br>
<span style="font-size:14px"><span style="white-space:pre"></span>1.Zxing和Zbar（网&#26684;）二维码扫描<br>
<span style="white-space:pre"></span>2.闪光灯开启与关闭<br>
<span style="white-space:pre"></span>3.本地二维码识别<br>
<span style="white-space:pre"></span>4.二维码生成<br>
<span style="white-space:pre"></span>5.Handler机制<br>
<span style="white-space:pre"></span>6.butterknife注解式开发</span><br>
</p>
<p><span style="font-size:14px"><br>
</span></p>
<p><span style="font-size:14px"><strong>引入依赖：</strong></span></p>
<p><span style="font-size:14px"><br>
</span></p>
<p><span style="font-size:14px"></span><pre name="code" class="java">    compile 'com.android.support:appcompat-v7:22.+'
    compile 'com.google.zxing:core:3.2.1'
    compile 'com.jakewharton:butterknife:7.0.1'
    compile files('libs/zbar.jar')</pre><br>
</p>
<p><span style="font-size:14px"><strong>部分代码：</strong></span></p>
<p><span style="font-size:14px"><br>
</span></p>
<p><span style="font-size:14px"></span><pre name="code" class="java">/**
 * Zbar二维码扫描+闪光灯+本地二维码识别
 */
public class ZbarActivity extends AppCompatActivity implements QRCodeView.Delegate {

    @Bind(R.id.zbarview)
    ZBarView mQRCodeView;

    @Bind(R.id.scancode_lamplight)
    ToggleButton toggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zbartest_scan_layout);
        ButterKnife.bind(this);
        initLayout();
    }

    private void initLayout() {
        mQRCodeView.setDelegate(this);
        mQRCodeView.startSpotAndShowRect();//显示扫描框，并且延迟1.5秒后开始识别
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mQRCodeView.openFlashlight();
                } else {
                    mQRCodeView.closeFlashlight();
                }
            }
        });
    }

    @OnClick({R.id.line_back, R.id.scancode_localimg})
    protected void onClickBtn(View view) {
        switch (view.getId()) {
            case R.id.line_back:
                finish();
                break;
            case R.id.scancode_localimg:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setType(&quot;image/*&quot;);
                startActivityForResult(intent, 0x11);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK &amp;&amp; requestCode == 0x11) {
            Uri uri = data.getData();
            String path = null;
            if (!TextUtils.isEmpty(uri.getAuthority())) {
                Cursor cursor = getContentResolver().query(uri,
                        new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                if (null == cursor) {
                    Toast.makeText(ZbarActivity.this, &quot;图片没找到&quot;, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ZbarActivity.this, &quot;图片路径为空&quot;, Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

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
//        mQRCodeView.closeFlashlight();
        super.onStop();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    private void codeDiscriminate(final String path) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                String result = null;
                if (Build.VERSION.SDK_INT &gt;= Build.VERSION_CODES.KITKAT) {
                    result = QRCodeDecoder.syncDecodeQRCode(path);
                } else {
                    result = QRCodeDecoder.syncDecodeQRCode2(path);
                }
                Log.i(&quot;zbar_result&quot;, Build.VERSION.SDK_INT + &quot;---&gt;&quot; + result);
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
    protected void onDestroy() {
        mQRCodeView.onDestroy();
        ButterKnife.unbind(this);
        super.onDestroy();

    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.i(&quot;zbar_result&quot;, &quot;result:&quot; + result);
        Toast.makeText(this, &quot;二维码的数据：&quot; + result, Toast.LENGTH_SHORT).show();
        vibrate();
        mQRCodeView.startSpot();
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(&quot;zbar_result&quot;, &quot;打开相机出错&quot;);
        Toast.makeText(this, &quot;打开相机出错&quot;, Toast.LENGTH_SHORT).show();
    }

}
</pre><br>
<br>
</p>
   
</div>



<!-- Baidu Button END -->
