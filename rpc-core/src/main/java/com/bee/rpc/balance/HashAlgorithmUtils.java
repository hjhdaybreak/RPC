package com.bee.rpc.balance;

/**
 * FNV hash算法对一个字符串计算,可以得到一个唯一确定的无符号整数值。对于大量的随机输入字符串，
 * 比如UUID串,得到的无符号整数值,通过简单的取余运算，基本上是均匀分布的。比如,对 100,000个UUID字符串
 * 做 FNV Hash计算,
 * 得到的每个结果值hashValue，都做 hashValue %= 10,000，其结果基本上是在 0 ~ 9,999
 * 范围内均匀分布的。但是请注意,是 “基本上“ 均匀分布,事实上还存在一定的偏差。
 * <p>
 * CRC32 以太网帧的末尾 FCS 帧校验序列 通过CRC算法得出
 * Redis CRC16
 * HTTPS SHA-1
 */

public class HashAlgorithmUtils {

    public static class FNV1_32_HASH {
        public static int getHash(String str) {
            final int p = 16777619;
            int hash = (int) 2166136261L;
            for (int i = 0; i < str.length(); i++) {
                hash = (hash ^ str.charAt(i)) * p;
            }
            hash += hash << 13;
            hash ^= hash >> 7;
            hash += hash << 3;
            hash ^= hash >> 17;
            hash += hash << 5;
            if (hash < 0) {
                hash = Math.abs(hash);
            }
            return hash;
        }
    }

}
