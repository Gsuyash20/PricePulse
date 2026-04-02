package org.pricepulse.auth.utility;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.ToDoubleFunction;

@Component
@RequiredArgsConstructor
public class RandomizerUtil {
  private static final SecureRandom secureRandom = new SecureRandom();
  private static final int HARDWARE_ENTROPY_BYTES = 32; // 256 bits
  private static final int OS_ENTROPY_BYTES = 16; // 128 bits
  private static final int SYSTEM_ENTROPY_BYTES = 8; // 64 bits
  private static final int ADDITIONAL_ENTROPY_BYTES = 16;

  public <T> List<T> selectNRandomElements(List<T> list, int n) {
    if (list == null || list.isEmpty()) {
      throw new IllegalArgumentException("list is null or empty");
    }
    if (n < 1) {
      throw new IllegalArgumentException("n must be greater than or equal to 1");
    }

    if (n >= list.size()) {
      List<T> shuffledList = new ArrayList<>(list);
      SecureRandom secureRandom1 = createSecureRandomWithMixedEntropy();
      Collections.shuffle(shuffledList, secureRandom1);
      return shuffledList;
    }

    // use fischer-yates shuffle algorithm for efficient selection
    SecureRandom secureRandom2 = createSecureRandomWithMixedEntropy();
    List<T> result = new ArrayList<>(n);
    List<T> workingList = new ArrayList<>(list);

    for (int i = 0; i < n; i++) {
      int randomIndex = secureRandom2.nextInt(workingList.size());
      result.add(workingList.remove(randomIndex));
    }
    return result;
  }

  private SecureRandom createSecureRandomWithMixedEntropy() {
    byte[] entropy = collectMultiSourceEntropy();
    return new SecureRandom(entropy);
  }

  private byte[] collectMultiSourceEntropy() {
    byte[] hardwareEntropy = new byte[HARDWARE_ENTROPY_BYTES];
    secureRandom.nextBytes(hardwareEntropy);

    byte[] osEntropy = collectOSEntropy();

    byte[] systemEntropy = collectSystemEvents();

    return mixEntropyCryptographically(hardwareEntropy, osEntropy, systemEntropy);
  }

  private byte[] mixEntropyCryptographically(byte[]... entropySources) {
    try {
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

      for (byte[] entropySource : entropySources) {
        if (entropySource != null) {
          sha256.update(entropySource);
        }
      }

      byte[] additional = new byte[ADDITIONAL_ENTROPY_BYTES];
      secureRandom.nextBytes(additional);
      sha256.update(additional);
      return sha256.digest();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private byte[] collectSystemEvents() {
    byte[] systemEntropy = new byte[SYSTEM_ENTROPY_BYTES];

    long systemNanoTime1 = System.nanoTime();
    long systemNanoTime2 = System.nanoTime();
    long currentTimeMillis = System.currentTimeMillis();

    for (int i = 0; i < SYSTEM_ENTROPY_BYTES; i += 2) {
      if (i < OS_ENTROPY_BYTES - 1) {
        systemEntropy[i] = (byte) (systemNanoTime1 >>> (i * 8));
        systemEntropy[i + 1] = (byte) ((systemNanoTime2 + currentTimeMillis) >>> (i * 8));
      }
    }

    secureRandom.nextBytes(systemEntropy);

    return systemEntropy;
  }

  private static byte[] collectOSEntropy() {
    byte[] osEntropy = new byte[OS_ENTROPY_BYTES];

    long systemNanoTime1 = System.nanoTime();
    long currentTimeMillis1 = System.currentTimeMillis();
    long systemNanoTime2 = System.nanoTime();
    long currentTimeMillis2 = System.currentTimeMillis();

    for (int i = 0; i < OS_ENTROPY_BYTES; i += 4) {
      if (i < OS_ENTROPY_BYTES - 3) {
        osEntropy[i] = (byte) (systemNanoTime1 >>> (i * 8));
        osEntropy[i + 1] = (byte) (currentTimeMillis1 >>> (i * 8));
        osEntropy[i + 2] = (byte) (systemNanoTime2 >>> (i * 8));
        osEntropy[i + 3] = (byte) (currentTimeMillis2 >>> (i * 8));
      }
    }

    secureRandom.nextBytes(osEntropy);

    return osEntropy;
  }

  public <T> List<T> weightedRandomSelectionES(
      Collection<T> items,
      ToDoubleFunction<T> weightExtractor,
      int n
  ) {
    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("items is null or empty");
    }
    if (n < 1) {
      throw new IllegalArgumentException("n must be >= 1");
    }

    SecureRandom random = createSecureRandomWithMixedEntropy();

    PriorityQueue<Map.Entry<T, Double>> pq =
        new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));

    for (T item : items) {

      double weight = weightExtractor.applyAsDouble(item);

      if (weight < 0) {
        throw new IllegalArgumentException("Weight cannot be negative");
      }

      if (weight == 0) continue;

      double u = random.nextDouble();
      double key = Math.pow(u, 1.0 / weight);

      pq.offer(new AbstractMap.SimpleEntry<>(item, key));

      if (pq.size() > n) {
        pq.poll();
      }
    }

    List<T> result = new ArrayList<>();
    while (!pq.isEmpty()) {
      result.add(pq.poll().getKey());
    }

    return result;
  }

}
