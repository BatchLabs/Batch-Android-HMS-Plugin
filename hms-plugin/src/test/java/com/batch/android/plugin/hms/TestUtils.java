package com.batch.android.plugin.hms;

import android.os.Bundle;

import androidx.annotation.NonNull;

import org.junit.Assert;

import java.util.Set;

public class TestUtils {
    public static boolean equalBundles(@NonNull Bundle first, @NonNull Bundle second) {
        Assert.assertNotNull(first);
        Assert.assertNotNull(second);

        if (first == second) {
            return true;
        }

        if (first.size() != second.size()) {
            return false;
        }

        Set<String> firstKeySet = first.keySet();
        if (!firstKeySet.containsAll(second.keySet())) {
            // No need to compare the other way around thanks to the size check
            return false;
        }

        for (String key : firstKeySet) {
            Object firstObject = first.get(key);
            Object secondObject = second.get(key);
            Assert.assertNotNull(firstObject);
            Assert.assertNotNull(secondObject);

            if (firstObject instanceof Bundle) {
                if (!(secondObject instanceof Bundle)) {
                    return false;
                }
                if (!equalBundles((Bundle) firstObject, (Bundle) secondObject)) {
                    return false;
                }
            } else if (!firstObject.equals(secondObject)) {
                return false;
            }
        }

        return true;
    }
}
