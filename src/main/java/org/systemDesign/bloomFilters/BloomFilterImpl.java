package org.systemDesign.bloomFilters;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class BloomFilterImpl {
    private final BitSet bloomArray;
    private final int hashCount;

    BloomFilterImpl(int k, int sz) {
        bloomArray = new BitSet(sz);
        this.hashCount = k;
    }
    private int murmurHash(String value, int seed) {
        byte[] data = value.getBytes(StandardCharsets.UTF_8);
        int hash = seed;
        for (byte b : data) {
            hash ^= b;
            hash *= 0x5bd1e995;
            hash ^= hash >> 15;
        }
        return Math.abs(hash % bloomArray.size());
    }
    // add to bit set
    private void add(String obj) {
        int [] hashes = new int[hashCount];
        for(int i = 0 ; i < hashCount; i++) {
            int hash = murmurHash(obj, i);
            bloomArray.set(hash);
        }
    }
    // Check if an element *might* be in the set
    public boolean mightContain(String value) {
        for (int i = 0; i < hashCount; i++) {
            int hash = murmurHash(value, i);
           if(!bloomArray.get(hash)) {
               return false;
           }
        }
        // might be present
        return true;
    }

    public static void main(String[] args) {
        BloomFilterImpl bloomFilter = new BloomFilterImpl(5, 1000);
        String[] urls = {
                "https://google.com",
                "https://example.com",
                "https://stackoverflow.com",
                "https://github.com"
        };
        Arrays.stream(urls).forEach(bloomFilter::add);
        System.out.println(bloomFilter.mightContain("https://github.com"));
        System.out.println(bloomFilter.mightContain("https://bitbucket.com"));
    }
}
