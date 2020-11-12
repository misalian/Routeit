
package de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Active_PeerGenerator;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Device;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Dijkstra;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Event;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.MeetingStats;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Msg;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.TSlot;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.VolumeStats;

public class MultiCast {

  public Hashtable<String, Device> Peerstable;

  LinkedList<String> tabdevList;

  Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gMsgTable;

  Event eventQ;

  Active_PeerGenerator apg;

  Dijkstra dij;

  int discovery_t;

  boolean resume;

  boolean queuing;

  boolean DT;

  boolean MED;

  boolean perfect;

  int ap_count;

  double bw;
  
  public VolumeStats vs;

  public MultiCast(Hashtable<String, Device> p, LinkedList<String> td,
      Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>>gm) {
  
    Peerstable = p;
    tabdevList = td;
    gMsgTable = gm;
    apg = new Active_PeerGenerator(p, td, gm);
    vs = new VolumeStats();
  }

  public void setParameters(int apc, double bw, boolean M, boolean D,
      boolean P, boolean res, boolean que) {
    resume = res;
    queuing = que;
    this.bw = bw;
    ap_count = apc;
    DT = D;
    MED = M;
    perfect = P;
    discovery_t = 1;
  }

  public void Create_InitialEvents() {

    dij = new Dijkstra(Peerstable, tabdevList);
    dij.cloneall(ap_count, perfect);
    if (!perfect)
      dij.Create_Oracletrace(ap_count);
    eventQ = new Event();
    Enumeration<Integer> mh_enum = gMsgTable.keys();
    while (mh_enum.hasMoreElements()) {
      Integer mid =   mh_enum.nextElement();
      Enumeration <Integer>c_enum =gMsgTable.get(mid).keys();
      Integer cust= new Integer(0);
      while(c_enum.hasMoreElements()){
        cust =c_enum.nextElement();
        eventQ.add_event(gMsgTable.get(mid).get(cust).get("0"));       
      }      
    }    
    
  }

/*  public void new_msg(Msg mtemp) {

    Msg mtemp2;

    if (mtemp.grp_semantic > 0) {
      LinkedList<Integer> dest_grp = get_grpmem(mtemp.dest,
          mtemp.m_interval.mem_interval, mtemp.m_interval.life_time,
          mtemp.grp_semantic);
      if (dest_grp.size() < 1) {
        gMsgTable.remove(mtemp.ID);
        return;
      }

      ListIterator dgit = dest_grp.listIterator();
      mtemp.grp_semantic = 0;
      mtemp.dest = (Integer) dgit.next();
      dij.dijkstra(mtemp, MED, ap_count);
      while (dgit.hasNext()) {
        BigDecimal nid = new BigDecimal((double) gMsgTable.size());
        gMsgTable.put(nid, mtemp2 = new Msg(mtemp, nid));
        dij.dijkstra(mtemp2, MED, ap_count);
        Peerstable.get(tabdevList.get(mtemp.src)).Msglist.add(mtemp2.ID);
      }
    } else
      dij.dijkstra(mtemp, MED, ap_count);

  }*/

  public void Multi_CastingInitial(String trace, Date init_T) {
    //Debugging needed
    Create_InitialEvents();
    /*for (int i = 0; i < eventQ.size(); i++) {
      Msg mtemp = gMsgTable.get(eventQ.get(i));
      for (int j = 0; j < mtemp.nhoplist.size() - 1; j++)
        System.out.print(mtemp.nhoplist.get(j) + " ");
      System.out.println(i + " p");
    }*/
    int i = 0;
    try {
      DataOutputStream out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
          + bw + "STBR"+(perfect ? "P":"")+"Volume.txt")); 
      DataOutputStream out2 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
          + bw + "STBR"+(perfect ? "P":"")+"bwVolume.txt")); 
      Msg mtemp;
      while (eventQ.size() > 0) {
        vs.vchange=false;
        vs.bwchange=false;
        mtemp = eventQ.removeFirst();
        if (i++ % 50 == 0)
          System.out.println(i++ + " " + eventQ.size() + " "
              + mtemp.m_interval.life_time.timemin);
        if(!mtemp.born)
        {
          Peerstable.get(tabdevList.get(mtemp.src)).hbytes+=mtemp.size;  
          Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.add(mtemp);
          mtemp.born=true;
          dij.dijkstra(mtemp,  ap_count);
          vs.volume+=mtemp.size;
          vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
          out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");       
        }
        STBR(mtemp);
        if (vs.vchange)
          out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");     
        if (vs.bwchange)
          out2.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+Long.toString(vs.consumed_bw) + "\n");     
      }
    } catch (Exception e) {
      System.err.println(e.getMessage()+" "+e.toString());
      e.printStackTrace();
    }
  }

  public void STBR(Msg mtemp) {

    Device dtemp = Peerstable.get(tabdevList.get(mtemp.hoplist.getLast()));
  
    TSlot ttemp;
    int i = dtemp.find_slot(mtemp.m_interval.life_time.timemin);
    if (i < 0 | i >= dtemp.sorted_slots.size()) {
      vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
      mtemp.m_interval.life_time.timemin
          .setTime(mtemp.m_interval.life_time.timemax.getTime());
      vs.volume -= mtemp.size;       
     vs. vchange = true;
      return;
    }
    while (i > 5) {
      TSlot itime = dtemp.sorted_slots.remove();
      dtemp.Aptable.get(itime.apname).contacttimeList.remove(itime);
      i--;
    }

    ttemp = dtemp.sorted_slots.get(i);

    if (mtemp.nhoplist.getFirst() != mtemp.dest) {
      if (ttemp.timemin.after(mtemp.nhop.T)) {
        mtemp.m_interval.life_time.timemin.setTime(ttemp.timemin.getTime() + 5);
        dij.dijkstra(mtemp,  ap_count);
      }
    }

    if (mtemp.msg_status(ttemp) == -1){
      vs.volume -= mtemp.size;
      vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
      vs.vchange = true;
      return;

    }

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
    int[] sorted_list = apg.GetNexthop(itime, mtemp, ap_count);
    for (int pcount1 = 0; pcount1 < sorted_list.length; pcount1++) {

      Device dtemp2 = Peerstable.get(tabdevList.get(sorted_list[pcount1]));
      int scount2 = 0;
      while (!dtemp2.active_slots[scount2++])
        ;
      TSlot ttemp2 = (TSlot) dtemp2.sorted_slots.get(scount2 - 1);
      dtemp2.active_slots[scount2 - 1] = false;

      TSlot itime2 = new TSlot();
      if (ttemp2.timemin.after(mtemp.m_interval.life_time.timemin)) {
        itime2.timemin.setTime(ttemp2.timemin.getTime());
        mtemp.m_interval.life_time.timemin.setTime(itime2.timemin.getTime());
      } else
        itime2.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime());
      if (ttemp2.timemax.before(itime.timemax))
        itime2.timemax.setTime(ttemp2.timemax.getTime());
      else
        itime2.timemax.setTime(itime.timemax.getTime());

      if(!eventQ.isFirstMsg(mtemp)){
        eventQ.add_event(mtemp);
        return;
      }
      vs.tstamp.setTime( itime2.timemin.getTime());
      double duration = ((double) itime2.timemax.getTime() - itime2.timemin
          .getTime()) / 1000;
      

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
      if (!dtemp.meetStat.containsKey(sorted_list[pcount1])) {             
        dtemp.meetStat.put(new Integer(sorted_list[pcount1]), new MeetingStats(sorted_list[pcount1],new Integer(mtemp.custodian)));
        dtemp2.meetStat.put(mtemp.custodian, new MeetingStats(new Integer(mtemp.custodian),new Integer(sorted_list[pcount1])));             
      }
      apg.recordUpdate( dtemp, dtemp2, mtemp, bw * duration,vs);
      adjust_timeslots(mtemp, dtemp, dtemp2, ttemp, ttemp2, itime2, bw);

      if (attempt_transfer(mtemp, dtemp, dtemp2, bw * duration)) {
        if (DT)
          dij.dijkstra(mtemp,  ap_count);
      }    
      else
        eventQ.add_event(mtemp);
      return;
    }
    if (dtemp.sorted_slots.size() > i + 1)
      mtemp.m_interval.life_time.timemin
          .setTime(dtemp.sorted_slots.get(i + 1).timemin.getTime() + 5);
    else
      mtemp.m_interval.life_time.timemin.setTime(ttemp.timemax.getTime() + 500);
    eventQ.add_event(mtemp);
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
          + discovery_t * 1000 + (long) (validSize / bw) * 1000);

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
    /*if (mtemp.nh > 1)
      mtemp.nh--;*/
    return false;
  }

  public boolean transfer(Msg mtemp, Device dtemp, Device dtemp2) {

    dtemp.aliveMsglist.remove(mtemp);
    mtemp.hoplist.add(tabdevList.indexOf(dtemp2.mac));
    mtemp.nhoplist.remove();
    apg.adjustCustody(mtemp,null, dtemp, dtemp2);
    dtemp2.aliveMsglist.add(mtemp);
    
    if (dtemp2.mac.equals(tabdevList.get(mtemp.dest))) {
      mtemp.del_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
      vs.vchange=true;
      vs.volume-=mtemp.size;
      if(mtemp.size==1)
        eventQ.KillAll(mtemp);
      return true;
    }
    mtemp.nhop = mtemp.nhop.to;
    eventQ.add_event(mtemp);

    return true;
  }
}