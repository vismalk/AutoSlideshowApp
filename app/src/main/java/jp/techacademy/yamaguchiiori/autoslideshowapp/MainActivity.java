package jp.techacademy.yamaguchiiori.autoslideshowapp;

import android.Manifest;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    Handler mHandler = new Handler();
    Timer timer;
    Button mAdvanceButton;
    Button mReturnButton;
    Button mStart_PauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdvanceButton = (Button) findViewById(R.id.advance_button);
        mReturnButton = (Button) findViewById(R.id.return_button);
        mStart_PauseButton = (Button) findViewById(R.id.start_pause_button);



        // パーミッションの許可状態を確認する
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsInfo();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            } else {
                getContentsInfo();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }
    public void getContentsInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        final Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );
        if (cursor.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            setImageView(cursor);
        }

        mAdvanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    advance(cursor);
                }
        });

        mReturnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (cursor.moveToPrevious()) {
                        setImageView(cursor);
                    } else {
                        cursor.moveToLast();
                        setImageView(cursor);
                    }
                }

        });
        mStart_PauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timer == null) {
                    timer = new Timer();
                    mAdvanceButton.setEnabled(false);
                    mReturnButton.setEnabled(false);
                    mStart_PauseButton.setText("停止");
                    timer.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    advance(cursor);
                                }
                            });
                        }
                    }, 2000, 2000);
                }else if (timer != null) {
                       timer.cancel();
                       timer = null;
                    mAdvanceButton.setEnabled(true);
                    mReturnButton.setEnabled(true);
                    mStart_PauseButton.setText("再生");
                }
            }
          });
    }

    private void advance(Cursor cursor) {
        if (cursor.moveToNext()) {
            setImageView(cursor);
        } else {
            cursor.moveToFirst();
            setImageView(cursor);
        }
    }

    private void setImageView(Cursor cursor) {
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);
    }
}

