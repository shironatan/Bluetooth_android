package websarvacom.wings.android.t16094;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ConnectNXT extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 2000;
    private Toast mLongToast;
    private Toast mShortToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_nxt);

        //Toast表示処理対応
        mLongToast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        mLongToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }

    @Override
    protected void onStart(){
        super.onStart();
        //Bluetooth対応端末チェック
        if(!BluetoothAdapter.getDefaultAdapter().equals(null)){
            //Bluetooth対応端末の処理
            Log.v("Bluetooth","Bluetooth is supported");
        }else{
            //Bluetooth非対応の時、処理終了
            Log.v("Bluetooth","Bluetooth is not supported");
        }
        //----Bluetooth有効判定
        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()){
            //有効でないとき　画面にトースト表示
            mShortToast.setText("BluetoothをONにします。");
            mShortToast.show();
            //Bluetoothへ指示を送るIntentを用意
            Intent reqEnableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //Bluetoothで処理した結果を受け取るためStartActivityForResult()を実行
            //その際インテントとリクエストコードを設定して実行
            startActivityForResult(reqEnableBTIntent,REQUEST_ENABLE_BT);
        }else{
            //有効時
            mShortToast.setText("BluetoothはONです。");
            mShortToast.show();
            Log.v("Bluetooth","Bluetooth is On");
        }
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        //終了したアクティビティからリクエストコード、結果コード、データを受け取る
        switch(requestCode){
            case REQUEST_ENABLE_BT:
                switch (resultCode){
                    case Activity.RESULT_OK:
                        mShortToast.setText("Bluetoothが有効になりました。");
                        mShortToast.show();
                        break;
                    case Activity.RESULT_CANCELED:
                        mShortToast.setText("Bluetoothを有効にしてください。");
                        mShortToast.show();
                        finish();
                        break;
                    default:
                        Log.v("Bluetooth",Integer.toString(resultCode));
                        finish();
                        break;
                }
                break;
            default:
                Log.v("Bluetooth",Integer.toString(requestCode));
                finish();
                break;
        }
    }

}
