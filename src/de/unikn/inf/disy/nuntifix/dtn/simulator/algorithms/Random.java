
package de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Active_PeerGenerator;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Device;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Event;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Msg;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.TSlot;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.VolumeStats;

public class Random {

  public Hashtable<String, Device> Peerstable;

  LinkedList<String> tabdevList;

  Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>>gMsgTable;

  Event eventQ;

  int discovery_t;

  VolumeStats vs;
  Active_PeerGenerator apg;

  public Random(Hashtable<String, Device> p, LinkedList<String> td,
      Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gm) {   
    Peerstable = p;
    tabdevList = td;
    gMsgTable = gm;
    discovery_t =0;
    vs = new VolumeStats();
    apg = new Active_PeerGenerator(p, td, gm);
  }

  public void Create_InitialEvents() {

    eventQ = new Event();    
    Enumeration<Integer> mh_enum = gMsgTable.keys();
    while (mh_enum.hasMoreElements()) {
      Integer mid =   mh_enum.nextElement();
      Enumeration <Integer>c_enum =gMsgTable.get(mid).keys();
      while(c_enum.hasMoreElements())
        eventQ.add_event(gMsgTable.get(mid).get(c_enum.nextElement()).get(new Integer(0)));       
    }    
  }

  public void Random_Initial(double bw, int ap_count) {

    //VOLUME STATUS NOT ADDED
    Create_InitialEvents();
    while (eventQ.size() > 0) {
      Random_rout(eventQ.removeFirst(), bw, ap_count);
    }
  }

  public void Random_rout(Msg mtemp, double bw, int ap_count) {

    Device dtemp = Peerstable.get(tabdevList.get(mtemp.hoplist.getLast()));
    TSlot ttemp;
    
      
      int i = dtemp.find_slot(mtemp.m_interval.life_time.timemin);
      if (i < 0 | i >= dtemp.sorted_slots.size()) {
        vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
        mtemp.m_interval.life_time.timemin
            .setTime(mtemp.m_interval.life_time.timemax.getTime());
        vs.volume -= mtemp.size;
        
        vs.vchange = true;
        return;
      }
      while (i > 0) {
        TSlot itime = dtemp.sorted_slots.remove();
        dtemp.Aptable.get(itime.apname).contacttimeList.remove(itime);
        i--;
      }

      ttemp = dtemp.sorted_slots.get(i);
      if (mtemp.msg_status(ttemp) == -1) {
        vs.volume -= mtemp.size;
  
        vs.tstamp=mtemp.m_interval.life_time.timemin;
        vs.vchange = true;
        return;
      }

      if (mtemp.m_interval.life_time.timemin.before(ttemp.timemin))
        mtemp.m_interval.life_time.timemin.setTime(ttemp.timemin.getTime());
      TSlot itime = new TSlot();
      if (ttemp.timemin.after(mtemp.m_interval.life_time.timemin))
        itime.timemin.setTime(ttemp.timemin.getTime());
      else
        itime.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime());
      itime.timemax.setTime(ttemp.timemax.getTime());
      

      int[] sorted_list = apg.GetactivePeers(itime, ap_count);
      for (int pcount1 = 0; pcount1 < sorted_list.length; pcount1++) {
        if (sorted_list[pcount1] == mtemp.hoplist.getLast())
          continue;
        Device dtemp2 = Peerstable.get(tabdevList.get(sorted_list[pcount1]));

        int scount2 = 0;
        while (!dtemp2.active_slots[scount2++])
          ;
        TSlot ttemp2 = (TSlot) dtemp2.sorted_slots.get(scount2 - 1);
        dtemp2.active_slots[scount2 - 1] = false;
        System.out.println(" " + dtemp2.mac + " " + ttemp2.timemin.toString()
            + " " + ttemp2.timemax.toString());
        if (!itime.isoverlap(ttemp2) | !ttemp.apname.equals(ttemp2.apname))
          continue;

        if (ttemp2.timemin.after(itime.timemin))
          itime.timemin.setTime(ttemp2.timemin.getTime());
        if (ttemp2.timemax.before(itime.timemax))
          itime.timemax.setTime(ttemp2.timemax.getTime());

        if(!eventQ.isFirstMsg(mtemp)){
          eventQ.add_event(mtemp);
          return;
        }
        double duration = ((double) itime.timemax.getTime() - itime.timemin
            .getTime()) / 1000;
        if (!dtemp2.mac.equals(tabdevList.get(mtemp.dest)))
          if (Math.random() > 0.5)
            continue;
        apg.recordUpdate( dtemp, dtemp2, mtemp, bw * duration,vs);
        adjust_timeslots(mtemp, dtemp, dtemp2, ttemp, ttemp2, itime, bw);

        if (!attempt_transfer(mtemp, dtemp, dtemp2, bw * duration))
          eventQ.add_event(mtemp);
        return;
      }
    }

   

  public void adjust_timeslots(Msg mtemp, Device dtemp, Device dtemp2,
      TSlot ttemp, TSlot ttemp2, TSlot itime, double bw) {

    if (itime.timemax.getTime() < itime.timemin.getTime() + discovery_t * 1000
        + (mtemp.size / bw) * 1000)
      mtemp.m_interval.life_time.timemin.setTime(itime.timemax.getTime()
          + discovery_t * 1000);
    else
      mtemp.m_interval.life_time.timemin.setTime(itime.timemin.getTime()
          + discovery_t * 1000 + (long) ((mtemp.validSize * 1000) /bw) );

    long max_temp;
    if (ttemp.timemax.getTime() > mtemp.m_interval.life_time.timemin.getTime()
        + discovery_t * 1000) {
      ttemp.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime());
     
    } else {
      dtemp.sorted_slots.remove(ttemp);
    }
    if (ttemp2.timemax.getTime() > mtemp.m_interval.life_time.timemin.getTime()
        + discovery_t * 1000) {
      ttemp2.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime());
     } else {
      dtemp2.sorted_slots.remove(ttemp2);
    }

  }

  public boolean attempt_transfer(Msg mtemp, Device dtemp, Device dtemp2,
      double trans_bytes) {
    mtemp.attemptT++;

    if (trans_bytes >= mtemp.size) {
      dtemp.aliveMsglist.remove(mtemp);
      mtemp.hoplist.add(tabdevList.indexOf(dtemp2.mac));
      dtemp2.aliveMsglist.add(mtemp);
      apg.adjustCustody(mtemp, null, dtemp, dtemp2);
      if (dtemp2.mac.equals(tabdevList.get(mtemp.dest))) {
        mtemp.del_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
        return true;
      }
      dtemp2.aliveMsglist.add(mtemp);
      eventQ.add_event(mtemp);
      return true;
    }
    return false;
  }


}
