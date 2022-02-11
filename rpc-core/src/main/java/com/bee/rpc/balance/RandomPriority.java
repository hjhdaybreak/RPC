package com.bee.rpc.balance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RandomPriority implements LoadBalancer {

    ConcurrentHashMap<String, Integer> serviceWeight = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, String> indexToService = new ConcurrentHashMap<>();
    public Random random = new Random();
    int[] preSum;

    public void addService(String serviceName, int weight) {
        serviceWeight.put(serviceName, weight);

    }

    public void removeService(String serviceName) {
        serviceWeight.remove(serviceName);
    }

    private void rebuildCache() {
        preSum = new int[serviceWeight.size()];
        int index = 0;
        int sum = 0;
        for (String serviceName : serviceWeight.keySet()) {
            indexToService.put(index, serviceName);
            sum += serviceWeight.get(serviceName);
            preSum[index++] = sum;
        }
    }

    public String getServer() {
        int max = preSum[preSum.length - 1];
        int target = random.nextInt(max) + 1;
        int low = 0;
        int high = preSum.length - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (preSum[mid] >= target) {
                if (mid == 0 || preSum[mid - 1] < target) {
                    return indexToService.get(mid);
                } else {
                    high = mid - 1;
                }
            } else {
                low = mid + 1;
            }
        }
        return null;
    }
}


