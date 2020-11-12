
package de.unikn.inf.disy.nuntifix.dtn.simulator.bloomfilter;

/**
 * 
 */

import java.util.ArrayList;
import java.util.Arrays;

/*
 * import utils.Arrays2; import utils.ReadWriteParaMeter;
 */
/**
 * @author zink
 */
public abstract class ExtendedBloomFilter<T> {

  /*****************************************************************************
   * MEMBERS
   ****************************************************************************/
  // the bloom filter array
  T[] _bloom;

  /*****************************************************************************
   * BLOOM METHODS
   ****************************************************************************/
  /**
   * increase and decrease values
   * 
   * @param indexes
   */
  abstract public void inc(final Integer[] indexes);

  abstract public void inc(final int i);

  abstract public void dec(final Integer[] indexes);

  abstract public void dec(final int i);

  /**
   * put and get values
   * 
   * @param indexes
   */
  abstract public void put(final Integer[] indexes, final T[] values);

  abstract public void put(final Integer index, final T value);

  abstract public Object[] get(final Integer[] indexes);

  abstract public Object get(final int i);

  /*****************************************************************************
   * GETTER AND SETTER
   ****************************************************************************/
  /**
   * @return the _bloom
   */
  public final T[] get_bloom() {

    return _bloom;
  }

  /**
   * @param _bloom
   *          the _bloom to set
   */
  public final void set_bloom(T[] _bloom) {

    this._bloom = _bloom;
  }

  /**
   * @return the length
   */
  public final Integer length() {

    return _bloom.length;
  }

  /*****************************************************************************
   * STRING METHODS
   ****************************************************************************/
  /**
   * @return a String representation of this bloom filter
   */
  public String toString() {

    return _bloom.toString();
  }

}
