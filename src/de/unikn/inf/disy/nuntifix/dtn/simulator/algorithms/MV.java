
package de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Active_PeerGenerator;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Device;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Event;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.History;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Msg;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.TSlot;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.VolumeStats;

public class MV {

  public Hashtable<String, Device> Peerstable;

  LinkedList<String> tabdevList;

  LinkedList<String> tabapList;

  Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gMsgTable;

  Event eventQ;

  int discovery_t;

  Active_PeerGenerator apg;

  Date starttime;

  History h;

  boolean resume;

  boolean queuing;

  int ap_count;

  double bw;
  
  public VolumeStats vs;

  public MV(Hashtable<String, Device> p, LinkedList<String> td,
      LinkedList<String> ta,Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gm) {

    /*Peerstable = new Hashtable<String, Device>();
    tabdevList = new LinkedList<String>();
    gMsgTable = new Hashtable<BigDecimal, Msg>();*/
    Peerstable = p;
    tabdevList = td;
    tabapList = ta;
    gMsgTable = gm;

    vs = new VolumeStats();
    apg = new Active_PeerGenerator(p, td, gm);
  }

  public void setParameters(int apc, double bw, boolean res, boolean que) {

    resume = res;
    queuing = que;
    discovery_t = 1;
    ap_count = apc;
    this.bw = bw;

  }

  public void Create_InitialEvents(Date init_T) {

    starttime = new Date();
    starttime.setTime(init_T.getTime());
    eventQ = new Event();
    Enumeration<Integer> mh_enum = gMsgTable.keys();
    while (mh_enum.hasMoreElements()) {
      Integer mid =   mh_enum.nextElement();
      Enumeration <Integer>c_enum =gMsgTable.get(mid).keys();
      while(c_enum.hasMoreElements())
        eventQ.add_event(gMsgTable.get(mid).get(c_enum.nextElement()).get(new Integer(0)));       
    }
    double[] keys = new double[gMsgTable.size()];
    
    for (int i = 0; i < Peerstable.size() - ap_count; i++)
      Peerstable.get(tabdevList.get(i)).probab = new double[tabdevList.size()
          - ap_count];
    // optimization. trying to calc history for those messages who were initialy
    // there in the table, and history calc starts from the point of previous
    // messege creation time
    
    for (int i = 0; i < eventQ.size(); i++) {
      Msg mtemp = eventQ.get(i);
      if (mtemp.frag_id.equals(0)) {
        new_msg(mtemp,
            eventQ.get(i-1).m_interval.life_time.timemin);
        System.out.println(eventQ.size());
        // break;
      }
      // }
    }
  }

  public void MV_Initial(Date init_T) {
    //VOLUME STATUS NOT ADDED
    h = new History(Peerstable, tabdevList, tabapList, init_T);
    Create_InitialEvents(init_T);
    int i = 0;
    while (eventQ.size() > 0) {
      Msg mtemp = eventQ.removeFirst();
      if (i++ % 500 == 0)
        System.out.println(eventQ.size() + " "
            + mtemp.m_interval.life_time.timemin);
      Mv_rout(mtemp);
    }
    System.out.println("done");
  }

  public void new_msg(Msg mtemp, Date init_T) {
    Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.add(mtemp);
    h.MV_weights(mtemp.m_interval.life_time.timemin, init_T, ap_count);
    Device dtemp = Peerstable.get(tabdevList.get(mtemp.src)), dtemp2 = Peerstable
        .get(tabdevList.get(mtemp.dest));
    if (dtemp2.homeap < 0) {
      mtemp.nhoplist.add(mtemp.dest);
      return;
    }

    double[] probab = new double[tabdevList.size() - ap_count];
    for (int i = 0; i < tabdevList.size() - ap_count; i++) {

      probab[i] = dtemp.probab[i]
          * Peerstable.get(tabdevList.get(i)).approbab[dtemp2.homeap]
          / ((double) (mtemp.m_interval.life_time.timemin.getTime() - starttime
              .getTime())) / (1000 * 60 * 60);
    }

    double max = 0;
    int max_index = 0;
    for (int i = 0; i < tabdevList.size() - ap_count; i++) {
      if (max < probab[i]) {
        max = probab[i];
        max_index = i;
      }
    }
    // as there can be only one intermediatry node
    if (max_index != mtemp.dest & max_index != mtemp.src) {
      Msg mtemp2 = new Msg(mtemp);
      gMsgTable.get(mtemp2.ID).put(mtemp2.custodian,new Hashtable<Integer, Msg>());
      gMsgTable.get(mtemp2.ID).get(mtemp2.custodian).put(mtemp2.frag_id, mtemp2);   
      gMsgTable.put(nid, new Msg(mtemp, nid));
      Peerstable.get(tabdevList.get(mtemp.src)).Msglist.add(nid);
      gMsgTable.get(nid).nhoplist.add(max_index);
      gMsgTable.get(nid).nhoplist.add(mtemp.dest);
      eventQ.add_event(nid);
    }
    mtemp.nhoplist.add(mtemp.dest);
    // MV_getPath(mtemp,init_T,ap_count);
  }

  public void Mv_rout(Msg mtemp) {

    Device dtemp = Peerstable.get(tabdevList.get(mtemp.hoplist.getLast()));
    TSlot ttemp;
    int i = dtemp.find_slot(mtemp.m_interval.life_time.timemin);
    if (i < 0 | i >= dtemp.sorted_slots.size()) {
      mtemp.m_interval.life_time.timemin
          .setTime(mtemp.m_interval.life_time.timemax.getTime());
      return;
    }
    while (i > 0) {
      dtemp.sorted_slots.remove();
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

      if (eventQ.size() > 0)
        if (itime.timemin
            .after(gMsgTable.get(eventQ.getFirst()).m_interval.life_time.timemin)) {
          eventQ.add_event(mtemp.ID);
          return;
        }

      double duration = ((double) itime2.timemax.getTime() - itime2.timemin
          .getTime()) / 1000;
      if (duration < discovery_t)
        continue;

      adjust_timeslots(mtemp, dtemp, dtemp2, ttemp, ttemp2, itime2, bw);

      if (!attempt_transfer(mtemp, dtemp, dtemp2, bw * duration))
        eventQ.add_event(mtemp.ID);
      return;
    }
    if (dtemp.sorted_slots.size() > i + 1)
      mtemp.m_interval.life_time.timemin
          .setTime(dtemp.sorted_slots.get(i + 1).timemin.getTime() + 5);
    else
      mtemp.m_interval.life_time.timemin.setTime(ttemp.timemax.getTime() + 500);
    eventQ.add_event(mtemp.ID);

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
    if (mtemp.nh > 1)
      mtemp.nh--;
    return false;
  }

  public boolean transfer(Msg mtemp, Device dtemp, Device dtemp2) {

    dtemp.Msglist.remove(mtemp.ID);
    mtemp.hoplist.add(tabdevList.indexOf(dtemp2.mac));
    mtemp.nhoplist.remove();

    if (dtemp2.mac.equals(tabdevList.get(mtemp.dest))) {
      mtemp.del_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
      return true;
    }
    dtemp2.Msglist.add(mtemp.ID);
    eventQ.add_event(mtemp.ID);

    return true;

  }
  public void adjustCustody(Msg mtemp,Msg mtemp2, Device dtemp,Device dtemp2){
    
    if(mtemp2==null){
      dtemp.aliveMsglist.remove(mtemp);
      if(gMsgTable.get(mtemp.ID).get(mtemp.custodian).remove(mtemp.frag_id)==null){
        System.err.println("Message not in right custody");
        System.exit(0);
      }    
      if(gMsgTable.get(mtemp.ID).get(mtemp.custodian).size()==0)
        gMsgTable.get(mtemp.ID).remove(mtemp.custodian);
      mtemp.custodian = new Integer(tabdevList.indexOf(dtemp2.mac));
      mtemp.hoplist.add(tabdevList.indexOf(dtemp2.mac));
      
      if(gMsgTable.get(mtemp.ID).containsKey(mtemp.custodian)){
        if(gMsgTable.get(mtemp.ID).get(mtemp.custodian).put(mtemp.frag_id, mtemp)!=null){
          System.err.println("Message already in  custody "+mtemp.ID.toString()+" "+mtemp.m_interval.life_time.timemin);
          System.exit(0);      
        }
      }
      else{
        gMsgTable.get(mtemp.ID).put(mtemp.custodian,new Hashtable<Integer, Msg>());
        gMsgTable.get(mtemp.ID).get(mtemp.custodian).put(mtemp.frag_id, mtemp);         
      }
    }
    else{
      
      mtemp2.custodian = new Integer(tabdevList.indexOf(dtemp2.mac));
      mtemp2.hoplist.add(mtemp2.custodian);
      if(gMsgTable.get(mtemp2.ID).containsKey(mtemp2.custodian)){
        if(gMsgTable.get(mtemp2.ID).get(mtemp2.custodian).put(mtemp2.frag_id, mtemp2)!=null){
          System.err.println("Message already in  custody "+mtemp2.ID.toString()+" "+mtemp2.custodian+" "+mtemp2.frag_id+" "+mtemp2.m_interval.life_time.timemin);
          System.exit(0);      
        }
      }
      else{
        gMsgTable.get(mtemp2.ID).put(mtemp2.custodian,new Hashtable<Integer, Msg>());
        gMsgTable.get(mtemp2.ID).get(mtemp2.custodian).put(mtemp2.frag_id, mtemp2);         
      }
    }
  }
  /*public BigDecimal get_high_copyid(BigDecimal id, int max) {

    double temp = 10;
    while (((double) max) / temp > 1)
      temp *= 10;
    int i;

    String fl = Double.toString(id.doubleValue());

    for (i = Integer.parseInt(fl.substring(fl.indexOf(".") + 1)) + 1; i <= max; i++) {
      if (!gMsgTable.containsKey(new BigDecimal(id.intValue() + (i / temp))
          .round(MathContext.DECIMAL32).stripTrailingZeros()))
        break;
    }
    fl = new BigDecimal(id.intValue() + (i / temp))
        .round(MathContext.DECIMAL32).stripTrailingZeros().toString();
    if (fl.substring(fl.indexOf(".") + 1).length() > 3)
      return id;
    return new BigDecimal(id.intValue() + (i / temp)).round(
        MathContext.DECIMAL32).stripTrailingZeros();
  }
*/
}
