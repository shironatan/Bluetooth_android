package websarvacom.wings.android.t16094;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class BTCommunicator extends Thread {
    public static final int DISPLAY_TOAST = 1000;
    public static final int STATE_CONNECTED = 1001;
    public static final int STATE_CONNECTERROR = 1002;
    public static final int MOTOR_STATE = 1003;
    public static final int STATE_RECEIVEERROR = 1004;
    public static final int STATE_SENDERROR = 1005;

    public static final int MOTOR_A = 0;
    public static final int MOTOR_B = 1;
    public static final int MOTOR_C = 2;
    public static final int DO_BEEP = 51;
    public static final int DISCONNECT = 99;
    public static final int NO_DELAY = 0;

    public static final UUID SERIAL_PORT_SERVICE_CLASS_UUID=
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String OUI_LEGO="00:16:53";

    private ConnectNXT myConNxt;
    private Handler uiHandler;
    private boolean connected=false;
    private BluetoothSocket nxtBTsocket = null;
    private DataOutputStream nxtDos = null;
    private boolean connect = false;
    private String mMACaddress;

    BluetoothAdapter btAdapter;

    public BTCommunicator(ConnectNXT myConNxt, Handler uiHandler, BluetoothAdapter btAdapter){
        this.myConNxt = myConNxt;
        this.uiHandler = uiHandler;
        this.btAdapter = btAdapter;
    }

    public Handler getHandler(){
        return myHandler;
    }

    public boolean isBTAdapterEnabled(){
        return (btAdapter == null)? false:btAdapter.isEnabled();
    }

    //スレッドスタートで実行される処理
    @Override
    public void run(){
        createNXTconnection();
        while(connected){
            //
        }
    }

    private void createNXTconnection(){
        try{
            BluetoothSocket nxtBTsocketTEMPORARY;
            BluetoothDevice nxtDevice = null;
            //MACアドレスよりデバイスを取得
            nxtDevice = btAdapter.getRemoteDevice(mMACaddress);

            if(nxtDevice == null){
                //ペアリングされたデバイスがないと表示
                sendToast(myConNxt.getResources().getString(R.string.no_paired_nxt));
                sendState(STATE_CONNECTERROR);
                return;
            }
            //Bluetooth Socketを生成
            nxtBTsocketTEMPORARY = nxtDevice.createRfcommSocketToServiceRecord(SERIAL_PORT_SERVICE_CLASS_UUID);
            nxtBTsocketTEMPORARY.connect();
            nxtBTsocket = nxtBTsocketTEMPORARY;

            //出力ストリームを生成
            nxtDos = new DataOutputStream(nxtBTsocket.getOutputStream());
            connect = true;

        }catch (IOException e){
            Log.d("BTCommunicator","error createNXTConnection()",e);
            if(myConNxt.newDevice){
                sendToast(myConNxt.getResources().getString(R.string.pairing_message));
                sendState(STATE_CONNECTERROR);
            }else{
                sendState(STATE_CONNECTERROR);
            }
            return;
        }
        //接続状態を引数でセットしてsendState()を呼び出す
        sendState(STATE_CONNECTED);
    }

    private void sendState(int message){
        //キーと値をバンドルしてsendBundle()実行
        Bundle myBundle = new Bundle();
        myBundle.putInt("message",message);
        sendBundle(myBundle);
    }

    private void sendBundle(Bundle myBundle){
        //メッセージにバンドル情報をセットしてsendMessage()実行
        Message myMessage = myHandler.obtaionMessage();
        myMessage.setData(myBundle);
        uiHandler.sendMessage(myMessage);
    }

    private boolean sendMessage(byte[] message){
        if(nxtDos == null){
            return false;
        }try{
            Log.v("sendMessage","message="+byteToStr(message));
            int messageLength = message.length;
            //接続先にバイトストリームで出力
            nxtDos.writeByte(messageLength);
            nxtDos.writeByte(messageLength>>8);
            nxtDos.write(message,0,message.length);
            nxtDos.flush();
            return true;
        }catch (IOException e){
            sendState(STATE_SENDERROR);
            return false;
        }
    }

    private void sendToast(String toastText){
        Bundle myBundle = new Bundle();
        myBundle.putInt("message",DISPLAY_TOAST);
        myBundle.putString("toastText",toastText);
        sendBundle(myBundle);
    }

    //スレッド間通信を行うHandleオブジェクトを作成
    final Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message myMessage){
            int message;
            switch (message = myMessage.getData().getInt("message")){
                case MOTOR_A:
                case MOTOR_B:
                case MOTOR_C:
                    changeMotorSpeed(message,myMessage.getData().getInt("value1"));
            }
        }
    }
}
