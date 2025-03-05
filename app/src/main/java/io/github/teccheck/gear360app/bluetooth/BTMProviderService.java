package io.github.teccheck.gear360app.bluetooth;

import android.content.Context;
import android.util.Log;

import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAAgentV2;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;

import java.io.IOException;
import java.util.Arrays;

public class BTMProviderService extends SAAgentV2 {

    private static final String TAG = "BTMProviderService";

    private BTMProviderConnection providerConnection = null;
    private StatusCallback callback = null;
    private boolean isConnected = false;

    public BTMProviderService(Context context) {
        super(TAG, context, BTMProviderConnection.class);
        Log.d(TAG, "BTMProviderService");
    }

    public void setup(StatusCallback callback) {
        Log.d(TAG, "setup");
        this.callback = callback;
    }

    public void findSaPeers() {
        Log.d(TAG, "findSaPeers");
        if (!isConnected) {
            isConnected = true;
            findPeerAgents();
        }
    }

    public void send(int channel, byte[] data) {
        if (providerConnection != null) {
            try {
                providerConnection.send(channel, data);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else {
            Log.w(TAG, "providerConnection is null");
        }
    }

    public boolean closeConnection() {
        Log.d(TAG, "closeConnection");
        if (providerConnection == null)
            return false;

        providerConnection.close();
        providerConnection = null;

        releaseAgent();
        return true;
    }

    @Override
    protected void onFindPeerAgentsResponse(SAPeerAgent[] peerAgents, int result) {
        Log.d(TAG, "onFindPeerAgentsResponse " + Arrays.toString(peerAgents) + " " + result);

        if ((result == SAAgent.PEER_AGENT_FOUND) && (peerAgents != null)) {
            for (SAPeerAgent peerAgent : peerAgents) {
                requestServiceConnection(peerAgent);
            }
        }
    }

    @Override
    protected void onServiceConnectionRequested(SAPeerAgent peerAgent) {
        Log.d(TAG, "onServiceConnectionRequested " + peerAgent);
        //if (peerAgent != null)
        acceptServiceConnectionRequest(peerAgent);
    }

    @Override
    protected void onError(SAPeerAgent peerAgent, String errorMessage, int errorCode) {
        super.onError(peerAgent, errorMessage, errorCode);
        Log.d(TAG, "onError " + peerAgent + " " + errorMessage + " " + errorCode);
    }

    @Override
    protected void onServiceConnectionResponse(SAPeerAgent peerAgent, SASocket socket, int result) {
        Log.d(TAG, "onServiceConnectionResponse " + peerAgent + " " + socket + " " + result);
        if (result == SAAgentV2.CONNECTION_SUCCESS) {
            if (socket != null) {
                Log.d(TAG, "setting up provider connection");
                providerConnection = (BTMProviderConnection) socket;

                if (callback != null)
                    callback.onConnectDevice(peerAgent.getAccessory().getName(), peerAgent.getPeerId(), peerAgent.getAccessory().getProductId());
            }

        } else if (result == SAAgentV2.CONNECTION_ALREADY_EXIST) {
            Log.d(TAG, "Agent already connected");
        }
    }

    public class BTMProviderConnection extends SASocket {
        public BTMProviderConnection() {
            super(BTMProviderConnection.class.getName());
            Log.d(TAG, "BTMProviderConnection");
        }

        @Override
        public void onReceive(int channelId, byte[] data) {
            if (callback != null)
                callback.onReceive(channelId, data);
        }

        @Override
        public void onError(int channelId, String errorString, int error) {
            Log.d(TAG, "onError " + channelId + " " + errorString + " " + error);
            if (callback != null)
                callback.onError(error);
        }

        @Override
        public void onServiceConnectionLost(int errorCode) {
            Log.d(TAG, "onServiceConnectionLost " + errorCode);
            if (callback != null)
                callback.onServiceDisconnection();
            isConnected = false;
        }
    }

    public interface StatusCallback {
        void onConnectDevice(String name, String peer, String product);

        void onError(int result);

        void onReceive(int channelId, byte[] data);

        void onServiceDisconnection();
    }
}
