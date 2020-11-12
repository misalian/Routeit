
package de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms;

import java.io.DataOutputStream;
import java.io.FileOutputStream;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
//import java.util.ListIterator;

import java.util.LinkedList;


import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Active_PeerGenerator;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Device;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Dijkstra;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Event;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.MeetingStats;
/*import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.History;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.MeetingStats;*/
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Msg;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.TSlot;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.VolumeStats;

public class Oracles {

  public Hashtable<String, Device> Peerstable;

  LinkedList<String> tabdevList;

  Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gMsgTable;

  Event eventQ;

  Active_PeerGenerator apg;

  Dijkstra dij;

  int discovery_t;

  boolean resume;

  boolean queuing;

  boolean STBR;

  boolean EPOX;

  boolean perfect;

  int ap_count;

  double bw;
  
  public VolumeStats vs;
  

  public Oracles(Hashtable<String, Device> p, LinkedList<String> td,
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
    STBR = D;
    EPOX = M;
    perfect = P;
    discovery_t = 0;
    //History h = new History(Peerstable,tabdevList,null,null);
    //h.GenHistory(ap_count, bw);

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

  public void STBRInitial(String trace, Date init_T,String bandwidth) {
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
          + bandwidth + "STBR"+(perfect ? "P":"")+"Volume.txt")); 
      DataOutputStream out2 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
          + bandwidth + "STBR"+(perfect ? "P":"")+"bwVolume.txt")); 
      Msg mtemp;
      while (eventQ.size() > 0) {
        vs.vchange=false;
        vs.bwchange= false;
        mtemp = eventQ.removeFirst();
        if (i++ % 50 == 0)
          System.out.println(i++ + " " + eventQ.size() + " "
              + mtemp.m_interval.life_time.timemin);
        if(i>=17)
          i=i+1-1;
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
        if(mtemp.nhoplist.size()==0)
          System.out.println(i);
        STBR(mtemp,null, null);
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

  public void EPOInitial(String trace, Date init_T, String bandwidth) {
    dij = new Dijkstra(Peerstable, tabdevList);
    eventQ = new Event();
    Msg mtemp;
    int i=0;
   // int index ;
    Event psuedoeventQ = new Event();
    Enumeration<Integer> mh_enum = gMsgTable.keys();
    while (mh_enum.hasMoreElements()) {
      Integer mid =   mh_enum.nextElement();
      Enumeration <Integer>c_enum =gMsgTable.get(mid).keys();
      Integer cust= new Integer(0);
      while(c_enum.hasMoreElements()){
        cust =c_enum.nextElement();
        psuedoeventQ.add_event(gMsgTable.get(mid).get(cust).get("0"));       
      }      
    }
    try{
    DataOutputStream out; 
    if(resume)
      out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"+ bandwidth + "EPOXVolume.txt"));
    else
      out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"+ bandwidth + "EPOVolume.txt"));
    while (psuedoeventQ.size() > 0) {      
      Hashtable<String, TSlot>slotBuffer;
      slotBuffer= new Hashtable<String, TSlot>();
      LinkedList<Integer>traversedDevice= new LinkedList<Integer>();
      Msg mtemp2 = psuedoeventQ.removeFirst();
      mtemp = new Msg(mtemp2);
      System.out.println(i++ + " " + psuedoeventQ.size() + " "+ mtemp.m_interval.life_time.timemin);
      Peerstable.get(tabdevList.get(mtemp.src)).hbytes+=mtemp.size;  
      double sizeDelivered =0;
      vs.volume+=mtemp.size;
      vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
      out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n"); 
        
        if(dij.dijkstraEPO(mtemp, bw, ap_count,resume))
        {
          eventQ = new Event();
          eventQ.add_event(mtemp);
          while( eventQ.size()>0){
            Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.add(mtemp);          
          
              Date t = new Date(mtemp.m_interval.life_time.timemin.getTime());
              while(mtemp.custodian!=mtemp.dest) {           
                 STBR(mtemp,slotBuffer,null);
                 
                 if(t.compareTo(mtemp.m_interval.life_time.timemin)==0)
                 {
                   System.out.println(i+" "+mtemp.ID+" "+mtemp.size+" "+mtemp.realSize+" "+
                       mtemp.nhoplist.size()+" "+mtemp.hoplist.size()+" "+sizeDelivered+" "+mtemp.m_interval.life_time.timemin.toString());
                   //System.exit(0);
                 }
                 t.setTime(mtemp.m_interval.life_time.timemin.getTime());
                 if(i++%100==0)
                   System.out.println(mtemp.m_interval.life_time.timemin.toString());
              }
             traversedDevice.addAll(mtemp.hoplist);
             eventQ = new Event();     
             sizeDelivered += mtemp.size;
             vs.volume -= mtemp.size;
             out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n"); 
             if(sizeDelivered>=mtemp.realSize){
               mtemp2.del_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
               mtemp.del_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
               mtemp2.delivered=true;
             }
             else{
               Peerstable.get(tabdevList.get(mtemp.dest)).aliveMsglist.remove(mtemp);
               Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.remove(mtemp);
                mtemp = new Msg(mtemp2);  
               
                gMsgTable.get(mtemp.ID).put(mtemp.custodian, new Hashtable<String, Msg>());
                gMsgTable.get(mtemp.ID).get(mtemp.custodian).put(mtemp.frag_id, mtemp);
                Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.add(mtemp);
                mtemp.size=mtemp2.realSize-sizeDelivered;                     
                if(dij.dijkstraEPO(mtemp, bw, ap_count, resume))
                  eventQ.add_event(mtemp);
              }
            //}           
          }
          if(EPOX & !mtemp.delivered ){
            restoreSlots(slotBuffer);
            restoreStructure(traversedDevice,mtemp2);
          } else destroyStructure(traversedDevice, mtemp2);
        }     
      } 
    }catch(Exception e){
      e.printStackTrace();
    }
  }
  public void restoreStructure(LinkedList<Integer> traversedDevice,Msg mtemp){
   
    Device dtemp ;  
    for(int i=0;i<traversedDevice.size();i++){ 
      dtemp = Peerstable.get(tabdevList.get(traversedDevice.get(i)));
      if(traversedDevice.get(i)==mtemp.dest){
        dtemp.rbytes-=mtemp.size;
        continue;
      }       
      dtemp.meetStat.get(traversedDevice.get(i+1)).msgStat.remove(mtemp.ID);            
      dtemp.meetStat.get(traversedDevice.get(i+1)).msgsAttempt--;
      dtemp.meetStat.get(traversedDevice.get(i+1)).msgsTransf--;
      dtemp.dbytes-=mtemp.size;    
      if(traversedDevice.get(i)!=mtemp.src){
        dtemp.rbytes-=mtemp.size;
      }         
    }         
  }
  public void restoreSlots(Hashtable<String, TSlot>slotBuffer){
    Enumeration<String>sbkeys = slotBuffer.keys();
    while(sbkeys.hasMoreElements()){
      String sbkey = sbkeys.nextElement();
      Device dtemp = Peerstable.get(tabdevList.get(Integer.parseInt(sbkey.substring(0, sbkey.indexOf(" ")))));
      boolean done = false;
      for(int k=0;k<dtemp.sorted_slots.size() & !done;k++){
        //System.out.println(dtemp.sorted_slots.get(k).timemin+" "+dtemp.
        if(dtemp.sorted_slots.get(k).hashCode()==Integer.parseInt(sbkey.substring(sbkey.indexOf(" ")+1))){
          dtemp.sorted_slots.remove(k);
          dtemp.sorted_slots.add(k, slotBuffer.get(sbkey));
          done=true;
        }
      }
      //if(!done)
       // System.err.println("Slot restoration error "+sbkey+" "+slotBuffer.get(sbkey).timemin+" "+slotBuffer.get(sbkey).timemax);
    }
    slotBuffer = new Hashtable<String, TSlot>();
}
  public void STBR(Msg mtemp,  Hashtable<String, TSlot>slotBuffer, Hashtable<String, TSlot>inslotBuffer) {

    Device dtemp = Peerstable.get(tabdevList.get(mtemp.hoplist.getLast()));
    
    TSlot ttemp;
    if(mtemp.nhoplist.getFirst()!=mtemp.dest)
      if(EPOX){
        if(mtemp.nhop!=null)
          if(mtemp.m_interval.life_time.timemin.before(mtemp.nhop.T))
            mtemp.m_interval.life_time.timemin.setTime(mtemp.nhop.T.getTime());
      }
      else{
        if(mtemp.m_interval.life_time.timemin.before(mtemp.nhop.T))
          mtemp.m_interval.life_time.timemin.setTime(mtemp.nhop.T.getTime());
      }
    int i = dtemp.find_slot(mtemp.m_interval.life_time.timemin);
    
    //
      if (i < 0 | i >= dtemp.sorted_slots.size()) {
        vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
        mtemp.m_interval.life_time.timemin
            .setTime(mtemp.m_interval.life_time.timemax.getTime());
        vs.volume -= mtemp.size;       
        vs. vchange = true;
        return;
      }
      //if(!epo){
      if(!EPOX)
        while (i > 5) {
          TSlot itime = dtemp.sorted_slots.remove();
          dtemp.Aptable.get(itime.apname).contacttimeList.remove(itime);
          i--;
        }
      ttemp = dtemp.sorted_slots.get(i);
      if ( mtemp.nhoplist.getFirst() != mtemp.dest) {
        if(EPOX){
          if(mtemp.nhop!=null){
            if(ttemp.timemin.after(mtemp.nhop.T)) { 
              if(!dij.dijkstraEPO(mtemp, bw, ap_count,resume))
                return;
            }
          }                    
        }
        else
          if(ttemp.timemin.after(mtemp.nhop.T)) 
            if(!dij.dijkstra(mtemp, ap_count))
              return;            
      }      
  
      if (mtemp.msg_status(ttemp) == -1){
        vs.volume -= mtemp.size;
        vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
        vs.vchange = true;
        return;
      }
    /*}
    else
      ttemp = dtemp.sorted_slots.get(i);*/
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
      while (!dtemp2.active_slots[scount2++]) ;
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
      mtemp.validSize=mtemp.size;
      if(mtemp.dest==tabdevList.indexOf(dtemp2.mac)){
        int index = dtemp2.if_repmsgexist(mtemp);
        if(index>-1)
        mtemp.validSize = mtemp.realSize-dtemp2.aliveMsglist.get(index).size;
        
      }      
      vs.tstamp.setTime( itime2.timemin.getTime());
      double duration = ((double) itime2.timemax.getTime() - itime2.timemin.getTime()) / 1000;
      if(duration<1){
        mtemp.m_interval.life_time.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime()+1000);
        continue;
      }
      if (!dtemp.meetStat.containsKey(sorted_list[pcount1])) {
             
        dtemp.meetStat.put(new Integer(sorted_list[pcount1]), new MeetingStats(sorted_list[pcount1],new Integer(mtemp.custodian)));
        dtemp2.meetStat.put(mtemp.custodian, new MeetingStats(new Integer(mtemp.custodian),new Integer(sorted_list[pcount1])));               
      }
      apg.recordUpdate( dtemp, dtemp2, mtemp, bw * duration,vs);
      if(resume)
        saveslots(tabdevList.indexOf(dtemp.mac), sorted_list[pcount1], ttemp, ttemp2, slotBuffer,inslotBuffer);
      adjust_timeslots(mtemp, dtemp, dtemp2, ttemp, ttemp2, itime2, bw,duration);
      if (!attempt_transfer(mtemp, dtemp, dtemp2, bw * duration))        
        eventQ.add_event(mtemp);
      
      return;
    }
    if(sorted_list.length>0)
      return;
   
      if (dtemp.sorted_slots.size() > i + 1)
        mtemp.m_interval.life_time.timemin
        .setTime(dtemp.sorted_slots.get(i + 1).timemin.getTime() + 5);
      else
        mtemp.m_interval.life_time.timemin.setTime(ttemp.timemax.getTime() + 500);
    
    eventQ.add_event(mtemp);
  }
  public void saveslots(int i, int j,TSlot ttemp, TSlot ttemp2,Hashtable<String, TSlot>slotBuffer, Hashtable<String, TSlot>inslotBuffer){
    
      String key = Integer.toString(i)+" "+Integer.toString(ttemp.hashCode());
      /*if(i==110)
        System.out.println(key+" "+ttemp.timemin.toString()+" "+ttemp.timemax.toString());
      */
      if(slotBuffer!=null)
        if(!slotBuffer.containsKey(key))
          slotBuffer.put(key, new TSlot(ttemp.timemin.getTime(),ttemp.timemax.getTime(),ttemp.apname));
      if(inslotBuffer!=null)
        if(!inslotBuffer.containsKey(key))
          inslotBuffer.put(key, new TSlot(ttemp.timemin.getTime(),ttemp.timemax.getTime(),ttemp.apname));
    
       key = Integer.toString(j)+" "+Integer.toString(ttemp2.hashCode());
     /*  if(j==110)
         System.out.println(key+" "+ttemp2.timemin.toString()+" "+ttemp2.timemax.toString());*/
    //  System.out.println(key+" "+qtemp2.ttemp2.timemin+" "+qtemp2.ttemp2.timemax);
      if(slotBuffer!=null)
       if(!slotBuffer.containsKey(key))
         slotBuffer.put(key, new TSlot(ttemp2.timemin.getTime(),ttemp2.timemax.getTime(),ttemp2.apname));
      
      if(inslotBuffer!=null)
        if(!inslotBuffer.containsKey(key))
         inslotBuffer.put(key, new TSlot(ttemp2.timemin.getTime(),ttemp2.timemax.getTime(),ttemp2.apname));
       
  }
  public void adjust_timeslots(Msg mtemp, Device dtemp, Device dtemp2,
      TSlot ttemp, TSlot ttemp2, TSlot itime, double bw,double duration) {

    // check if partial delivery has already happened.
    if (resume & mtemp.Ptrans.containsKey(dtemp2.mac))
      mtemp.validSize = Math.min(mtemp.validSize,bw*duration);
    else
      mtemp.validSize = Math.min(mtemp.validSize,bw*duration);
   /* System.out.println(itime.timemin.toString()+" "+itime.timemax.toString()+" "+mtemp.validSize);
    System.out.println(mtemp.m_interval.life_time.timemin.toString()+" "+mtemp.m_interval.life_time.timemax.toString()+" "+mtemp.validSize);
    System.out.println();
*/
    if (itime.timemax.getTime() < itime.timemin.getTime() + discovery_t * 1000
        + (mtemp.validSize / bw) * 1000)
      mtemp.m_interval.life_time.timemin.setTime(itime.timemax.getTime()
          + discovery_t * 1000);
    else
      mtemp.m_interval.life_time.timemin.setTime(itime.timemin.getTime()
          + discovery_t * 1000 + (long) ((mtemp.validSize * 1000)/bw));

    //if (ttemp.timemax.getTime() > mtemp.m_interval.life_time.timemin.getTime()
    //    + discovery_t * 1000)
      ttemp.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime());
    //else if(!EPOX)
   //   dtemp.sorted_slots.remove(ttemp);

   // if (ttemp2.timemax.getTime() > mtemp.m_interval.life_time.timemin.getTime()
     //   + discovery_t * 1000)
      ttemp2.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime());
   // else if(!EPOX)
   //   dtemp2.sorted_slots.remove(ttemp2);
  }

  public boolean attempt_transfer(Msg mtemp, Device dtemp, Device dtemp2,
      double trans_bytes) {
    boolean append=false;
    mtemp.attemptT++;

    Msg mtemp2=mtemp;
    if (EPOX) {
      /*if (mtemp.Ptrans.containsKey(dtemp2.mac)) {
        
        //if (trans_bytes >= mtemp.size - mtemp.validSize) {
          if(mtemp.Ptrans.get(dtemp2.mac)+mtemp.validSize>=mtemp.realSize){
            mtemp.Ptrans.remove(dtemp2.mac);
            if(mtemp.dest!=tabdevList.indexOf(dtemp2.mac)){
              vs.vchange = true;
              vs.volume -=mtemp.realSize;
            }
           
          }
          
         else{
          mtemp.Ptrans.put(dtemp2.mac, new Double(mtemp.Ptrans.get(dtemp2.mac) + mtemp.validSize));
          mtemp2 = STBRmsgtransfer(mtemp);  
          
        }
      } else {
        if (mtemp.realSize>mtemp.validSize){
          //if(mtemp.size<mtemp.realSize){
            mtemp.Ptrans.put(dtemp2.mac, new Double(mtemp.validSize));
            mtemp2 = STBRmsgtransfer(mtemp);                     
          }
        /*}
        else{
          mtemp.Ptrans.put(dtemp2.mac, new Double(mtemp.validSize));
          mtemp2 = STBRmsgtransfer(mtemp);  
          eventQ.add_event(mtemp);
          
        }*/
      //}
      /*append=dtemp2.appendMsg(mtemp);
      if(append){
        if(!dtemp2.ifcompleteMsg(mtemp2))
          eventQ.add_event(mtemp);   
      }else if(mtemp!=mtemp2)
        eventQ.add_event(mtemp);*/
      if (mtemp.realSize > mtemp.validSize)
        mtemp.size = mtemp.validSize;
      //append = dtemp2.appendMsg(mtemp);

      
      return transfer(mtemp2, dtemp, dtemp2);
    } else if (trans_bytes >= mtemp.size)
      return transfer(mtemp, dtemp, dtemp2);
    return false;
  }
  public Msg STBRmsgtransfer(Msg mtemp){
    Msg mtemp2 = new Msg(mtemp); 
    mtemp2.size=mtemp.validSize;
    mtemp2.validSize=mtemp2.size;
    mtemp2.nhoplist.addAll(mtemp.nhoplist);
    mtemp2.nhop = mtemp.nhop;
    return mtemp2;
  }

  public boolean transfer(Msg mtemp, Device dtemp, Device dtemp2) {
    
    if(mtemp.size==mtemp.realSize)
      dtemp.aliveMsglist.remove(mtemp);
    //mtemp.hoplist.add(tabdevList.indexOf(dtemp2.mac));
    while(mtemp.nhoplist.remove()!=tabdevList.indexOf(dtemp2.mac));
  
      apg.adjustCustody(mtemp,null, dtemp, dtemp2);
    
    if (dtemp2.mac.equals(tabdevList.get(mtemp.dest))) {
     
        mtemp.del_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
      vs.vchange=true;
      vs.volume-=mtemp.size;
      mtemp.delivered = true;
      if(mtemp.size==1)
        eventQ.KillAll(mtemp);
      return true;
    }
    if(EPOX){
      if(mtemp.nhop!=null)
        mtemp.nhop = mtemp.nhop.to;
    }
    else
      mtemp.nhop = mtemp.nhop.to;
    
      eventQ.add_event(mtemp);
   

    return true;
  }
  public void destroyStructure(LinkedList<Integer> traversedDevice,Msg mtemp){
    Device dtemp ;
  
    for(int i=0;i<traversedDevice.size();i++){ 
      dtemp = Peerstable.get(tabdevList.get(traversedDevice.get(i)));
      for(int k=0;k<dtemp.sorted_slots.size();k++){
        if(!dtemp.sorted_slots.get(k).timemin.before(mtemp.m_interval.life_time.timemax))
          break;
        if(dtemp.sorted_slots.get(k).timemax.getTime()-dtemp.sorted_slots.get(k).timemin.getTime()<1500)
        {
          dtemp.sorted_slots.remove(k);
          k--;
        }
      }
    }
  }
}