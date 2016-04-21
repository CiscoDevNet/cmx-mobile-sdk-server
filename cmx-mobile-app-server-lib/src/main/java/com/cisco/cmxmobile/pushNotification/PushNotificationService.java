/**
 * A Thread pool which manage threads to send push notification messages.
 * Whenever a containment event received, thread from pool execute that runnable
 * object
 */

package com.cisco.cmxmobile.pushNotification;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.cisco.cmxmobile.utils.CmxProperties;

@Component
public class PushNotificationService {

    // Number of pool size
    int poolSize = CmxProperties.getInstance().getPushNotificationPoolSize();

    // Maximum number of pool size
    int maxPoolSize = CmxProperties.getInstance().getPushNotificationMaxPoolSize();

    // Time to alive a pool thread
    long keepAliveTime = CmxProperties.getInstance().getPushNotificationKeepAliveTime();

    ThreadPoolExecutor threadPool = null;

    final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(1000);

    public PushNotificationService() {
        threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, queue);
    }

    public void runTask(Runnable task) {
        threadPool.execute(task);
    }

    public void shutDown() {
        threadPool.shutdown();
    }
}
