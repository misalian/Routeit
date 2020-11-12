
package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;

/*
 * This class manages the time span for which a device may be connected with
 * some base stattion
 */
import java.util.*;
import java.text.*;

public class TSlot {
  
  public Hashtable<Integer, TSlot>Correspond;
  
  public Hashtable<Integer, TSlot>correspo;
  
  public LinkedList<TSlot>Simul_Trans;

  public Date timemin;

  public Date timemax;

  public String apname;
  
  public double etime;

  public boolean adjust,exact;
  
  public int channelFail;
  
 // public int count;
  
  /*public Date omin;
  public Date omax;
  
  //int src,dest;*/
  //public Hashtable<String, Device> meetings;

  //public int meetingcount;
  
  //public boolean  real; 
  
  //boolean recorded;

  public TSlot(String stime, String etime) {

    Simul_Trans = new LinkedList<TSlot>();
    Correspond = new Hashtable<Integer, TSlot>();
    timemin = new Date();
    timemax = new Date();
    apname = new String();
    if(stime.length()>19)
      stime = stime.substring(0,stime.indexOf("."));
    if(etime.length()>19)
      etime = etime.substring(0,etime.indexOf("."));
      
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    try {
      timemin = format.parse(stime);
      if(etime.equals("UL"))
        timemax.setTime(timemin.getTime()+(1000*60*5));
      else
        timemax = format.parse(etime);
    } catch (Exception e) {
      System.out.println("Unable to parse string for date" + e.getMessage()
          + e.toString() + " " + stime);
    }
    channelFail=0;

    /*
     * if(timemin.getHours()>=9 & timemin.getHours()<=17) offhrs = true;
     * if(timemax.getHours()>=9 & timemax.getHours()<=17) offhrs=true;
     */
  }

  public TSlot(long stime, long etime, String ap) {
  //  count=0;
    Simul_Trans = new LinkedList<TSlot>();
    Correspond = new Hashtable<Integer, TSlot>();
   // real = true;
    timemin = new Date(stime);
    timemax = new Date(etime);
    apname = new String(ap);
    channelFail=0;
  }

  public TSlot() {
    Simul_Trans = new LinkedList<TSlot>();
    Correspond = new Hashtable<Integer, TSlot>();
    timemin = new Date();
    timemax = new Date();
    channelFail=0;
    ///real = true;
  }

  public TSlot(Date st, Date et) {
  //  count=0;
    Simul_Trans = new LinkedList<TSlot>();
    Correspond = new Hashtable<Integer, TSlot>();
    timemin = new Date();
    timemax = new Date();

    timemin.setTime(st.getTime());
    timemax.setTime(et.getTime());
    channelFail=0;
   // real = true;
  }

  public TSlot(String time) {
    Simul_Trans = new LinkedList<TSlot>();
    Correspond = new Hashtable<Integer, TSlot>();
   // real = true;
    timemin = new Date();
    timemax = new Date();
    // offhrs = false;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    try {
      timemin = format.parse(time);
    } catch (Exception e) {
      System.out.println("Unable to parse string for date" + e.getMessage()
          + e.toString() + " " + time);
    }
    timemax.setTime(timemin.getTime() + 5 * 60 * 997);
    channelFail=0;
    /*
     * if(timemin.getHours()>=9 & timemin.getHours()<=17) offhrs = true;
     * if(timemax.getHours()>=9 & timemax.getHours()<=17) offhrs=true;
     * //System.out.print(timemin.toString() + " " + timemax.toString()+"\n");
     */
  }

  /*
   * public DeviceContact getspanslot(int hrs){ Iterator dcit =
   * devconlist.iterator(); while(dcit.hasNext()){ DeviceContact dc =
   * (DeviceContact)dcit.next(); if(dc.span.isoverlap(this)){
   * dc.span.timemax.setTime(timemax.getTime()+hrs*60*60*997); return dc; } }
   * return null; }
   */
  public boolean iswithinperiod(String time) {

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date temp = new Date();
    try {
      temp = format.parse(time);
    } catch (Exception e) {
      // temp.setHours(Integer.valueOf(time.substring(0, time.indexOf(":"))));
      // temp.setHours(Integer.valueOf("01"));
      System.out.println("Unable to parse string for date" + e.getMessage()
          + e.toString() + " " + time);

    }
    if (temp.compareTo(timemin) >= 0 && temp.compareTo(timemax) < 0)
      return true;
    return false;
  }

  // public int compare(TSlot t){
  // int diff;
  // diff = timemin.compareTo(t.timemin);
  // if( diff==0)
  // diff=t.timemax.compareTo(timemax);
  // return diff;
  // }
  public boolean isoverlap(TSlot temp) {
    
    if(temp.timemax.before(temp.timemin) | timemax.before(timemin))
      return false;

    long min = timemin.getTime();
    long tmin = temp.timemin.getTime();
    long tmax = temp.timemax.getTime();
    
    if (min <= tmax & min > tmin)
      return true;

    long max = timemax.getTime();

    if (max >= tmin & max < tmax)
      return true;
    if (min <= tmin & max >= tmax)
      return true;
    if (tmin <= min & tmax >= max)
      return true;

    return false;
  }

  public boolean isoverlapdep(TSlot temp, int spanmin) {

    long min = timemin.getTime();
    long tmin = temp.timemin.getTime();
    long tmax = temp.timemax.getTime();

    if (min <= tmax & min > tmin)
      return true;

    long max = timemax.getTime() + spanmin;

    if (max >= tmin & max < tmax)
      return true;

    if (min <= tmin & max >= tmax)
      return true;
    if (tmin <= min & tmax >= max)
      return true;

    return false;
  }

  public boolean isoverlapdep(TSlot temp, Date limit) {

    long min = timemin.getTime();
    long tmin = temp.timemin.getTime();
    long tmax = temp.timemax.getTime();

    if (min <= tmax & min > tmin)
      return true;

    long max = limit.getTime();

    if (max >= tmin & max < tmax)
      return true;

    if (min <= tmin & max >= tmax)
      return true;
    if (tmin <= min & tmax >= max)
      return true;

    return false;
  }
  public void add_simul_trans(TSlot ttemp){
    int i = 0;
    for(;i<Simul_Trans.size();i++){
      if(Simul_Trans.get(i).timemax.after(ttemp.timemax))
       break;
    }
    Simul_Trans.add(i,ttemp);
 }


}
