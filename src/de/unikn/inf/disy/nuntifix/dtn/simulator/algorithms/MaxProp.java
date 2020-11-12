
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
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.History;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.MeetingStats;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Msg;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.TSlot;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.VolumeStats;

public class MaxProp {

  public Hashtable<String, Device> Peerstable;

  LinkedList<String> tabdevList;

  LinkedList<String> tabapList;

  Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>>gMsgTable;

  Event eventQ;

  int discovery_t;

  Active_PeerGenerator apg;

  Dijkstra dij;

  int b_thresh;

  // int meetin_count;
  // double bytes_transf;
  History h;

  boolean resume;

  boolean queuing;

  int ap_count;

  double bw;

  public VolumeStats vs;
  int p;
  
  public MaxProp(Hashtable<String, Device> p, LinkedList<String> td,
      Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>>gm) {

    /*Peerstable = new Hashtable<String, Device>();
    tabdevList = new LinkedList<String>();
    gMsgTable = new Hashtable<BigDecimal, Msg>();*/
    Peerstable = p;
    tabdevList = td;
    gMsgTable = gm;

    vs = new VolumeStats();
    apg = new Active_PeerGenerator(p, td, gm);
  }

  public void setParameters(int apc, double bw, boolean res, boolean que) {

    discovery_t = 1;
    resume = res;
    queuing = que;
    this.bw = bw;
    ap_count = apc;
  }

  public void Create_InitialEvents(Date init_T) {

    
    for (int j = 0; j < tabdevList.size() - ap_count; j++) {
      Peerstable.get(tabdevList.get(j)).probab = new double[tabdevList.size()
          - ap_count];
      for (int i = 0; i < tabdevList.size() - ap_count; i++) {
       /* Peerstable.get(tabdevList.get(j)).probab[i] = 1 / ((double) tabdevList
            .size()
            - ap_count - 1);*/
        Peerstable.get(tabdevList.get(j)).meetingcount = new int[tabdevList
            .size()- ap_count];
        }
    }
    eventQ = new Event();
    Enumeration<Integer> mh_enum = gMsgTable.keys();
    while (mh_enum.hasMoreElements()) {
      Integer mid =   mh_enum.nextElement();
    //  System.out.println(mid.toString());
      Enumeration <Integer>c_enum =gMsgTable.get(mid).keys();
      Integer cust= new Integer(0);
      while(c_enum.hasMoreElements()){
        cust =c_enum.nextElement();
        //System.out.println(mid.toString());
        eventQ.add_event(gMsgTable.get(mid).get(cust).get("0"));          
      }      
    }    
  }

  public void Maxprop_Initial(Date init_T, String trace,String bandwidth) {

    h = new History(Peerstable, tabdevList, null, null);
    dij = new Dijkstra(Peerstable, tabdevList);
    b_thresh = 0;
    Date histT = new Date();
    try {
      Create_InitialEvents(init_T);
      histT.setTime(eventQ.getFirst().m_interval.life_time.timemin.getTime());
      DataOutputStream out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
          + bandwidth+ "MaxPropVolume.txt"));
      DataOutputStream out2 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
          + bandwidth+ "MaxPropbwVolume.txt")); 
      //int i = 0;
      p =0;
      /*for(int i=0;i<eventQ.size();i++){
        System.out.println(eventQ.get(i).ID+" "+eventQ.get(i).m_interval.life_time.timemin);
      }*/
      h.MaxpropHistory(histT, init_T, ap_count,bw);
      Msg mtemp;
      
      while (eventQ.size() > 0) {
        vs.vchange=false; 
        vs.bwchange=false;
        mtemp = eventQ.removeFirst();
        if (p++ % 500 == 0)
          System.out.println(eventQ.size() + " " + mtemp.m_interval.life_time.timemin);
        if(!mtemp.born)
        {           
          mtemp.rTime.setTime(mtemp.m_interval.life_time.timemin.getTime()-50000);
          Peerstable.get(tabdevList.get(mtemp.src)).hbytes+=mtemp.size;
          Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.add(mtemp);
          mtemp.born=true;
          vs.volume+=mtemp.size;
          mtemp.value=Double.MAX_VALUE;
          vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
          vs.msgs.put(mtemp.ID.toString(), mtemp.ID);
          out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");       
        } 
        
      //  if(mtemp.ID.equals(5))
        //  System.out.println(mtemp.m_interval.life_time.timemin);
        Maxprop_rout(mtemp, true);
        if (vs.vchange)
          out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");
        if (vs.bwchange)
          out2.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+Long.toString(vs.consumed_bw) + "\n");
        if (eventQ.size() > 0 )
          if(eventQ.getFirst().m_interval.life_time.timemin.after(histT)){   
            histT.setTime(eventQ.getFirst().m_interval.life_time.timemin.getTime()+1000*60*10);
            h.MaxpropHistory(histT,eventQ.getFirst().m_interval.life_time.timemin, ap_count,bw); 
          
        } 
     }
    out.close();
    }catch (Exception e) {
      System.err.println(e.getMessage()+" "+e.toString());
      e.printStackTrace();
    }
  }

  public void Maxprop_rout(Msg mtemp, boolean applyDij) {

    Device dtemp = Peerstable.get(tabdevList.get(mtemp.hoplist.getLast()));
    TSlot ttemp;
   
      int i = dtemp.find_slot(mtemp.m_interval.life_time.timemin);
      if (i < 0 | i >= dtemp.sorted_slots.size()) {
        vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
        mtemp.m_interval.life_time.timemin
            .setTime(mtemp.m_interval.life_time.timemax.getTime());
        vs.volume -= mtemp.size;       
        vs.vchange = true;
        if(mtemp.ID.equals(98))
          System.out.println("Message goin out 1 "+p);
        if(vs.msgs.remove(mtemp.ID.toString())==null)
        {
          System.out.println(mtemp.ID.toString()+" 1 "+mtemp.custodian+" "+p);
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
        
        if(vs.msgs.remove(mtemp.ID.toString())==null)
        {
          System.out.println(mtemp.ID.toString()+" 2 "+mtemp.custodian+" "+p);
          System.exit(-1);
        }
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
      if (applyDij & mtemp.rTime.before(mtemp.m_interval.life_time.timemin))
        dij.RMaxprop_dijkstra(mtemp, ap_count);

      int[] sorted_list = apg.GetNexthop(itime, mtemp, ap_count);
      for (int pcount1 = 0; pcount1 < sorted_list.length; pcount1++) {
        Device dtemp2 = Peerstable.get(tabdevList.get(sorted_list[pcount1]));
        TSlot itime2 = new TSlot(itime.timemin.getTime(), itime.timemax
            .getTime(), "");

        int scount2 = 0;
        while (!dtemp2.active_slots[scount2++]);
        TSlot ttemp2 = (TSlot) dtemp2.sorted_slots.get(scount2 - 1);
        dtemp2.active_slots[scount2 - 1] = false;

        if (ttemp2.timemin.after(mtemp.m_interval.life_time.timemin)) {
          itime2.timemin.setTime(ttemp2.timemin.getTime());
          mtemp.m_interval.life_time.timemin.setTime(itime2.timemin.getTime());
        }
        if (ttemp2.timemax.before(itime2.timemax))
          itime2.timemax.setTime(ttemp2.timemax.getTime());

        if(!eventQ.isFirstMsg(mtemp)){
          eventQ.add_event(mtemp);
          return;
      }
      
        
        vs.tstamp.setTime( itime2.timemin.getTime());
        double duration = ((double) itime2.timemax.getTime() - itime2.timemin
            .getTime()) / 1000;
        Msg mtemp2;
        if (queuing)
          mtemp2 = apg
              .getnexthopmsg(dtemp, mtemp, itime2, dtemp2);
        else {
          // mtemp2=getdestmsg(dtemp,mtemp,itime2,sorted_list[pcount1],ap_count);
          dtemp.sortmsglist(b_thresh);
          double avg_bytes_trans = dtemp.dbytes / dtemp.getmeetingcount();
          //double avg_bytes_trans = (bw*duration) / dtemp.getmeetingcount();
          double bsize=dtemp.getByteSize();
          if (avg_bytes_trans < bsize / 2)
            b_thresh = (int) avg_bytes_trans;
          else if (avg_bytes_trans >= bsize / 2
              & avg_bytes_trans < bsize)
            b_thresh = (int)Math.min(avg_bytes_trans ,bsize -avg_bytes_trans);
          else
            b_thresh = 0;
          mtemp2 = getnexthopmsg(dtemp, mtemp, itime2, sorted_list[pcount1],
              ap_count, applyDij);
        }

        if (mtemp != mtemp2) {
          eventQ.remove(mtemp2);
          eventQ.add_event(mtemp);
          if (itime2.timemin.after(mtemp.m_interval.life_time.timemin))
            mtemp2.m_interval.life_time.timemin.setTime(itime2.timemin
                .getTime());
          mtemp = mtemp2;
        }       
        if (!dtemp.meetStat.containsKey(sorted_list[pcount1])) {             
          dtemp.meetStat.put(new Integer(sorted_list[pcount1]), new MeetingStats(sorted_list[pcount1],new Integer(mtemp.custodian)));
          dtemp2.meetStat.put(mtemp.custodian, new MeetingStats(new Integer(mtemp.custodian),new Integer(sorted_list[pcount1])));             
        }
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
    //mtemp.hoplist.add(tabdevList.indexOf(dtemp2.mac));
    /*if (mtemp.nhoplist.size() > 0)
      mtemp.nhoplist.remove();*/
    while(mtemp.nhoplist.remove()!=tabdevList.indexOf(dtemp2.mac));
      
    apg.adjustCustody(mtemp, null, dtemp, dtemp2);
    if (tabdevList.indexOf(dtemp2.mac) == mtemp.dest) {
      mtemp.del_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
      vs.vchange=true;
      vs.volume-=mtemp.size;      
      mtemp.delivered=true;;
      if(vs.msgs.remove(mtemp.ID.toString())==null)
      {
        System.out.println(mtemp.ID.toString()+" 3 "+mtemp.custodian+" "+p);
        System.exit(-1);
      }
      
      return true;
    }
    dtemp2.aliveMsglist.add(mtemp);
    eventQ.add_event(mtemp);

    return true;
  }

  public Msg getnexthopmsg(Device dtemp, Msg mtemp, TSlot itime, int nextHop,
      int ap_count, boolean applyDij) {

    Msg mtemp2;
    for (int i = 0; i < dtemp.aliveMsglist.size(); i++) {
      mtemp2 = dtemp.aliveMsglist.get(i);
      if(mtemp2.delivered)
        continue;
      if(mtemp2.msg_status(itime)!=1)
        continue;  
      if (mtemp2.dest == nextHop & mtemp2.m_interval.life_time.timemin.compareTo(itime.timemin)<=0){
        mtemp2.nhoplist.add(mtemp.dest);
        return mtemp2;
        }
    }

    int mindex = dtemp.aliveMsglist.indexOf(mtemp);
    for (int i = 0; i < mindex; i++) {
      
      mtemp2 = dtemp.aliveMsglist.get(i);
      if(mtemp2.delivered)
        continue;
      if(mtemp2.msg_status(itime)!=1)
        continue;  
      if (applyDij)
        dij.RMaxprop_dijkstra(mtemp2, ap_count);
      if (mtemp2.nhoplist.size() > 0)
        if (mtemp2.nhoplist.getFirst() == nextHop
            & mtemp2.m_interval.life_time.timemin.compareTo(itime.timemin)<=0)
          return mtemp2;
    }
    return mtemp;
  }


}
