
package de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Active_PeerGenerator;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Apoint;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Device;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Event;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.MeetingStats;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Msg;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.TSlot;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.VolumeStats;

public class FirstContact {

  public Hashtable<String, Device> Peerstable;

  LinkedList<String> tabdevList;

  Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gMsgTable;

  Event eventQ;

  int discovery_t;

  Active_PeerGenerator apg;

  boolean resume;

  boolean queuing;

  double bw;

  int ap_count;

  VolumeStats vs;
  public FirstContact(Hashtable<String, Device> p, LinkedList<String> td,
      Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gm, Hashtable<String, Apoint> gapTab) {

    /*Peerstable = new Hashtable<String, Device>();
    tabdevList = new LinkedList<String>();
    gMsgTable = new Hashtable<BigDecimal, Msg>();*/
    Peerstable = p;
    tabdevList = td;
    gMsgTable = gm;
    vs = new VolumeStats();

    apg = new Active_PeerGenerator(p, td, gm);
    apg.set_aptable(gapTab);

  }

  public void setParameters(int apc, double bw, boolean res, boolean que) {

    resume = res;
    queuing = que;
    ap_count = apc;
    this.bw = bw;
    discovery_t = 0;
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

  public void FC_initial() {

    Create_InitialEvents();
    int i = 0;
    while (eventQ.size() > 0) {
      // if(i++%5==0)
      System.out.println(eventQ.size() + " "
          + eventQ.getFirst().m_interval.life_time.timemin + " "
          + i++);
      FC_rout(eventQ.removeFirst());
    }
  }

  public void FC_rout(Msg mtemp) {

    Device dtemp = Peerstable.get(tabdevList.get(mtemp.hoplist.getLast()));
    TSlot ttemp;
   /* if (queuing)
      dtemp.sortmsglist();*/
    // while(true)
    {

      int i = dtemp.find_slot(mtemp.m_interval.life_time.timemin);
      if (i < 0 | i >= dtemp.sorted_slots.size()) {
        mtemp.m_interval.life_time.timemin
            .setTime(mtemp.m_interval.life_time.timemax.getTime());
        return;
      }
      while (i > 0) {
        TSlot itime = dtemp.sorted_slots.remove();
        dtemp.Aptable.get(itime.apname).contacttimeList.remove(itime);
        i--;
      }
      ttemp = dtemp.sorted_slots.get(i);
      if (mtemp.msg_status(ttemp) == -1)
        return;

      TSlot itime = new TSlot();
      if (ttemp.timemin.after(mtemp.m_interval.life_time.timemin)) {
        itime.timemin.setTime(ttemp.timemin.getTime());
        mtemp.m_interval.life_time.timemin.setTime(itime.timemin.getTime());
      } else
        itime.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime());
      if (ttemp.timemax.after(mtemp.m_interval.life_time.timemax))
        itime.timemax.setTime(mtemp.m_interval.life_time.timemax.getTime());
      else
        itime.timemax.setTime(ttemp.timemax.getTime());

      itime.apname = ttemp.apname;

      Date limitT = new Date();
      int[] sorted_list = apg.GetPoten_FCPeers(itime, mtemp, limitT, ap_count);

      for (int pcount1 = 0; pcount1 < sorted_list.length; pcount1++) {
        Device dtemp2 = Peerstable.get(tabdevList.get(sorted_list[pcount1]));
        int scount2 = 0;
        TSlot itime2 = new TSlot(mtemp.m_interval.life_time.timemin.getTime(),
            itime.timemax.getTime(), "");
        // while(!dtemp2.active_slots[scount2++]);
        Apoint aptemp = dtemp2.Aptable.get(itime.apname);
        while (!aptemp.activeslots[scount2++])
          ;

        TSlot ttemp2 = (TSlot) aptemp.contacttimeList.get(scount2 - 1);
        aptemp.activeslots[scount2 - 1] = false;
        // dtemp2.active_slots[scount2-1]=false;

        if (ttemp2.timemin.after(mtemp.m_interval.life_time.timemin)) {
          itime2.timemin.setTime(ttemp2.timemin.getTime());
          mtemp.m_interval.life_time.timemin.setTime(itime2.timemin.getTime());
        }
        if (ttemp2.timemax.before(itime2.timemax))
          itime2.timemax.setTime(ttemp2.timemax.getTime());

        if (itime2.timemin.after(limitT))
          if(!eventQ.isFirstMsg(mtemp)){
            eventQ.add_event(mtemp);
            return;
          }

        double duration = ((double) itime2.timemax.getTime() - itime2.timemin
            .getTime()) / 1000;
        if (duration <= discovery_t) {
          continue;
        }
        if (queuing) {
          Msg mtemp2 = apg.getnexthopmsg(dtemp, mtemp, itime2,
              dtemp2);
          if (mtemp != mtemp2) {
            eventQ.remove(mtemp2);
            eventQ.add_event(mtemp);
            if (itime2.timemin.after(mtemp.m_interval.life_time.timemin))
              mtemp2.m_interval.life_time.timemin.setTime(itime2.timemin
                  .getTime());
            mtemp = mtemp2;
          }
        }
        apg.recordUpdate( dtemp, dtemp2, mtemp, bw * duration,vs);
        adjust_timeslots(mtemp, dtemp, dtemp2, ttemp, ttemp2, itime2, bw);
        if (!dtemp.meetStat.containsKey(sorted_list[pcount1])) {             
          dtemp.meetStat.put(new Integer(sorted_list[pcount1]), new MeetingStats(sorted_list[pcount1],new Integer(mtemp.custodian)));
          dtemp2.meetStat.put(mtemp.custodian, new MeetingStats(new Integer(mtemp.custodian),new Integer(sorted_list[pcount1])));             
        }
        if (!attempt_transfer(mtemp, dtemp, dtemp2, bw * duration))
          eventQ.add_event(mtemp);
        return;
      }
      if (dtemp.sorted_slots.size() > i + 1)
        mtemp.m_interval.life_time.timemin.setTime(dtemp.sorted_slots
            .get(i + 1).timemin.getTime() + 5);
      else
        mtemp.m_interval.life_time.timemin
            .setTime(ttemp.timemax.getTime() + 500);

      eventQ.add_event(mtemp);
    }
  }

  public void adjust_timeslots(Msg mtemp, Device dtemp, Device dtemp2,
      TSlot ttemp, TSlot ttemp2, TSlot itime, double bw) {

    double validSize;
    // check if partial delivery has already happened.
    if (resume & mtemp.Ptrans.containsKey(dtemp2.mac))
      validSize = mtemp.size - mtemp.Ptrans.get(dtemp2.mac);
    else
      validSize = mtemp.size;

    if (itime.timemax.getTime() < itime.timemin.getTime() + discovery_t * 1000
        + (validSize / bw) * 1000)
      mtemp.m_interval.life_time.timemin.setTime(itime.timemax.getTime()
          + discovery_t * 1000);
    else
      mtemp.m_interval.life_time.timemin.setTime(itime.timemin.getTime()
          + discovery_t * 1000 + (long) ((validSize * 1000) /bw) );

    if (ttemp.timemax.getTime() > mtemp.m_interval.life_time.timemin.getTime()
        + discovery_t * 1000)
      ttemp.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime());
    else
      dtemp.sorted_slots.remove(ttemp);
    if (ttemp2.timemax.getTime() > mtemp.m_interval.life_time.timemin.getTime()
        + discovery_t * 1000)
      ttemp2.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime());
    else
      dtemp2.sorted_slots.remove(ttemp2);
  }

  public boolean attempt_transfer(Msg mtemp, Device dtemp, Device dtemp2,
      double trans_bytes) {
    mtemp.attemptT++;

    if (resume) {
      if (mtemp.Ptrans.containsKey(dtemp2.mac)) {
        if (trans_bytes >= mtemp.size - mtemp.Ptrans.get(dtemp2.mac)) {
          mtemp.Ptrans.remove(dtemp2.mac);
          return transfer(mtemp, dtemp, dtemp2);
        } else
          mtemp.Ptrans.put(dtemp2.mac, new Double(mtemp.Ptrans.get(dtemp2.mac)
              + trans_bytes));
      } else {
        if (trans_bytes >= mtemp.size)
          return transfer(mtemp, dtemp, dtemp2);
        else
          mtemp.Ptrans.put(dtemp2.mac, new Double(trans_bytes));
      }
    } else if (trans_bytes >= mtemp.size)
      return transfer(mtemp, dtemp, dtemp2);
    return false;
  }

  public boolean transfer(Msg mtemp, Device dtemp, Device dtemp2) {

    dtemp.aliveMsglist.remove(mtemp);
    if(gMsgTable.get(mtemp.ID).get(mtemp.custodian).remove(mtemp.frag_id)==null){
      System.err.println("Message not in right custody");
      System.exit(0);
    }
    mtemp.hoplist.add(tabdevList.indexOf(dtemp2.mac));

    if (dtemp2.mac.equals(tabdevList.get(mtemp.dest))) {
      mtemp.del_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
      return true;
    }
    dtemp2.aliveMsglist.add(mtemp);
    eventQ.add_event(mtemp);
    return true;
  }


}
