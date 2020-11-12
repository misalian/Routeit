
package de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Active_PeerGenerator;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Apoint;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Device;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Event;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.History;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.MeetingStats;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Msg;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.TSlot;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.VolumeStats;

public class EBEC {

  public Hashtable<String, Device> Peerstable;

  LinkedList<String> tabdevList;

  Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>>gMsgTable;

  Event eventQ;

  int discovery_t;

  Active_PeerGenerator apg;

  int G;

  boolean resume;

  boolean queuing;

  double bw;

  int ap_count;

  int rfact;

  int kfact;
  
  public VolumeStats vs;

  public EBEC(Hashtable<String, Device> p, LinkedList<String> td,
      Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gm, Hashtable<String, Apoint> apTab) {

   /* Peerstable = new Hashtable<String, Device>();
    tabdevList = new LinkedList<String>();
    gMsgTable = new Hashtable<BigDecimal, Msg>();*/
    Peerstable = p;
    tabdevList = td;
    gMsgTable = gm;
    vs = new VolumeStats();
    apg = new Active_PeerGenerator(p, td, gm);
    apg.set_aptable(apTab);

  }

  public void setParameters(int apc, double bw, int r, int k, boolean res,
      boolean que) {
    resume = res;
    queuing = que;
    ap_count = apc;
    this.bw = bw;
    discovery_t = 1;
    rfact = r;
    kfact = k;
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
    
   /* double[] keys = new double[gMsgTable.size()];
    Enumeration<BigDecimal> m_enum = gMsgTable.keys();
    for (int i = 0; i < gMsgTable.size(); i++)
      keys[i] = m_enum.nextElement().doubleValue();

    for (int i = 0; i < keys.length; i++) {
      System.out.println(eventQ.size());
      eventQ.add_event(new BigDecimal(keys[i]));
      
    }*/
    for (int i = 0; i < Peerstable.size() - ap_count; i++){
      Peerstable.get(tabdevList.get(i)).probab = new double[tabdevList.size()
          - ap_count];
      Peerstable.get(tabdevList.get(i)).probab[i]=1;
                                                        
    }
  }
  public void new_msg(Msg mtemp) {

    mtemp.size /= kfact;
    mtemp.frag_count=kfact;
    mtemp.born=true;
    mtemp.kfact = kfact;
    mtemp.value =0;
   
    vs.msgs.put(mtemp.ID.toString()+":"+mtemp.frag_id, mtemp.ID);
    for (int j = 1; j < rfact * kfact; j++) {
      Msg mtemp2 = new Msg(mtemp);
      mtemp2.born=true;
      mtemp2.kfact = mtemp.kfact;
      mtemp2.value =0;
      mtemp2.frag_id = new String(Integer.toString(j));
      gMsgTable.get(mtemp2.ID).get(mtemp2.custodian).put(mtemp2.frag_id, mtemp2);
      Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.add(mtemp2);
      eventQ.add_event(mtemp2);
      vs.msgs.put(mtemp2.ID.toString()+":"+mtemp2.frag_id, mtemp.ID);
    }

  }
  public void EBECC_initial(Date init_T,String trace, String bandwidth) {

    G = kfact;
    Create_InitialEvents();
   
    try {
      DataOutputStream out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
          + bandwidth + "EBECVolume.txt"));
      DataOutputStream out2 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
          + bandwidth + "EBECbwVolume.txt"));
      Msg mtemp = eventQ.getFirst();
      History h = new History(Peerstable, tabdevList, null, init_T);
      h.calc_history(mtemp.m_interval.life_time.timemin, init_T,
        ap_count,bw);
      int i = 0;

    while (eventQ.size() > 0) {
      vs.vchange=false;
      vs.bwchange = false;
      /* for(int k=0;k<eventQ.size()-1;k++){
      if(eventQ.get(k).m_interval.life_time.timemin.after(eventQ.get(k+1).m_interval.life_time.timemin))
      {  System.err.println("time error 3 "+i);
        System.exit(0);
      }
       }/**/
      mtemp = eventQ.removeFirst();
      
      if((mtemp.ID.equals(29)) &mtemp.frag_id.equals("1") & mtemp.hoplist.getLast()==19)
          i=i+1-1;
      if (i++ % 500 == 0)
        System.out.println(eventQ.size() + " " + mtemp.m_interval.life_time.timemin);
      if(i>=12238)
      
        i=i+1-1;
      Date histT = new Date();
      if(!mtemp.born)
      {        
        
        Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.add(mtemp);
        vs.volume+=mtemp.size*rfact;
        Peerstable.get(tabdevList.get(mtemp.src)).hbytes+=mtemp.size*rfact;
        new_msg(mtemp);
        vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
        out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");       
      }
      
      histT.setTime(mtemp.m_interval.life_time.timemin.getTime());
      EBEC_rout(mtemp, bw, ap_count,i);
      /*if((mtemp.ID.equals(29)) &mtemp.frag_id.equals("9"))
        System.out.println(mtemp.hoplist.getLast()+" "+i);*/
      if (eventQ.size() > 0)
        
      if (eventQ.getFirst().m_interval.life_time.timemin.after(histT)) {
        histT.setTime(eventQ.getFirst().m_interval.life_time.timemin.getTime()+1000*60*10);
          h.calc_history(histT,eventQ.getFirst().m_interval.life_time.timemin, ap_count,bw);        
      }
      if (vs.vchange)
        out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");
      if (vs.bwchange)
        out2.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+Long.toString(vs.consumed_bw) + "\n");
      
    }
    out.close();
    Enumeration<String>vsenum = vs.msgs.keys();
    while(vsenum.hasMoreElements()){
      String s  = vsenum.nextElement();
      System.out.println(s);
      Enumeration<Integer> genum = gMsgTable.get(vs.msgs.get(s)).keys();      
      while(genum.hasMoreElements()){
        Integer d = genum.nextElement();
        Enumeration<String>fenum=gMsgTable.get(vs.msgs.get(s)).get(d).keys();
        while(fenum.hasMoreElements()){
          String f = fenum.nextElement();
          mtemp = gMsgTable.get(vs.msgs.get(s)).get(d).get(f);
        System.out.println(s+" "+d.toString()+" "+f+" "+mtemp.custodian+" "+mtemp.m_interval.life_time.timemin+" "+
            mtemp.m_interval.life_time.timemax+" "+mtemp.m_interval.life_time.timemin.getTime());
        }
     }
    }
    } catch (Exception e) {
      System.err.println(e.getMessage()+" "+e.toString());
      e.printStackTrace();
    }
  }



  public void EBEC_rout(Msg mtemp, double bw, int ap_count, int p) {

    Device dtemp = Peerstable.get(tabdevList.get(mtemp.hoplist.getLast()));
    
     /*if(queuing)
     dtemp.sortmsglist();*/
     if(dtemp.msgcountec(mtemp)>16)
       System.out.println("count error");
    TSlot ttemp;
    // while(true)
    {
      int i = dtemp.find_slot(mtemp.m_interval.life_time.timemin);
      if (i < 0 | i >= dtemp.sorted_slots.size()) {
        vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
        mtemp.m_interval.life_time.timemin
            .setTime(mtemp.m_interval.life_time.timemax.getTime());
        vs.volume -= mtemp.size;        
       vs. vchange = true;
       if(vs.msgs.remove(mtemp.ID.toString()+":"+mtemp.frag_id)==null)
         System.out.println("Problem 1 "+mtemp.ID+" "+mtemp.frag_id+" "+mtemp.custodian);
        return;       
      }
      while (i > 5) {
        TSlot itime = dtemp.sorted_slots.remove();
        dtemp.Aptable.get(itime.apname).contacttimeList.remove(itime);
        i--;
      }
      ttemp = dtemp.sorted_slots.get(i);
      if (mtemp.msg_status(ttemp) == -1){
        vs.volume -= mtemp.size;
        vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
        vs.vchange = true;
        if(vs.msgs.remove(mtemp.ID.toString()+":"+mtemp.frag_id)==null)
          System.out.println("Problem 2 "+mtemp.ID+" "+mtemp.frag_id+" "+mtemp.custodian);
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
      int[] sorted_list;
              
      sorted_list = apg.GetPotenPeers(itime, mtemp, new Date(), ap_count,false);
      for (int pcount1 = 0; pcount1 < sorted_list.length; pcount1++) {
        
        Device dtemp2 = Peerstable.get(tabdevList.get(sorted_list[pcount1]));
        if(dtemp2.probab[mtemp.dest]==0|mtemp.hoplist.contains(tabdevList.indexOf(dtemp2.mac)))
          continue;
        if(dtemp2.probab[mtemp.dest]<mtemp.value)
          continue;
        int scount2 = 0;
        TSlot itime2 = new TSlot(mtemp.m_interval.life_time.timemin.getTime(),
            itime.timemax.getTime(), "");
        Apoint aptemp = dtemp2.Aptable.get(itime.apname);
        while (!aptemp.activeslots[scount2++]) ;

        TSlot ttemp2 = (TSlot) aptemp.contacttimeList.get(scount2 - 1);
        aptemp.activeslots[scount2 - 1] = false;

       
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
        double duration = ((double) itime2.timemax.getTime() - itime2.timemin
            .getTime()) / 1000;
        if (duration < discovery_t)
          continue;
        if(backwardKill(dtemp2, mtemp))
          return;
        if(queuing)
          mtemp= apg.getnexthopmsg(dtemp, mtemp, itime2, dtemp2);
        if (mtemp.nhoplist.size()==0 & dtemp.msgcountec(mtemp) > G) {
          if (dtemp.probab[mtemp.dest] < dtemp2.probab[mtemp.dest]) {
            double msg_to_trans = Math
                .ceil((dtemp.msgcountec(mtemp) * dtemp2.probab[mtemp.dest])
                    / (dtemp.probab[mtemp.dest] + dtemp2.probab[mtemp.dest]));
            dtemp.assignNexthop((int)msg_to_trans, tabdevList.indexOf(dtemp2.mac), mtemp, dtemp2.probab[mtemp.dest]);
          } else {
            double msg_to_trans = Math
                .floor((dtemp.msgcountec(mtemp) * dtemp2.probab[mtemp.dest])
                    / (dtemp.probab[mtemp.dest] + dtemp2.probab[mtemp.dest]));
            dtemp.assignNexthop((int)msg_to_trans, tabdevList.indexOf(dtemp2.mac), mtemp, dtemp2.probab[mtemp.dest]);
          }
          
        } else if (dtemp.probab[mtemp.dest] < dtemp2.probab[mtemp.dest]) 
          dtemp.assignNexthop(dtemp.msgcountec(mtemp) , tabdevList.indexOf(dtemp2.mac), mtemp, dtemp2.probab[mtemp.dest]);
         
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
  }
  public boolean  backwardKill( Device dtemp2,Msg mtemp ){
    Msg mtemp2;
    int existIndex = dtemp2.if_repmsgexist(mtemp);
    if(existIndex>-1 ) {
       mtemp2 = dtemp2.aliveMsglist.get(existIndex);
       
      if( mtemp2.delivered){
    
        mtemp.delivered=true;
        mtemp.alive=false;
        if(mtemp.m_interval.life_time.timemin.before(mtemp.m_interval.life_time.timemax))
          mtemp.m_interval.life_time.timemin.setTime(mtemp.m_interval.life_time.timemax.getTime()+50);
        vs.volume-=mtemp.size;
        vs.vchange=true;
        if(vs.msgs.remove(mtemp.ID.toString()+":"+mtemp.frag_id)==null)
          System.out.println("Problem 3 "+mtemp.ID+" "+mtemp.frag_id+" "+mtemp.custodian);
        return true;
       }            
    }
    return false;
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

    itime.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime());
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

    apg.adjustCustody(mtemp, null, dtemp, dtemp2);
    mtemp.value = 0;
    mtemp.nhoplist = new LinkedList<Integer>();
    if (dtemp2.mac.equals(tabdevList.get(mtemp.dest))) {
      if(mtemp.size==1)
        eventQ.KillAll(mtemp);
      
      if(dtemp2.msgcountec(mtemp)>=mtemp.kfact)
      {
        for(int i=0;i<dtemp2.aliveMsglist.size();i++)
          if(dtemp2.aliveMsglist.get(i).ID.equals(mtemp.ID))
              dtemp2.aliveMsglist.get(i).delivered=true;
      }
      mtemp.alive=false;
      
      mtemp.del_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
      vs.volume-=mtemp.size;
      vs.vchange=true;
      if(vs.msgs.remove(mtemp.ID.toString()+":"+mtemp.frag_id)==null)
        System.out.println("Problem 4 "+ mtemp.ID+" "+mtemp.frag_id+" "+mtemp.custodian);
      return true;
    }

    eventQ.add_event(mtemp);
   // dtemp2.aliveMsglist.add(mtemp);
    return true;

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
