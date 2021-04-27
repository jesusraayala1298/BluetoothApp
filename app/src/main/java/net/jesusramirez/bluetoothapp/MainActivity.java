package net.jesusramirez.bluetoothapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Button btnBuscarDispositivo;
    private ListView lvDispositivos;
    private Button btnBluetooth;
    private BluetoothAdapter bAdapter;
    private ArrayList<BluetoothDevice> arrayDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnBuscarDispositivo = (Button)findViewById(R.id.btnBuscarDispositivo);
        lvDispositivos = (ListView)findViewById(R.id.lvDispositivos);
        btnBluetooth = (Button)findViewById(R.id.btnBluetooth);
        registrarEventosBluetooth();

        bAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bAdapter == null)
        {
            btnBluetooth.setEnabled(false);
            return;
        }

        // Comprobamos si el Bluetooth esta activo y cambiamos el texto del
        // boton dependiendo del estado.
        if(bAdapter.isEnabled())
            btnBluetooth.setText(R.string.DesactivarBluetooth);
        else
            btnBluetooth.setText(R.string.ActivarBluetooth);

        btnBluetooth.setOnClickListener(v->{
            if(bAdapter.isEnabled())
                bAdapter.disable();
            else
            {
                bAdapter.enable();
            }
        });

        btnBuscarDispositivo.setOnClickListener(v->{
            if(arrayDevices != null)
                arrayDevices.clear();

            // Iniciamos la busqueda de dispositivos y mostramos el mensaje de que el proceso ha comenzado
            if(bAdapter.startDiscovery())
                Toast.makeText(this, "Iniciando búsqueda de dispositivos bluetooth", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Error al iniciar búsqueda de dispositivos bluetooth", Toast.LENGTH_SHORT).show();
        });

    }
    // Instanciamos un BroadcastReceiver que se encargara de detectar cuando
// un dispositivo es descubierto.
    private final BroadcastReceiver bReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // Cada vez que se descubra un nuevo dispositivo por Bluetooth, se ejecutara
            // este fragmento de codigo
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction()))
            {
                // Si el array no ha sido aun inicializado, lo instanciamos
                if(arrayDevices == null)
                    arrayDevices = new ArrayList<BluetoothDevice>();

                // Extraemos el dispositivo del intent mediante la clave BluetoothDevice.EXTRA_DEVICE
                BluetoothDevice dispositivo = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // Añadimos el dispositivo al array
                arrayDevices.add(dispositivo);

                // Le asignamos un nombre del estilo NombreDispositivo [00:11:22:33:44]
                String descripcionDispositivo = dispositivo.getName() + " [" + dispositivo.getAddress() + "]";

                // Mostramos que hemos encontrado el dispositivo por el Toast
                Toast.makeText(getBaseContext(), getString(R.string.DetectadoDispositivo) + ": " + descripcionDispositivo, Toast.LENGTH_SHORT).show();
            }

            // Codigo que se ejecutara cuando el Bluetooth finalice la busqueda de dispositivos.
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction()))
            {
                // Instanciamos un nuevo adapter para el ListView mediante la clase que acabamos de crear
                ArrayAdapter arrayAdapter = new BluetoothDeviceArrayAdapter(getBaseContext(), android.R.layout.simple_list_item_2, arrayDevices);

                lvDispositivos.setAdapter(arrayAdapter);
                Toast.makeText(getBaseContext(), "Fin de la búsqueda", Toast.LENGTH_SHORT).show();
            }
            else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction()))
            {
                // Solicitamos la informacion extra del intent etiquetada como BluetoothAdapter.EXTRA_STATE
                // El segundo parametro indicara el valor por defecto que se obtendra si el dato extra no existe
                int estado = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
                switch (estado)
                {
                    // Apagado
                    case BluetoothAdapter.STATE_OFF:
                    {
                        ((Button)findViewById(R.id.btnBluetooth)).setText(R.string.ActivarBluetooth);
                        break;
                    }

                    // Encendido
                    case BluetoothAdapter.STATE_ON:
                    {
                        ((Button)findViewById(R.id.btnBluetooth)).setText(R.string.DesactivarBluetooth);
                        // Lanzamos un Intent de solicitud de visibilidad Bluetooth, al que añadimos un par
                        // clave-valor que indicara la duracion de este estado, en este caso 120 segundos
                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
                        startActivity(discoverableIntent);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    };

    private void registrarEventosBluetooth()
    {
        // Registramos el BroadcastReceiver que instanciamos previamente para
        // detectar las distintos acciones que queremos recibir
        IntentFilter filtro = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filtro.addAction(BluetoothDevice.ACTION_FOUND);
        filtro.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(bReceiver, filtro);
    }
    // Ademas de realizar la destruccion de la actividad, eliminamos el registro del
    // BroadcastReceiver.
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(bReceiver);
    }
}