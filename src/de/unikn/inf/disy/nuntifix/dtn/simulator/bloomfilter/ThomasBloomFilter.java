
package de.unikn.inf.disy.nuntifix.dtn.simulator.bloomfilter;

/**
 * 
 */

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author zink
 */
public class ThomasBloomFilter extends ExtendedBloomFilter<Double> {

  /*****************************************************************************
   * CONSTRUCTORS
   ****************************************************************************/
  /**
   * @param m
   *          length of counter array
   */

  public ThomasBloomFilter(final int m) {

    _bloom = new Double[m];
    Arrays.fill(_bloom, 0.0);
  }

  public ThomasBloomFilter() {

    this(11);
  }

  /*****************************************************************************
   * COUNTING METHODS
   ****************************************************************************/
  /*
   * (non-Javadoc)
   * 
   * @see bloomfilter.ExtendedBloomFilter#dec(java.lang.Integer[])
   */
  @Override
  public void dec(Integer[] indexes) {

    for (int i : indexes)
      dec(i);
  }

  /*
   * (non-Javadoc)
   * 
   * @see bloomfilter.ExtendedBloomFilter#dec(int)
   */
  @Override
  public void dec(int i) {

    _bloom[i]--;
  }

  /*
   * (non-Javadoc)
   * 
   * @see bloomfilter.ExtendedBloomFilter#get(java.lang.Integer[])
   */
  @Override
  public Object[] get(Integer[] indexes) {

    ArrayList<Double> tmp = new ArrayList<Double>();
    for (Integer i : indexes)
      tmp.add(_bloom[i]);
    return tmp.toArray();
  }

  /*
   * (non-Javadoc)
   * 
   * @see bloomfilter.ExtendedBloomFilter#get(int)
   */
  @Override
  public Object get(int i) {

    return _bloom[i];
  }

  /*
   * (non-Javadoc)
   * 
   * @see bloomfilter.ExtendedBloomFilter#inc(java.lang.Integer[])
   */
  @Override
  public void inc(Integer[] indexes) {

    for (int i : indexes)
      inc(i);
  }

  /*
   * (non-Javadoc)
   * 
   * @see bloomfilter.ExtendedBloomFilter#inc(int)
   */
  @Override
  public void inc(int i) {

    _bloom[i]++;
  }

  /*
   * (non-Javadoc)
   * 
   * @see bloomfilter.ExtendedBloomFilter#put(java.lang.Integer[], T[])
   */
  @Override
  public void put(Integer[] indexes, Double[] values) {

    for (int i = 0; i < indexes.length; i++)
      put(indexes[i], values[i]);
  }

  /*
   * (non-Javadoc)
   * 
   * @see bloomfilter.ExtendedBloomFilter#put(java.lang.Integer,
   *      java.lang.Object)
   */
  @Override
  public void put(Integer index, Double value) {

    _bloom[index] = value;
  }

}
