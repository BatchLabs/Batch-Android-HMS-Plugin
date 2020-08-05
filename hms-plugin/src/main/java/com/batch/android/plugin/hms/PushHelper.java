package com.batch.android.plugin.hms;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.huawei.hms.push.RemoteMessage;

import java.util.Map;

public class PushHelper {

    /**
     * Convert a Huawei RemoteMessage to a bundle, for compatibility with all the methods that used
     * to deal with a BroadcastReceiver
     * @param message RemoteMessage to convert
     * @return Converted Bundle, null if error
     */
    @Nullable
    public static Bundle huaweiMessageToReceiverBundle(@Nullable RemoteMessage message)
    {
        if (message == null) {
            return null;
        }

        Map<String, String> data = message.getDataOfMap();
        if (data == null || data.size() == 0) {
            return null;
        }

        Bundle retVal = new Bundle();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            retVal.putString(entry.getKey(), entry.getValue());
        }
        return retVal;
    }

}
