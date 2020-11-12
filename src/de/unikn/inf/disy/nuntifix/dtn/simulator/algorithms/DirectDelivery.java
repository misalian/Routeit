
package de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms;

import java.io.DataOutputStream;
import java.io.FileOutputStream;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Active_PeerGenerator;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Device;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Event;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.MeetingStats;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Msg;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.TSlot;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.VolumeStats;


public class DirectDelivery {

  public Hashtable<String, Device> Peerstable;

  LinkedList<String> tabdevList;

  Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>>  gMsgTable;

  Event eventQ;

  int discovery_t;

  Active_PeerGenerator apg;

  boolean resume;

  boolean queuing;

  double bw;

  int ap_count;

  int simul_users;
  
  public VolumeStats vs;

  int p;
  public DirectDelivery(Hashtable<String, Device> p, LinkedList<String> td,
      Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>>  gm) {

   /* Peerstable = new Hashtable<String, Device>();
    tabdevList = new LinkedList<String>();
    gMsgTable = new Hashtable<Integer, Hashtable<Integer, Hashtable<Integer,Msg>>> ();*/
    Peerstable = p;
    tabdevList = td;
    gMsgTable = gm;
    vs = new VolumeStats();
    apg = new Active_PeerGenerator(p, td, gm);
  }

  public void setParameters(int apc, int susers, double bw, boolean res, boolean que) {

    simul_users = susers;
    resume = res;
    queuing = que;
    ap_count = apc;
    this.bw = bw;
    discovery_t = 1;
  }

  public void Create_InitialEvents() {

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

  public void directdel_initial( Date init_T,String trace, String bw) {

    Create_InitialEvents();
    //int i = 0;
    p=0;
    try {
      DataOutputStream out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
          + bw + "DirectVolume.txt"));
      DataOutputStream out2 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
          + bw + "DirectbwVolume.txt"));
    while (eventQ.size() > 0) {
      vs.vchange=false;
      vs.bwchange = false;
      Msg mtemp = eventQ.removeFirst();
      if (p++ % 500 == 0)
        System.out.println(eventQ.size() + " "
            + mtemp.m_interval.life_time.timemin);
    
      if(!mtemp.born)
      {
         
        Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.add(mtemp);
        Peerstable.get(tabdevList.get(mtemp.src)).hbytes+=mtemp.size;
        mtemp.born=true;
        vs.volume+=mtemp.size;
        mtemp.nhoplist.add(mtemp.dest);
        vs.msgs.put(mtemp.ID.toString(), mtemp.ID);
        vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
        out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");       
      }
     
      Direct_rout(mtemp);
      if (vs.vchange)         
        out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");
      if (vs.bwchange)
        out2.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+Long.toString(vs.consumed_bw) + "\n");
     
    }
    out.close();
    } catch (Exception e) {
      System.err.println(e.getMessage()+" "+e.toString());
      e.printStackTrace();
    }
  }

  public void Direct_rout(Msg mtemp) {

    Device dtemp = Peerstable.get(tabdevList.get(mtemp.hoplist.getLast()));
    TSlot ttemp;
    
    
   
    int i = dtemp.find_slot(mtemp.m_interval.life_time.timemin);
    if (i < 0 | i >= dtemp.sorted_slots.size()) {
      vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
      mtemp.m_interval.life_time.timemin
          .setTime(mtemp.m_interval.life_time.timemax.getTime());
      vs.volume -= mtemp.size;       
     vs. vchange = true;
     if(vs.msgs.remove(mtemp.ID.toString())==null)
     {
       System.out.println(mtemp.ID.toString()+" "+mtemp.custodian+" "+p);
       System.exit(-1);
     }
      return;
    }
    while (i > 5) {
      TSlot itime = dtemp.sorted_slots.remove();
      dtemp.Aptable.get(itime.apname).contacttimeList.remove(itime);
      i--;
    }

    ttemp = dtemp.sorted_slots.get(i);
    if (mtemp.msg_status(ttemp) == -1) {
      vs.volume -= mtemp.size;
      vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
      mtemp.m_interval.life_time.timemin
      .setTime(mtemp.m_interval.life_time.timemax.getTime());
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
        if (mtemp.dest != sorted_list[pcount1])
          continue;
       
        Device dtemp2 = Peerstable.get(tabdevList.get(sorted_list[pcount1]));
        TSlot itime2 = new TSlot(itime.timemin.getTime(), itime.timemax
            .getTime(), "");

        int scount2 = 0;
        while (!dtemp2.active_slots[scount2++]);

        TSlot ttemp2 = (TSlot) dtemp2.sorted_slots.get(scount2 - 1);
        // System.out.println(" "+dtemp2.mac+" "+ttemp2.timemin.toString()+ "
        // "+ttemp2.timemax.toString());
        dtemp2.active_slots[scount2 - 1] = false;

        if (ttemp2.timemin.after(itime.timemin)) {
          itime2.timemin.setTime(ttemp2.timemin.getTime());
          mtemp.m_interval.life_time.timemin.setTime(itime2.timemin.getTime());
        }
        if (ttemp2.timemax.before(itime.timemax))
          itime2.timemax.setTime(ttemp2.timemax.getTime());

        if(!eventQ.isFirstMsg(mtemp)){
          eventQ.add_event(mtemp);
          return;
        }
        vs.tstamp.setTime( itime2.timemin.getTime());
        double duration = ((double) itime2.timemax.getTime() - itime2.timemin
            .getTime()) / 1000;
        if (duration < discovery_t)
          continue;
        
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
        ttemp.add_simul_trans(ttemp2);
        ttemp2.add_simul_trans(ttemp);
        apg.recordUpdate( dtemp, dtemp2, mtemp, bw * duration,vs);
        adjust_timeslots(mtemp, dtemp, dtemp2, ttemp, ttemp2, itime2, bw);
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
          + discovery_t * 1000 +(long) ((validSize * 1000) /bw) );
  adjustSimultimeslot(dtemp, ttemp, ttemp2,mtemp);
  adjustSimultimeslot(dtemp2, ttemp2, ttemp, mtemp);

    
/*    if (ttemp.timemax.getTime() > mtemp.m_interval.life_time.timemin.getTime()
        + discovery_t * 1000)
      ttemp.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime());
    else
      dtemp.sorted_slots.remove(ttemp);

    if (ttemp2.timemax.getTime() > mtemp.m_interval.life_time.timemin.getTime()
        + discovery_t * 1000)
      ttemp2.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime());
    else
      dtemp2.sorted_slots.remove(ttemp2);
*/
  }
  public void adjustSimultimeslot(Device dtemp, TSlot ttemp, TSlot ttemp2, Msg mtemp){
    if(ttemp.Simul_Trans.size()==simul_users)
    {
      if(ttemp.timemax.getTime()>ttemp.Simul_Trans.getFirst().timemax.getTime() ||
          ttemp.timemax.getTime() > mtemp.m_interval.life_time.timemin.getTime()){
        ttemp.timemin.setTime(Math.min(ttemp.Simul_Trans.getFirst().timemax.getTime(),
            mtemp.m_interval.life_time.timemin.getTime()));
        if(ttemp.Simul_Trans.getFirst().timemax.getTime()>mtemp.m_interval.life_time.timemin.getTime())
          ttemp.Simul_Trans.remove(ttemp2);
        else  if(ttemp.Simul_Trans.getFirst().timemax.getTime()==mtemp.m_interval.life_time.timemin.getTime()){
          ttemp.Simul_Trans.removeFirst();
          ttemp.Simul_Trans.remove(ttemp2);
        }
        else
          ttemp.Simul_Trans.removeFirst();
      }
      else 
        dtemp.sorted_slots.remove(ttemp);
    }
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
    
    apg.adjustCustody(mtemp,null, dtemp, dtemp2);/*
    mtemp.hoplist.add(tabdevList.indexOf(dtemp2.mac));
    mtemp.custodian = new Integer(mtemp.hoplist.getLast());
    
    gMsgTable.get(mtemp.ID).put(mtemp.custodian,new Hashtable<String, Msg>());
    gMsgTable.get(mtemp.ID).get(mtemp.custodian).put(mtemp.frag_id, gMsgTable.get(mtemp.ID).get(mtemp.custodian).remove(mtemp.frag_id));
    gMsgTable.get(mtemp.ID).remove(mtemp.custodian);*/
    
    mtemp.del_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
    vs.vchange=true;
    vs.volume-=mtemp.size;

    return true;
  }

}
