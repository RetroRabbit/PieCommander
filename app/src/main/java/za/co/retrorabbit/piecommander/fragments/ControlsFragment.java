package za.co.retrorabbit.piecommander.fragments;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.devpaul.analogsticklib.AnalogStick;
import com.devpaul.analogsticklib.OnAnalogMoveListener;
import com.devpaul.analogsticklib.Quadrant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import za.co.retrorabbit.piecommander.R;
import za.co.retrorabbit.piecommander.SampleGattAttributes;
import za.co.retrorabbit.piecommander.views.main.BluetoothLeService;
import za.co.retrorabbit.piecommander.views.main.MainActivity;

public class ControlsFragment extends Fragment implements OnAnalogMoveListener, View.OnTouchListener {

    private final static String TAG = ControlsFragment.class.getSimpleName();
    @BindView(R.id.fragment_control_stick_analog_stick)
    AnalogStick analogStick;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    // Toolbar toolbar;
    private int COMMAND_TIME = 100;
    private double CONSTSPEEDCHANGE = 0.55;
    private Handler handler;
    private int commandIndex = -1;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private OnFragmentInteractionListener mListener;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                analogStick.setEnabled(true);

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                analogStick.setEnabled(false);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(getBluetoothLeService().getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private boolean sendCommands;
    private MoveData moveData = new MoveData();

    public ControlsFragment() {
        // Required empty public constructor
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public static ControlsFragment newInstance() {
        ControlsFragment fragment = new ControlsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void updateConnectionState(final int resourceId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //    toolbar.setTitle("CONNECTED");
                Toast.makeText(getContext(), getString(resourceId), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayData(final String data) {
        if (data != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // toolbar.setTitle(data);

                    Toast.makeText(getContext(), data, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onResume() {
        super.onResume();
        if (mGattUpdateReceiver != null)
            getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGattUpdateReceiver != null)
            getActivity().unregisterReceiver(mGattUpdateReceiver);
    }

    public DatabaseReference dataReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();

        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_controls, container, false);
        ButterKnife.bind(this, view);
        //   toolbar = (Toolbar) view.getRootView().findViewById(R.id.toolbar);
        analogStick.setOnAnalogMoveListner(this);
        analogStick.setOnTouchListener(this);
        analogStick.setEnabled(false);
        analogStick.setMaxYValue(1f);
        analogStick.setMaxXValue(1f);
        return view;
    }

    public void bindReferences() {
        if (dataReference != null || getCurrentDevice() == null)
            return;

        dataReference = FirebaseDatabase.getInstance().getReference().child("robot").child(getCurrentDevice().getName());
        dataReference.setValue(moveData);
        dataReference.keepSynced(true);
        dataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                moveData = dataSnapshot.getValue(MoveData.class);
                if (moveData.isOnline())
                    processCommands();
                else
                    stopCommands();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
    public void onAnalogMove(float x, float y) {
        moveData.setMoveX(x);
        moveData.setMoveY(y);
    }

    @Override
    public void onAnalogMovedScaledX(float scaledX) {
        moveData.setScaledX(scaledX);

    }

    @Override
    public void onAnalogMovedScaledY(float scaledY) {
        moveData.setScaledY(scaledY);
    }

    @Override
    public void onAnalogMovedGetAngle(float angle) {
        moveData.setAngle(angle);
    }

    int powerLeft = 1, powerRight = 1;
    float angle;
    int def;


    private void manualCalculate() {

        float angle = moveData.angle;

        if (angle > 0 && angle <= 90)
            onAnalogMovedGetQuadrant(Quadrant.BOTTOM_RIGHT);
        else if (angle > 90 && angle <= 180)
            onAnalogMovedGetQuadrant(Quadrant.BOTTOM_LEFT);
        else if (angle > 180 && angle <= 270)
            onAnalogMovedGetQuadrant(Quadrant.TOP_LEFT);
        else if (angle > 270 && angle <= 360)
            onAnalogMovedGetQuadrant(Quadrant.TOP_RIGHT);

    }

    @Override
    public void onAnalogMovedGetQuadrant(Quadrant quadrant) {

        float speedFactor = moveData.scaledX > moveData.scaledY ? moveData.scaledX : moveData.scaledY;
        int calcSpeed = (int) Math.round(128 * speedFactor);
        int constSpeed = calcSpeed < 60 ? 65 : calcSpeed;
        switch (quadrant) {
            case TOP_LEFT:
                angle = 90 - (moveData.angle - 180);
                def = (int) Math.round(angle * CONSTSPEEDCHANGE);
                powerLeft = constSpeed - def;
                powerRight = constSpeed + def;

                break;
            case TOP_RIGHT:
                angle = (moveData.angle - 270);
                def = (int) Math.round(angle * CONSTSPEEDCHANGE);
                powerLeft = constSpeed + def;
                powerRight = constSpeed - def;
                break;
            case BOTTOM_LEFT:
                powerLeft = -1;
                powerRight = -1;
                break;
            case BOTTOM_RIGHT:
                powerLeft = -1;
                powerRight = -1;
                break;
        }
    }

    private void processCommands() {

        Observable.just(moveData)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Action1<MoveData>() {
                    @Override
                    public void call(MoveData movedata) {
                        System.out.println("MOVE DATA : \n" + moveData.toString());

                        if (movedata.isOnline())
                            manualCalculate();

                        moveCommand(Math.min(powerRight, 127), Math.min(powerLeft, 127), COMMAND_TIME);

                        try {
                            Thread.sleep(COMMAND_TIME);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (sendCommands || movedata.isOnline())
                            processCommands();
                    }
                });
    }


    private void stopCommands() {
        moveCommand(1, 1, COMMAND_TIME);
    }

    public int incrementCommandIndex() {
        commandIndex++;
        if (commandIndex > 255) {
            commandIndex = 0;
        }
        return commandIndex;
    }

    public void moveCommand(int speedLeft, int speedRight, int time) {

        int commandIndex = incrementCommandIndex();
        int commandBehaviour = 0;

        char commandType = 'M'; // indicator M for move

        //length of the command payload is 6 bytes header is always eight bytes
        int commandLength = 14; //2 bytes

        //create an array buffer of commandLength bytes
        //create a dataview for the buffer
        byte[] dataView = new byte[commandLength];

        //header = bytes 0 to 7
        dataView[0] = (byte) commandIndex;
        dataView[1] = (byte) commandBehaviour;
        dataView[2] = (byte) commandLength;
        dataView[7] = (byte) commandType;
        //commandPayload = bytes 8 to 20
        dataView[8] = (byte) speedLeft;
        dataView[9] = (byte) speedRight;

        dataView[10] = (byte) (speedLeft > 0 ? 0 : 1);
        dataView[11] = (byte) (speedRight > 0 ? 0 : 1);

        dataView[12] = (byte) time;

        sendToBluetoothService(dataView);
    }

    public void stopCommand(int speedLeft, int speedRight, int time) {

        int commandIndex = incrementCommandIndex();
        int commandBehaviour = 0;

        char commandType = 'S'; // indicator M for move

        //length of the command payload is 4 bytes header is always eight bytes
        int commandLength = 8; //2 bytes

        //create an array buffer of commandLength bytes
        //create a dataview for the buffer
        byte[] dataView = new byte[commandLength];

        //header = bytes 0 to 7
        dataView[0] = (byte) commandIndex;
        dataView[1] = (byte) commandBehaviour;
        dataView[2] = (byte) commandLength;
        dataView[7] = (byte) commandType;

        sendToBluetoothService(dataView);
    }

    public void toggleCommand(ToggleFlag deviceFlags, ToggleState state, int onTime, int offTime) {
        //toggle robot
        int commandIndex = incrementCommandIndex();
        int commandBehaviour = 0;

        char commandType = 'T';

        //length of the command payload is 7 bytes, header is always eight bytes
        int commandLength = 15; //2 bytes

        //create an array buffer of commandLength bytes
        //create a dataview for the buffer
        byte[] dataView = new byte[commandLength];


        //header = bytes 0 to 7
        //commandIndex
        dataView[0] = (byte) commandIndex;
        dataView[1] = (byte) commandBehaviour;
        dataView[2] = (byte) commandLength;
        dataView[7] = (byte) commandType;
        //commandPayload = bytes 8 to 20
        dataView[8] = (byte) deviceFlags.getValue();
        dataView[10] = (byte) state.getValue();
        dataView[11] = (byte) onTime;
        dataView[13] = (byte) offTime;


        sendToBluetoothService(dataView);
    }

    private void sendToBluetoothService(byte[] dataView) {

        if (getCurrentGatt() == null) {
            Toast.makeText(this.getContext(), "Connect a robot", Toast.LENGTH_SHORT).show();
            return;
        }
        Observable.just(dataView)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Action1<byte[]>() {
                    @Override
                    public void call(byte[] bytes) {
                        BluetoothGattCharacteristic out = getCurrentGatt().getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")).getCharacteristic(UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e"));

                        out.setValue(bytes);
                        out.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                        boolean gatt1 = getCurrentGatt().writeCharacteristic(out);
                       /* try {
                            Thread.sleep(1000);
                            stopCommand(0, 0, 0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } */
                    }
                });

    }

    public BluetoothLeService getBluetoothLeService() {
        return ((MainActivity) getActivity()).getBluetoothLeService();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                sendCommands = true;
                moveData.setOnline(false);
                processCommands();

                break;
            case MotionEvent.ACTION_UP:
                sendCommands = false;
                moveData.setOnline(false);
                stopCommands();
        }
        return false;
    }

    @OnClick(R.id.fragment_control_disconnect_button)
    void disconnect() {
        if (getBluetoothLeService() != null)
            getBluetoothLeService().disconnect();
    }

    @OnClick(R.id.fragment_control_stop_button)
    void stop() {
        stopCommands();
        stopCommands();
        stopCommands();
        stopCommands();
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
