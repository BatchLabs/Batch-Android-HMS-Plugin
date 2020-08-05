package com.batch.android.plugin.hms;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.huawei.hms.push.RemoteMessage;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class PushHelperTest {

    @Test
    public void testMessageConversion() throws Exception {
        Bundle expectedBundle = new Bundle();
        expectedBundle.putString("foo", "bar");
        expectedBundle.putString("number", "23");
        expectedBundle.putString("test", "{\"foo\":\"baz\"}");


        Bundle convertedBundle = PushHelper.huaweiMessageToReceiverBundle(hmsMessageForBundle(expectedBundle));
        Assert.assertNotNull(convertedBundle);
        Assert.assertTrue(TestUtils.equalBundles(expectedBundle, convertedBundle));
    }

    private RemoteMessage hmsMessageForBundle(Bundle source) throws Exception {
        // Internal HMS format, test might break because of this assumption
        JSONObject root = new JSONObject();
        JSONObject message = new JSONObject();
        JSONObject data = new JSONObject();
        for (String key : source.keySet()) {
            data.put(key, JSONObject.wrap(source.get(key)));
        }
        message.put("data", data.toString());
        root.put("msgContent", message);

        Bundle internalHMSBundle = new Bundle();
        byte[] byteRoot = root.toString().getBytes();
        internalHMSBundle.putByteArray("message_body", byteRoot);

        return new RemoteMessage(internalHMSBundle);
    }
}
