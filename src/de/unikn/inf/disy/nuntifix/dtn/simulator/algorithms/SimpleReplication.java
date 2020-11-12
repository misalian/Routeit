
package de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms;

import java.io.DataOutputStream;
import java.io.FileOutputStream;

import java.util.Collections;
import java.util.Comparator;
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
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.PossibleHop;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.TSlot;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.VolumeStats;

public class SimpleReplication {

  public Hashtable<String, Device> Peerstable;

  LinkedList<String> tabdevList;

  Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gMsgTable;

  Event eventQ;

  int discovery_t;

  Active_PeerGenerator apg;

  LinkedList<DevDelivery>[] delivery;

  History h;

  double bw;

  boolean resume;

  boolean queuing;

  boolean history;

  //boolean EC;

  int ap_count;

  int rfact;

  int kfact;

  boolean extended;
  
  public VolumeStats vs;


  public SimpleReplication(Hashtable<String, Device> p, LinkedList<String> td,
      Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gm, Hashtable<String, Apoint> gapTab) {

   /* Peerstable = new Hashtable<String, Device>();
    tabdevList = new LinkedList<String>();
    gMsgTable = new Hashtable<BigDecimal, Msg>();*/
    Peerstable = p;
    tabdevList = td;
    gMsgTable = gm;

    apg = new Active_PeerGenerator(p, td, gm);
    apg.set_aptable(gapTab);
    delivery = new LinkedList[gMsgTable.size()];
    vs = new VolumeStats();
  }

  public void setParameters(double bw, int apc, int r, int k, boolean h,
      boolean ec, boolean extend, boolean res, boolean que) {

    resume = res;
    queuing = que;
    discovery_t = 0;
    rfact = r;
    kfact = k;
    extended = extend;
    ap_count = apc;
    history = h;
   // EC = ec;
    this.bw = bw;
  }

  class DevDelivery {

    int dev;
    double probab;
    public DevDelivery(int i, double p) {
      dev = i;
      probab = p;
    }
  }

  public class ProbabComparator implements Comparator {

    public int compare(Object o1, Object o2) {

      if (((DevDelivery) o1).probab > ((DevDelivery) o2).probab)
        return -1;
      if (((DevDelivery) o1).probab < ((DevDelivery) o2).probab)
        return 1;
      return 0;
    }
  }

  public void Create_InitialEvents(Date init_T) {
    eventQ = new Event();
    Enumeration<Integer> mh_enum = gMsgTable.keys();
    while (mh_enum.hasMoreElements()) {
      Integer mid =   mh_enum.nextElement();
      Enumeration <Integer>c_enum =gMsgTable.get(mid).keys();
      Integer cust= new Integer(0);
      while(c_enum.hasMoreElements()){
        cust =c_enum.nextElement();
        Msg mtemp = gMsgTable.get(mid).get(cust).get("0");
        mtemp.frag_count = kfact;
        mtemp.rfact = (kfact*rfact);
        mtemp.size /= kfact;
        eventQ.add_event(mtemp);       
      }      
    }           
    for (int i = 0; i < tabdevList.size() - ap_count; i++)
      Peerstable.get(tabdevList.get(i)).probab = new double[tabdevList.size()- ap_count];   
  }

 
  public void calc_path(Msg mtemp) {
    mtemp.mynhoplist = new Hashtable<Integer,PossibleHop>();
    delivery[mtemp.ID.intValue()] = new LinkedList<DevDelivery>();
    Device dtemp;
    dtemp = Peerstable.get(tabdevList.get(mtemp.src));
    for (int i = 0; i < dtemp.probab.length; i++) {
      if (dtemp.probab[i] > 0)
        delivery[mtemp.ID.intValue()].add(new DevDelivery(i, Peerstable
            .get(tabdevList.get(i)).probab[mtemp.dest]));
    }
    if(delivery[mtemp.ID.intValue()].size()<1)
      return;    
    Collections.sort(delivery[mtemp.ID.intValue()], new ProbabComparator());   
    for(int i=0;i<Math.min(delivery[mtemp.ID.intValue()].size(),mtemp.rfact);i++){      
      mtemp.mynhoplist.put(delivery[mtemp.ID.intValue()].get(i).dev, new PossibleHop());
    }    
  }
  public void SR_initial(Date init_T,String trace, String bw) {

    h = new History(Peerstable, tabdevList, null, null);
    Create_InitialEvents(init_T);
    int i = 0;
    try {
      DataOutputStream out,out2;
    if (history) {
      if(kfact>1){
        out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + bw + "ErasureCodingVolume.txt"));
        out2 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + bw + "ErasureCodingbwVolume.txt"));
      }
      else{
        out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + bw + "HistSimpleRepliVolume.txt"));
        out2 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + bw + "HistSimpleReplibwVolume.txt"));
      }
      Date histT = new Date();
      histT.setTime(eventQ.getFirst().m_interval.life_time.timemin.getTime());
      h.calc_zebraHistory(histT, init_T, ap_count);
      while (eventQ.size() > 0) {
        vs.vchange=false;
        vs.bwchange = false;
        Msg mtemp  = eventQ.removeFirst();
        if (i++ % 100 == 0)
          System.out.println(eventQ.size() + " "+ mtemp.m_interval.life_time.timemin);
      
        if(!mtemp.born)
        {          
          Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.add(mtemp);
          mtemp.born=true;
          if(kfact>1){
            vs.volume += mtemp.size*kfact*rfact;
            Peerstable.get(tabdevList.get(mtemp.src)).hbytes+=mtemp.size*kfact*rfact;
          }
          else{
            vs.volume+=mtemp.size;
            Peerstable.get(tabdevList.get(mtemp.src)).hbytes+=mtemp.size;
          }         
          vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
          out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");       
        }                
        SR_Rout(mtemp);        
        if (eventQ.size() > 0){
          if(histT.after(eventQ.getFirst().m_interval.life_time.timemin)){
            histT.setTime(eventQ.getFirst().m_interval.life_time.timemin.getTime()+600000);
            h.calc_zebraHistory(eventQ.getFirst().m_interval.life_time.timemin,histT, ap_count);
          }
        }
        if (vs.vchange)         
          out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");
        if (vs.bwchange)
          out2.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+Long.toString(vs.consumed_bw) + "\n");
         
     }
    } else {

      if(kfact>1){
        System.exit(0);
        out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + bw + "SRECVolume.txt"));
        out2 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + bw + "SimpleReplibwVolume.txt"));        
      }
      else{
        out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + bw + "SimpleRepliVolume.txt"));
        out2 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + bw + "SimpleReplibwVolume.txt"));        
      }
        while (eventQ.size() > 0) {
          vs.vchange=false;
          vs.bwchange = false;
          Msg mtemp = eventQ.removeFirst(); 
          if (i++ % 1000 == 0)
            System.out.println(eventQ.size() + " "
                + mtemp.m_interval.life_time.timemin);
          if(!mtemp.born)
          {
            Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.add(mtemp);
            mtemp.born=true;
            vs.volume+=mtemp.size;
            vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
            out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");
          }
          SR_Rout(mtemp);
          if (vs.vchange)
            out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");        
          if (vs.bwchange)
            out2.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+Long.toString(vs.consumed_bw) + "\n");
           
      }
    }
    out.close();
    } catch (Exception e) {
      System.err.println(e.getMessage()+" "+e.toString());
      e.printStackTrace();
      //System.out.println(mt)
    }
  }

  public void SR_Rout(Msg mtemp) {

    Device dtemp = Peerstable.get(tabdevList.get(mtemp.custodian));

    TSlot ttemp;
    int i = dtemp.find_slot(mtemp.m_interval.life_time.timemin, 0,
        dtemp.sorted_slots.size() - 1);

      if (i < 0 | i >= dtemp.sorted_slots.size()) {
        vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
        
        if(kfact>1 & mtemp.rfact>0)//in case a message that has not been replicated fully 
          vs.volume-=(mtemp.size*mtemp.rfact);       
        else
          vs.volume-=mtemp.size;
        
        mtemp.m_interval.life_time.timemin
        .setTime(mtemp.m_interval.life_time.timemax.getTime());
        vs.vchange = true;
        return;
      }

      while (i > 5) {
        TSlot itime = dtemp.sorted_slots.remove();
        dtemp.Aptable.get(itime.apname).contacttimeList.remove(itime);
        i--;
      }

      ttemp = dtemp.sorted_slots.get(i);

      if (mtemp.msg_status(ttemp) == -1) {
        if(kfact>1 & mtemp.rfact>0)//in case a message that has not been replicated fully 
          vs.volume-=(mtemp.size*mtemp.rfact);        
        else
          vs.volume-=mtemp.size;
          
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
      
      if (history | !mtemp.custodian.equals(mtemp.src)) {
        if(mtemp.custodian.equals(mtemp.src))
          calc_path(mtemp);
        if (find_histnexthop(mtemp, dtemp, ttemp.apname, ttemp, itime))
          return;
      } else 
        if (find_nexthop(mtemp, dtemp, ttemp.apname, ttemp, itime))
        return;
      if (dtemp.sorted_slots.size() > i + 1)
        mtemp.m_interval.life_time.timemin.setTime(dtemp.sorted_slots
            .get(i + 1).timemin.getTime() + 5);
      else
        mtemp.m_interval.life_time.timemin
            .setTime(ttemp.timemax.getTime() + 500);

      eventQ.add_event(mtemp);

  }

  public boolean find_nexthop(Msg mtemp, Device dtemp, String apname,
      TSlot ttemp, TSlot itime) {

    int[] sorted_list;
    sorted_list = apg.GetPotenPeers(itime, mtemp, new Date(), ap_count,true);
    
    for (int pcount1 = 0; pcount1 < sorted_list.length; pcount1++) {
      Device dtemp2 = Peerstable.get(tabdevList.get(sorted_list[pcount1]));
      int existIndex = dtemp2.if_repmsgexist(mtemp);
      if(existIndex>-1 ) {
       /* Msg  mtemp2 = dtemp2.aliveMsglist.get(existIndex);
         
        if( mtemp2.delivered){
      
          mtemp.delivered=true;
          mtemp.alive=false;
          if(mtemp.m_interval.life_time.timemin.before(mtemp.m_interval.life_time.timemax))
            mtemp.m_interval.life_time.timemin.setTime(mtemp.m_interval.life_time.timemax.getTime()+50);
          vs.volume-=mtemp.size;
          vs.vchange=true;
          return true;
         }    */
        continue;
      }
      TSlot itime2 = new TSlot(itime.timemin.getTime(),
          itime.timemax.getTime(), "");

      int scount2 = 0;
      TSlot ttemp2;
      Apoint aptemp = dtemp2.Aptable.get(itime.apname);
      while (!aptemp.activeslots[scount2++]);
      ttemp2 = (TSlot) aptemp.contacttimeList.get(scount2 - 1);
       aptemp.activeslots[scount2 - 1] = false;
       double duration;
       if((duration = prepare_timeslots(dtemp, ttemp, dtemp2, ttemp2, itime, itime2, mtemp))>0){
         vs.tstamp.setTime(itime2.timemin.getTime());
         if(eventQ.isFirstMsg(mtemp)){
           if(queuing)
             mtemp=apg.getnexthopmsg(dtemp, mtemp, itime2, dtemp2);
           if (!dtemp.meetStat.containsKey(sorted_list[pcount1])) {             
             dtemp.meetStat.put(new Integer(sorted_list[pcount1]), new MeetingStats(sorted_list[pcount1],new Integer(mtemp.custodian)));
             dtemp2.meetStat.put(mtemp.custodian, new MeetingStats(new Integer(mtemp.custodian),new Integer(sorted_list[pcount1])));             
           }
           apg.recordUpdate( dtemp, dtemp2, mtemp, bw * duration,vs);
           adjust_timeslots(mtemp, dtemp, dtemp2, ttemp, ttemp2, itime2);
           if (!attempt_transfer(mtemp, dtemp, dtemp2, bw * duration))
               eventQ.add_event(mtemp);
            return true;
         }else{
           eventQ.add_event(mtemp);
           return true;
         }
       }
    }
    return false;
  }

  public boolean find_histnexthop(Msg mtemp, Device dtemp, String apname,
      TSlot ttemp, TSlot itime) {
    int[] sorted_list;
    sorted_list = apg.GetMyNexthop(itime, mtemp, ap_count);
    //sorted_list = apg.GetPotenPeers(itime, mtemp, new Date(), ap_count,true);
    for (int pcount1 = 0; pcount1 < sorted_list.length; pcount1++) {
      Device dtemp2 = Peerstable.get(tabdevList.get(sorted_list[pcount1]));
      int existIndex = dtemp2.if_repmsgexist(mtemp);
      if(existIndex>-1 ) {
        /*Msg  mtemp2 = dtemp2.aliveMsglist.get(existIndex);
         
        if( mtemp2.delivered){
      
          mtemp.delivered=true;
          mtemp.alive=false;
          if(mtemp.m_interval.life_time.timemin.before(mtemp.m_interval.life_time.timemax))
            mtemp.m_interval.life_time.timemin.setTime(mtemp.m_interval.life_time.timemax.getTime()+50);
          vs.volume-=mtemp.size;
          vs.vchange=true;
          return true;
         }    */
        continue;
      }
      TSlot itime2 = new TSlot(itime.timemin.getTime(),
          itime.timemax.getTime(), "");

      int scount2 = 0;
      while (!dtemp2.active_slots[scount2++]);

      TSlot ttemp2 = (TSlot) dtemp2.sorted_slots.get(scount2 - 1);
      dtemp2.active_slots[scount2 - 1] = false;

      double duration;
      if((duration = prepare_timeslots(dtemp, ttemp, dtemp2, ttemp2, itime, itime2, mtemp))>0){
        vs.tstamp.setTime(itime2.timemin.getTime());
        if(eventQ.isFirstMsg(mtemp)){
          if(queuing)
            mtemp= apg.getnexthopmsg(dtemp, mtemp, itime2, dtemp2);
          if (!dtemp.meetStat.containsKey(sorted_list[pcount1])) {
            
            dtemp.meetStat.put(new Integer(sorted_list[pcount1]), new MeetingStats(sorted_list[pcount1],new Integer(mtemp.custodian)));
            dtemp2.meetStat.put(mtemp.custodian, new MeetingStats(new Integer(mtemp.custodian),new Integer(sorted_list[pcount1])));             
          }
          apg.recordUpdate( dtemp, dtemp2, mtemp, bw * duration,vs);
          adjust_timeslots(mtemp, dtemp, dtemp2, ttemp, ttemp2, itime2);
          if (!attempt_transfer(mtemp, dtemp, dtemp2, bw * duration))
              eventQ.add_event(mtemp);
           return true;
        }
        else{
          eventQ.add_event(mtemp);
          return true;
        }
      }
   }
   return false;

  }
  public double prepare_timeslots(Device dtemp,TSlot ttemp,Device dtemp2, TSlot ttemp2,TSlot itime, TSlot itime2, Msg mtemp){
    if (ttemp2.timemin.after(itime.timemin))
      itime2.timemin.setTime(ttemp2.timemin.getTime());
    if (ttemp2.timemax.before(itime.timemax))
      itime2.timemax.setTime(ttemp2.timemax.getTime());
    mtemp.m_interval.life_time.timemin.setTime(itime2.timemin.getTime());

    return ((double) itime2.timemax.getTime() - itime2.timemin.getTime()) / 1000;  
  }
  /*public void setFirstMsg(Device dtemp, Msg mtemp, TSlot itime2,Device dtemp2){

     dtemp.sortmsglist();

     Msg mtemp2 = apg.getnexthopmsg(dtemp, mtemp, itime2,  dtemp2);
     if (mtemp != mtemp2) {
        eventQ.remove(mtemp2);
        eventQ.add_event(mtemp);
        mtemp = mtemp2;
      }      
  }*/

  public void adjust_timeslots(Msg mtemp, Device dtemp, Device dtemp2,
      TSlot ttemp, TSlot ttemp2, TSlot itime) {

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
    
      
    mtemp.rfact--;
    
    if(mtemp.custodian.equals(mtemp.src) & mtemp.rfact>0){
        Msg mtemp2 = new Msg(mtemp);
        eventQ.add_event(mtemp);
        mtemp2.mynhoplist = new Hashtable<Integer, PossibleHop>();
        if(kfact>1)
          mtemp2.frag_id = new String(Integer.toString(mtemp.rfact));
        apg.adjustCustody(mtemp, mtemp2, dtemp, dtemp2);
        dtemp2.aliveMsglist.add(mtemp2);    
        if (dtemp2.mac.equals(tabdevList.get(mtemp2.dest))) {
          mtemp2.del_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
          vs.volume-=mtemp.size;
          vs.vchange=true;
          mtemp2.delivered=true;
          mtemp.delivered=true;
          mtemp.alive=false;
          mtemp2.alive=false;
          if(mtemp.size==1)
            eventQ.KillAll(mtemp);
          
          //gMsgTable.get(mtemp.ID).g.sizel+=mtemp.size;         
          return true;
        }
        if(kfact==1)
        {
          vs.volume+=mtemp2.size;
          vs.vchange=true;
        }
        
           
        eventQ.add_event(mtemp2);
        
        
        return true;
            
    }
    
    apg.adjustCustody(mtemp, null, dtemp, dtemp2);
    dtemp2.aliveMsglist.add(mtemp);    
    if (dtemp2.mac.equals(tabdevList.get(mtemp.dest))) {
      mtemp.del_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
      vs.vchange=true;
      vs.volume-=mtemp.size;
      //gMsgTable.get(new BigDecimal(mtemp.ID.intValue())).sizel+=mtemp.size;
      return true;
    }
    mtemp.mynhoplist = new Hashtable<Integer,PossibleHop>();
   
   
    eventQ.add_event(mtemp);
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
  }*/
}
