
package de.unikn.inf.disy.nuntifix.dtn.simulator.bloomfilter;

/**
 * Implements a universal hash function use with the counting bloom filter and
 * the fast hash table. see Song, H., Dharmapurikar, S., Turner, J., Lockwood,
 * J. "Fast Hash Table Lookup Using Extended Bloom Filter", SIGCOMM 2005 and
 * Carter, J. L., Wegman, m. n. "Universal Classes of Hash Functions" for
 * further details.
 * 
 * @author Thomas Zink
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public final class Hash {

  /*****************************************************************************
   * MEMBERS
   ****************************************************************************/
  private final ArrayList<Integer> _rndtbl;

  private final int _k;

  private final int _bits;

  /*****************************************************************************
   * CONSTRUCTORS
   ****************************************************************************/
  public Hash(final int k, final int bits, final ArrayList<Integer> rndtbl) {

    _k = k;
    _bits = bits;
    _rndtbl = rndtbl;
  }

  /**
   * @param k
   *          number of hash functions
   * @param bits
   *          number of bits to check, ie ddress space of hash value
   * @param m
   *          maximum random number to generate
   */
  public Hash(final int k, final int bits, final int m) {

    this(k, bits, generateRandomTable(k * bits, m - 1));
  }

  public Hash(final int k, final int m) {

    this(k, 32, m);
  }

  /*****************************************************************************
   * PUBLIC METHODS
   ****************************************************************************/
  /**
   * hashes the key
   * 
   * @key the key to has
   */
  public final Integer[] hash(final Object key) {

    /* convert the key to long value for hashing */
    Long lkey = ktol(key) + 1;
    /* do the hashing */
    Integer[] hashs = calcHash(lkey);
    /* remove dubs and select a number of hash values */
    return hashs;
  }

  /**
   * converts a generic key to a long value
   * 
   * @param key
   * @return the converted key
   */
  public final long ktol(final Object key) {

    long lkey;
    if (key instanceof Long)
      lkey = (Long) key;
    else if (key instanceof Number)
      lkey = ((Number) key).longValue();
    else if (key instanceof String)
      lkey = new BigInteger(((String) key).getBytes()).longValue();
    else {
      ByteArrayOutputStream byteout = new ByteArrayOutputStream();
      try {
        ObjectOutputStream objectout = new ObjectOutputStream(byteout);
        objectout.writeObject(key);
      } catch (IOException e) {
        lkey = 0L;
      }
      lkey = new BigInteger(byteout.toByteArray()).longValue();
    }
    return lkey;
  }

  /**
   * calculates the k hash values
   * 
   * @param key
   *          the key as a long value
   * @return an array of hash k values
   */
  private final Integer[] calcHash(final Long key) {

    Integer[] hash = new Integer[_k];
    Arrays.fill(hash, 0);
    int r = 0;
    for (int i = 0; i < _k; i++) {
      for (int b = 0; b < _bits; b++, r++) {
        hash[i] ^= (((key >> b) & 1) == 1) ? _rndtbl.get(r) : 0;
      }
    }
    return hash;
  }

  /*****************************************************************************
   * SETTERS AND GETTERS
   ****************************************************************************/
  private final int get(final int i) {

    return _rndtbl.get(i);
  }

  public final ArrayList<Integer> get() {

    return _rndtbl;
  }

  /*****************************************************************************
   * UTILITY METHODS
   ****************************************************************************/
  public final static ArrayList<Integer> generateRandomTable(final int length,
      final int max) {

    ArrayList<Integer> rndtbl = new ArrayList<Integer>(length);
    Random rnd = new Random(0x5DEECE66DL);
    for (int h = 0; h < length; h++)
      rndtbl.add(rnd.nextInt(max));
    return rndtbl;
  }
}
