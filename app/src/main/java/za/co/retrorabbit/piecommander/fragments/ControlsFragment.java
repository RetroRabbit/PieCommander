package za.co.retrorabbit.piecommander.fragments;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devpaul.analogsticklib.AnalogStick;
import com.devpaul.analogsticklib.OnAnalogMoveListener;
import com.devpaul.analogsticklib.Quadrant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import za.co.retrorabbit.piecommander.BluetoothLeService;
import za.co.retrorabbit.piecommander.MainActivity;
import za.co.retrorabbit.piecommander.R;
import za.co.retrorabbit.piecommander.SampleGattAttributes;

import static android.content.Context.BIND_AUTO_CREATE;

public class ControlsFragment extends Fragment implements OnAnalogMoveListener {

    private final static String TAG = ControlsFragment.class.getSimpleName();

    @BindView(R.id.fragment_control_stick_analog_stick)
    AnalogStick analogStick;

    Toolbar toolbar;

    private OnFragmentInteractionListener mListener;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(getBluetoothLeService().getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void updateConnectionState(final int resourceId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle(resourceId);
            }
        });
    }

    private void displayData(final String data) {
        if (data != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toolbar.setTitle(data);
                }
            });
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

    }


    public ControlsFragment() {
        // Required empty public constructor
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    }

    public static ControlsFragment newInstance() {
        ControlsFragment fragment = new ControlsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_controls, container, false);
        ButterKnife.bind(this, view);
        toolbar = (Toolbar) view.getRootView().findViewById(R.id.toolbar);
        analogStick.setOnAnalogMoveListner(this);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public boolean bindService(Intent service, ServiceConnection conn,
                               int flags) {
        return getActivity().bindService(service, conn, flags);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    BluetoothDevice getCurrentDevice() {
        return ((MainActivity) getActivity()).getCurrentDevice();
    }

    BluetoothGatt getCurrentGatt() {
        return ((MainActivity) getActivity()).getCurrentGatt();
    }


    @Override
    public void onAnalogMove(float v, float v1) {

    }

    @Override
    public void onAnalogMovedScaledX(float v) {

    }

    @Override
    public void onAnalogMovedScaledY(float v) {

    }

    @Override
    public void onAnalogMovedGetAngle(float v) {

    }

    @Override
    public void onAnalogMovedGetQuadrant(Quadrant quadrant) {

        switch (quadrant) {
            case TOP_LEFT:
                byte[] value = new byte[20];
                value[0] = (byte) (0);
                value[1] = (byte) (0);
                value[2] = (byte) (1);
                value[3] = (byte) (1);
                value[4] = (byte) (1);
                value[5] = (byte) (1);
                value[6] = (byte) (1);
                value[7] = (byte) (1);
         /*   mGattCharacteristics.set(0).setValue(value);
                boolean status = getCurrentGatt().writeCharacteristic(charac);*/
                break;
        }
    }

    public BluetoothLeService getBluetoothLeService() {
        return ((MainActivity) getActivity()).getBluetoothLeService();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
