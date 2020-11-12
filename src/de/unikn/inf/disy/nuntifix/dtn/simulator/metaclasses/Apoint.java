
package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;

import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;

public class Apoint {

  public String Apointname;

  public LinkedList<TSlot> contacttimeList;
  public LinkedList<TSlot> congestiontimeList;


  // public LinkedList <Apoint>apointlist;
  public long timespan;

  public float tdevcontime;

  public int grpcount;

  public int meetincount;

  public TSlot cslot;

  public int transfreq;

  public int gtranfreq;

  public long transtime;

  public boolean[] activeslots;

  public LinkedList<Device> devlist;
  public int channelFail;

  public Apoint(Apoint ap) {

    Apointname = new String(ap.Apointname);
    contacttimeList = new LinkedList<TSlot>();
    congestiontimeList= new LinkedList<TSlot>();

    for (int i = 0; i < ap.contacttimeList.size(); i++)
      contacttimeList.add(new TSlot(
          ap.contacttimeList.get(i).timemin.getTime(), ap.contacttimeList
              .get(i).timemax.getTime(), ap.Apointname));
    // delaytlist = new LinkedList<Long>();
    // apointlist = new LinkedList<Apoint>();
    // connecdura = 0;
    timespan = 5 * 60 * 950;
    /*
     * transfreq=0; gtranfreq=0; transtime=0;
     */
    tdevcontime = 0;
    channelFail = 0;
    devlist = new LinkedList<Device>();

  }

  public Apoint(String apname) {

    Apointname = apname;
    contacttimeList = new LinkedList<TSlot>();
    congestiontimeList= new LinkedList<TSlot>();
    // delaytlist = new LinkedList<Long>();
    // apointlist = new LinkedList<Apoint>();
    // connecdura = 0;
    timespan = 5 * 60 * 950;
    /*
     * transfreq=0; gtranfreq=0; transtime=0;
     */
    tdevcontime = 0;
    channelFail = 0;
    devlist = new LinkedList<Device>();

  }

  public int find_slot(Date dtemp, int low, int high) {

    if (high < 0)
      return -1;
    if (dtemp.after(contacttimeList.getLast().timemax))
      return -1;

    if (dtemp.before(contacttimeList.getFirst().timemin))
      return 0;

    int p = (high + low) / 2;
    TSlot ttemp = contacttimeList.get(p);

    if (ttemp.timemin.before(dtemp) & ttemp.timemax.after(dtemp))
      return p;
    if (high <= low + 1) {
      return high;
    }

    if (dtemp.before(ttemp.timemax))
      return find_slot(dtemp, low, p);
    return find_slot(dtemp, p, high);
  }

  // public Apoint getaptrans(String apstr){
  // ListIterator apit= apointlist.listIterator();
  // Apoint temp;
  // while(apit.hasNext()){
  // temp = (Apoint)apit.next();
  // if(temp.Apointname.equals(apstr))
  // return temp;
  // }
  // return null;
  //		
  // }
  public boolean adjusttslot(TSlot ttemp2) {

    TSlot ttemp1;
    long timediff;
    ListIterator contactit = contacttimeList.listIterator();
    while (contactit.hasNext()) {
      ttemp1 = (TSlot) contactit.next();
      if (ttemp1.timemax.before(ttemp2.timemax)
          & ttemp1.timemin.before(ttemp2.timemin)) {
        if (ttemp1.timemax.before(ttemp2.timemin)) {
          timediff = ttemp2.timemin.getTime() - ttemp1.timemax.getTime();
          if (timediff <= timespan) {
            ttemp1.timemax.setTime(ttemp2.timemax.getTime());
            return true;
          }
        } else {
          ttemp1.timemax.setTime(ttemp2.timemax.getTime());
          return true;
        }
      }

      if (ttemp1.timemin.after(ttemp2.timemin)
          & ttemp1.timemax.after(ttemp2.timemax)) {
        if (ttemp1.timemin.after(ttemp2.timemax)) {
          timediff = ttemp1.timemin.getTime() - ttemp2.timemax.getTime();
          if (timediff <= timespan) {
            ttemp1.timemin.setTime(ttemp2.timemin.getTime());
            return true;
          }
        } else {
          ttemp1.timemin.setTime(ttemp2.timemin.getTime());
          return true;

        }
      }
    }

    return false;
  }

  public int find_slot(Date dtemp) {

    for (int i = 0; i < contacttimeList.size(); i++) {
      if (contacttimeList.get(i).timemax.compareTo(dtemp) >= 0)
        return i;
    }
    return contacttimeList.size() - 1;
  }

  public void rearrnagetime() {

    TSlot ttemp1, ttemp2;
    ListIterator contactit = contacttimeList.listIterator();
    ListIterator contactit2;
    while (contactit.hasNext()) {
      ttemp1 = (TSlot) contactit.next();
      contactit2 = contacttimeList
          .listIterator(contacttimeList.indexOf(ttemp1) + 1);
      while (contactit2.hasNext()) {
        ttemp2 = (TSlot) contactit.next();
        if (ttemp1.timemax.after(ttemp2.timemin)) {
          ttemp1.timemax = ttemp2.timemax;
          contacttimeList.remove(ttemp2);
          contactit2 = contacttimeList.listIterator(contacttimeList
              .indexOf(ttemp1) + 1);

        }

      }
    }
  }
  /*
   * public int checkarrndep(Date dtemp,Date dtemp2, LinkedList meetinglist,int
   * low,int high){ int p = (low+high)/2; ListIterator cit2 =
   * meetinglist.listIterator(p); Meeting mtemp =(Meeting) cit2.next(); //
   * System.out.println(ttemp2.timemin.toString() + "
   * "+ttemp2.timemax.toString());
   * if(Math.abs(mtemp.starttime.getTime()-dtemp.getTime())<1000*60*margin){
   * if(Math.abs(mtemp.endtime.getTime()-dtemp2.getTime())<1000*60*margin){
   * //mtemp.endtime.setTime((mtemp.endtime.getTime()+dtemp2.getTime())/2);
   * //mtemp.starttime.setTime((mtemp.starttime.getTime()+dtemp.getTime())/2);
   * return p; } } if(high-low<=1) return -1; if(dtemp.before(mtemp.starttime))
   * return checkarrndep(dtemp,dtemp2,meetinglist,low,p); return
   * checkarrndep(dtemp,dtemp,meetinglist,p,high); }
   */
}