package com.example.sensores;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;




public class MainActivity extends AppCompatActivity {

    Button btnOn, btnOff;
    TextView txtArduino, txtString, txtStringLength, sensorView0, sensorView1, sensorView2, sensorView3;
    TextView txtSendorLDR,textView3;
    //ImageButton btnArriba, btnAbajo, btnIzquierda, btnDerecha;
    TextView textView5, textView8,textView10,textView9;
    LinearLayout pantalla;

    private StringBuilder recDataString = new StringBuilder();
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;
    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    private static String address = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //configurarDispositivo();
        btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter
        VerificarEstadoBT();
        textView5=findViewById(R.id.textView5);
        textView8=findViewById(R.id.textView8);
        textView10=findViewById(R.id.textView10);
        textView9=findViewById(R.id.textView9);
        pantalla=findViewById(R.id.pantalla);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {										//if message is what we want
                    String readMessage = (String) msg.obj;                          // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);      								//keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                    //    String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        //txtString.setText("Datos recibidos = " + dataInPrint);
                     //   int dataLength = dataInPrint.length();                            //get length of data received
                        //txtStringLength.setText("Tamaño del String = " + String.valueOf(dataLength));
                        if (recDataString.charAt(0) == '#')                                //if it starts with # we know it is what we are looking for
                        {

                            String temp =recDataString.substring(1,endOfLineIndex);
                            temp=temp.replace("-"," ");
                            String datos[] = temp.split(" ");
                            String sensor0 = datos[0];            //get sensor value from string between indices 1-5
                            String sensor1 = datos[1];
                            String sensor2= datos[2];
                            //update the textviews with sensor values
                            textView5.setText(sensor0+" °C");
                            textView8.setText(sensor1+"%");
                            textView10.setText(sensor2+" ppm");

                            float CO2 = Float.parseFloat(sensor2);
                            if(CO2>1000.0){
                                pantalla.setBackgroundColor(getResources().getColor(R.color.danger));
                                textView9.setTextColor(getResources().getColor(R.color.danger_text));
                                textView9.setText("La concentración de CO2 es alto. Este nivel puede ser mortal");
                                textView10.setTextColor(getResources().getColor(R.color.danger_text));
                            }else{
                                pantalla.setBackgroundColor(getResources().getColor(R.color.danger_text));
                                textView9.setTextColor(getResources().getColor(R.color.negro));
                                textView9.setText(getResources().getText(R.string.CO2_is));
                                textView10.setTextColor(getResources().getColor(R.color.negro));
                            }
                            //sensorView3.setText(" Sensor 3 Voltage = " + sensor3 + "V");
                        }
                        recDataString.delete(0, recDataString.length()); 					//clear all string data
                        //strIncom =" ";
                        //dataInPrint = " ";
                    }
                }
            }
        };

    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        //crea un conexion de salida segura para el dispositivo
        //usando el servicio UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //Consigue la direccion MAC desde DeviceListActivity via intent
        Intent intent = getIntent();
        //Consigue la direccion MAC desde DeviceListActivity via EXTRA
        address = intent.getStringExtra(DispositivosBT.EXTRA_DEVICE_ADDRESS);//<-<- PARTE A MODIFICAR >->->
        //Setea la direccion MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establece la conexión con el socket Bluetooth.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {}
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        { // Cuando se sale de la aplicación esta parte permite
            // que no se deje abierto el socket
            btSocket.close();
        } catch (IOException e2) {}
    }

    //Comprueba que el dispositivo Bluetooth Bluetooth está disponible y solicita que se active si está desactivado
    private void VerificarEstadoBT() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //Crea la clase que permite crear el evento de conexion
    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Envia los datos obtenidos hacia el evento via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //Envio de trama
        public void write(String input)
        {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    // private void configurarDispositivo() {
     //   Button button = (Button) findViewById(R.id.button);
       // button.setOnClickListener(new View.OnClickListener() {
         //   @Override
           // public void onClick(View v) {
             //   startActivity(new Intent(MainActivity.this, .class));
           // }
        //});


    }



