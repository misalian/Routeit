
package de.unikn.inf.disy.nuntifix.dtn.simulator.bloomfilter;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList; // import java.util.Vector;
import java.util.Set;

import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Device;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.MeetingStats;

public class MyBloomFilter {

  public LinkedList<BloomValue> bloom;

  //public ArrayList<BloomValue> vbloom;
  public Hashtable<Integer, BloomValue>hbloom;
  
  public MyBloomFilter(int size) {

    /*
     * bloom = new LinkedList<BloomValue>(); bloom.add(new
     * BloomValue(0,size,0));
     */
    hbloom = new Hashtable<Integer, BloomValue>();
    //vbloom = new ArrayList<BloomValue>(512);

  }
  public final int hsize(){
    return hbloom.size();
  }
  public BloomValue hget(Integer key){
    return hbloom.get(key);    
  }
  public void hput(Integer key, BloomValue bvtemp){
    hbloom.put(key,bvtemp);
  }
  public void hreplace(Integer key, BloomValue bvtemp){
    hbloom.remove(key);
    hput(key, bvtemp);
  }
  public Double hgetval(Integer key){
    return hbloom.get(key).proximity;
  }
  public Integer hgetdepth(Integer key){
    return hbloom.get(key).depth;
  }
  public boolean difference(MyBloomFilter mbf, int depth){
    int count=0;
    int rsize=0;
    Set<Integer> keySet2=mbf.hbloom.keySet();
    Enumeration<Integer> kenum= hbloom.keys();
    Integer key;
    while(kenum.hasMoreElements()){
      key = kenum.nextElement();
      BloomValue bvtemp = hget(key);
      if(!keySet2.contains(key)){
        if(bvtemp.depth<=depth)
          count++;
      }
      else
      {
       
        BloomValue bvtemp2 = mbf.hget(key);
        if(bvtemp2.depth>=depth & bvtemp.depth>=depth){
          rsize++;
          continue;
        }
          
        if(!bvtemp.depth.equals(bvtemp2.depth)){
          count++;
          continue;
        }
        if(!bvtemp.proximity.equals(bvtemp2.proximity))
          count++;
      }
    }
    Set<Integer> keySet=hbloom.keySet();
    Enumeration<Integer> kenum2= mbf.hbloom.keys();
    while(kenum2.hasMoreElements()){
      key = kenum2.nextElement();
      BloomValue bvtemp2 = mbf.hget(key);
      if(!keySet.contains(key)){
        if(bvtemp2.depth<=depth)
          count++;
      }
    }
    if((count*2)/(float)(hsize()+mbf.hsize()-(rsize*2))>0.5)
      return true;
    return false;
 
  }
 /* public final long llength() {

    return bloom.getLast().vindex + bloom.getLast().count;
  }*/
  /*public final int size(){
    return vbloom.size();
  }*/
  /*public BloomValue get(int index){
    return vbloom.get(index);
  }*/

 /* public final int vlength() {

    return vbloom.size();
  }*/
  

  /*public void vput(Integer pos, int depth, double val) {

    vbloom.add(new BloomValue(pos, depth, val));
  }*/

  /*public void vreplace(int index, int d, double val) {

    vbloom.get(index).proximity = new Double(val);
    vbloom.get(index).depth = new Integer(d);

  }*/
  /*public int vgetrindex(Integer  index) {

    for (int i = 0; i < vbloom.size(); i++) {
      if (vbloom.get(i).vindex.equals(index))
        return i;
    }
    return -1;
  }*/

 /* public double vgetval(int index) {

    return vbloom.get(index).proximity;
  }*/
  /*public Integer vgetdepth(int index) {

    return vbloom.get(index).depth;
  }*/

  /*public Integer vgetvindex(int index) {

    return vbloom.get(index).vindex;
  }*/
  //public void lput(int pos, int count, double val) {

   /* int index = -1, i = 0, nextindex = 0;
    double prevval = 0;
    i = findval(0, bloom.size() - 1, pos);
    index = bloom.get(i).vindex;
    try {
      if (bloom.size() - 1 > i)
        nextindex = bloom.get(i + 1).vindex;
      else
        nextindex = index + bloom.get(i).count;
      if (index + bloom.get(i).count == pos) {
        bloom.get(i).count -= count;
        if (bloom.size() > i && bloom.get(i + 1).proximity == val) {
          bloom.get(i + 1).count += count;
          bloom.get(i).count -= count;
          bloom.get(i + 1).vindex -= count;
        } else {
          bloom.add(i + 1, new BloomValue(pos, count, val));
          bloom.get(i).count -= count;
        }

      } else if (index == pos) {
        if (bloom.get(i).count == 1)
          bloom.get(i).proximity = val;
        else {
          if (i > 0 && bloom.get(i - 1).proximity == val) {
            bloom.get(i - 1).count += count;
            bloom.get(i).count -= count;
            bloom.get(i).vindex += count;
          } else {
            bloom.add(i, new BloomValue(pos, count, val));
            bloom.get(i + 1).vindex += count;
            bloom.get(i + 1).count -= count;

          }
        }
      } else if (index < pos & index + bloom.get(i).count > pos) {
        prevval = bloom.get(i).proximity;
        bloom.get(i).count = pos - index;
        bloom.add(i + 1, new BloomValue(pos, count, val));
        bloom.add(i + 2, new BloomValue(pos + count, nextindex - pos - count,
            prevval));
      }
    } catch (Exception e) {
      System.out.println(e.getMessage() + " " + e.toString() + " "
          + bloom.size());
    }

    /*
     * System.out.println(); for(i=0;i<bloom.size();i++)
     * System.out.print(bloom.get(i).index+","+bloom.get(i).proximity+","+bloom.get(i).count+"\t");
     */
    /*
     * for(i=1;i<bloom.size();i++){
     * if(bloom.get(i-1).index+bloom.get(i-1).count!=bloom.get(i).index){
     * System.out.print(bloom.get(i-1).index+","+bloom.get(i-1).proximity+","+bloom.get(i-1).count+"
     * "+pos+"\t"); System.exit(0); } }
     */

    // System.out.println(bloom.size());
  //}

  /*public int findval(int low, int high, int pos) {

    if (high < 1)
      return 0;

    int mid = (low + high) / 2;
    int index = bloom.get(mid).vindex;
    if (index <= pos & index + bloom.get(mid).count > pos)
      return mid;
    if (index > pos)
      return findval(low, mid, pos);
    if (mid + 1 >= high)
      return high;
    return findval(mid, high, pos);
  }

  public int lgetlinkindex(int pos) {

    return findval(0, bloom.size() - 1, pos);
  }

  public double lget(int index) {

    return bloom.get(index).proximity;
  }
*/
 

}
