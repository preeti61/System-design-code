package org.systemDesign.bloomFilters;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;

public class ScalableBloomFilter {
    private BitSet bloomArray;
    private int bitSize;
    private int hashCount;
    private int elementsAdded;
    private final double falsePositiveRate;

    // Constructor with desired false positive rate
    public ScalableBloomFilter(int expectedElements, double falsePositiveRate) {
        this.falsePositiveRate = falsePositiveRate;
        this.bitSize = optimalBitSize(expectedElements, falsePositiveRate);
        this.hashCount = optimalHashCount(expectedElements, bitSize);
        this.bloomArray = new BitSet(bitSize);
        this.elementsAdded = 0;
    }

    // Calculate optimal bit size m
    private int optimalBitSize(int n, double p) {
        return (int) Math.ceil((-n * Math.log(p)) / (Math.log(2) * Math.log(2)));
    }

    // Calculate optimal number of hash functions k
    private int optimalHashCount(int n, int m) {
        return (int) Math.round((m / (double) n) * Math.log(2));
    }

    // MurmurHash function
    private int murmurHash(String value, int seed) {
        byte[] data = value.getBytes(StandardCharsets.UTF_8);
        int hash = seed;
        for (byte b : data) {
            hash ^= b;
            hash *= 0x5bd1e995;
            hash ^= hash >> 15;
        }
        return Math.abs(hash % bitSize);
    }

    // Add element to Bloom Filter
    public void add(String obj) {
        for (int i = 0; i < hashCount; i++) {
            int hash = murmurHash(obj, i);
            bloomArray.set(hash);
        }
        elementsAdded++;

        // Check if expansion is needed
        if (elementsAdded > bitSize / 2) {
            expandBloomFilter();
        }
    }

    // Check if an element *might* be present
    public boolean mightContain(String value) {
        for (int i = 0; i < hashCount; i++) {
            int hash = murmurHash(value, i);
            if (!bloomArray.get(hash)) {
                return false;
            }
        }
        return true;
    }

    // Expand Bloom Filter dynamically
    private void expandBloomFilter() {
        int newBitSize = bitSize * 2;
        BitSet newBloomArray = new BitSet(newBitSize);

        for (int i = 0; i < bitSize; i++) {
            if (bloomArray.get(i)) {
                newBloomArray.set(i);
            }
        }

        this.bitSize = newBitSize;
        this.bloomArray = newBloomArray;
        this.hashCount = optimalHashCount(elementsAdded, bitSize);
    }

    public static void main(String[] args) {
        ScalableBloomFilter bloomFilter = new ScalableBloomFilter(1000, 0.01); // 1% false positive rate

        String[] urls = {
                "https://google.com",
                "https://example.com",
                "https://stackoverflow.com",
                "https://github.com"
        };

        Arrays.stream(urls).forEach(bloomFilter::add);

        // Test lookups
        System.out.println(bloomFilter.mightContain("https://github.com")); // True
        System.out.println(bloomFilter.mightContain("https://bitbucket.com")); // False
    }
}
