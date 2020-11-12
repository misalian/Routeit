
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

public class Flood {

  public Hashtable<String, Device> Peerstable;

  LinkedList<String> tabdevList;

  Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>>gMsgTable;

  Hashtable<String, Apoint> gApTable;
  Event eventQ;

  int discovery_t;

  Active_PeerGenerator apg;

  boolean resume;

  double bw;

  int ap_count;

  boolean queuing;

  //double volume;
  VolumeStats vs;

  DataOutputStream out,out2;
  
  int simul_users;
  int deliverdmsgs;

  //boolean vchange;

  public Flood(Hashtable<String, Device> p, LinkedList<String> td,
      Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gm, Hashtable<String, Apoint> gapTab) {

    /*Peerstable = new Hashtable<String, Device>();
    tabdevList = new LinkedList<String>();
    gMsgTable = new Hashtable<BigDecimal, Msg>();*/
    Peerstable = p;
    tabdevList = td;
    gMsgTable = gm;

    apg = new Active_PeerGenerator(p, td, gm);
    apg.set_aptable(gapTab);
    vs = new VolumeStats();
    gApTable = gapTab;
    deliverdmsgs=0;

    
  }

  public void setParameter(int apc, int susers, double bw, boolean res, boolean que) {

    resume = res;
    discovery_t = 1;
    ap_count = apc;
    this.bw = bw;
    queuing = que;
    simul_users = susers;
  }

  public void Create_InitialEvents() {
    for (int j = 0; j < tabdevList.size() - ap_count; j++) {
      Peerstable.get(tabdevList.get(j)).probab = new double[tabdevList.size()
          - ap_count];
      /* for (int i = 0; i < tabdevList.size() - ap_count; i++) {
        Peerstable.get(tabdevList.get(j)).probab[i] = 1 / ((double) tabdevList
            .size()
            - ap_count - 1);*/
        Peerstable.get(tabdevList.get(j)).meetingcount = new int[tabdevList
            .size()- ap_count];
        //}
    } 

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

  public void flood_initial(Date init_T,String trace, String bandwidth) {

    Create_InitialEvents();
    int i = 0;

    try {
      out = new DataOutputStream(new FileOutputStream("result/" + trace + "/VOL/"
          + bandwidth + "FloodVolume.txt")); // Continue to read lines while
      out2 = new DataOutputStream(new FileOutputStream("result/" + trace + "/VOL/"
          + bandwidth + "FloodbwVolume.txt")); // Continue to read lines while

      //History h = new History(Peerstable, tabdevList, null, null);
      //h.MaxpropHistory(eventQ.getLast().m_interval.life_time.timemax, init_T, ap_count,bw);
      
     Date t = new Date();
      while (eventQ.size() > 0) {
        vs.vchange = false;
        vs.bwchange = false;
        if (i++ % 2000 == 0)
          System.out.println( eventQ.size() + " "
              + eventQ.getFirst().m_interval.life_time.timemin);
        Msg mtemp = eventQ.removeFirst();
        if(!mtemp.born)
        {
          Peerstable.get(tabdevList.get(mtemp.src)).hbytes+=mtemp.size;  
          Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.add(mtemp);
          mtemp.born=true;
          vs.volume+=mtemp.size;
          vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
          out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");       
        }
        /*System.out.println();
          for(int k=0;k<Peerstable.get("63db0dc69e8ea5aa00678d7f3963a3a5d49afa4c").aliveMsglist.size();k++){
            
            //if(Peerstable.get("6dd590acec479cff4802371ad0205ce116b7ffbb").aliveMsglist.get(k).ID.equals(47))
              System.out.println(Peerstable.get("63db0dc69e8ea5aa00678d7f3963a3a5d49afa4c").aliveMsglist.get(k).ID+" "+i);
          }*/
          
        if(mtemp.ID.equals(1) )
          i++;
        t.setTime(mtemp.m_interval.life_time.timemin.getTime());
        flood_delivery(mtemp);
        /*if(!t.before(mtemp.m_interval.life_time.timemin))
        {
          System.out.println(mtemp.ID+" "+mtemp.m_interval.life_time.timemin.toString()+" "+mtemp.custodian);
        }*/
        if (vs.vchange)
          out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");
        if (vs.bwchange)
          out2.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+Long.toString(vs.consumed_bw) + "\n");
        
       
      }
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("Flooding complete "+deliverdmsgs);

  }

  public void flood_delivery(Msg mtemp) {

    Device dtemp = Peerstable.get(tabdevList.get(mtemp.hoplist.getLast()));
    TSlot ttemp;
    /*if (queuing)
      dtemp.sortmsglist();*/
    // while(true)
    {

      int i = dtemp.find_slot(mtemp.m_interval.life_time.timemin);
      if (i < 0 | i >= dtemp.sorted_slots.size()) {
        vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
        mtemp.m_interval.life_time.timemin
            .setTime(mtemp.m_interval.life_time.timemax.getTime());
        vs.volume -= mtemp.size;
        
        vs.vchange = true;
        dtemp.aliveMsglist.remove(mtemp);

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
        dtemp.aliveMsglist.remove(mtemp);
        vs.tstamp=mtemp.m_interval.life_time.timemin;
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
     float cog_prob= getSpectrumProbab(itime, itime.apname,mtemp.ID);
    //  System.out.println(cog_prob);
      //Date limtT = new Date();
     
      int[] sorted_list = apg.GetPotenPeers(itime, mtemp,ap_count,true,-1);

      for (int pcount1 = 0; pcount1 < sorted_list.length; pcount1++) {
        Device dtemp2 = Peerstable.get(tabdevList.get(sorted_list[pcount1]));
        int scount2 = 0;
        int existIndex = dtemp2.if_repmsgexist(mtemp);
        if(existIndex>-1 ) {
           Msg mtemp2 = dtemp2.aliveMsglist.get(existIndex);
           
          if( mtemp2.delivered){
        
            mtemp.delivered=true;
            mtemp.alive=false;
            if(mtemp.m_interval.life_time.timemin.before(mtemp.m_interval.life_time.timemax))
              mtemp.m_interval.life_time.timemin.setTime(mtemp.m_interval.life_time.timemax.getTime()+50);
            vs.volume-=mtemp.size;
            vs.vchange=true;
            dtemp.aliveMsglist.remove(mtemp);

            return ;
           }
          continue;
        }
        
        TSlot itime2 = new TSlot(mtemp.m_interval.life_time.timemin.getTime(),
            itime.timemax.getTime(), "");
        // while(!dtemp2.active_slots[scount2++]);
        Apoint aptemp = dtemp2.Aptable.get(itime.apname);
        while (!aptemp.activeslots[scount2++]) ;

        TSlot ttemp2 = (TSlot) aptemp.contacttimeList.get(scount2 - 1);
        aptemp.activeslots[scount2 - 1] = false;
        // dtemp2.active_slots[scount2-1]=false;

        if (ttemp2.timemin.after(mtemp.m_interval.life_time.timemin)) {
          itime2.timemin.setTime(ttemp2.timemin.getTime());
          mtemp.m_interval.life_time.timemin.setTime(itime2.timemin.getTime());
        }
        if (ttemp2.timemax.before(itime2.timemax))
          itime2.timemax.setTime(ttemp2.timemax.getTime());
        if(itime2.timemax.after(mtemp.m_interval.life_time.timemax))
          itime2.timemax.setTime(mtemp.m_interval.life_time.timemax.getTime());

        if(!eventQ.isFirstMsg(mtemp)){
          eventQ.add_event(mtemp);
          return;
        }
        aptemp.contacttimeList.getFirst().add_simul_trans(itime2);

        double duration = ((double) itime2.timemax.getTime() - itime2.timemin.getTime()) / 1000;
        duration = duration *cog_prob/aptemp.contacttimeList.getFirst().Simul_Trans.size();

        if (duration <= discovery_t) {
          continue;
        }
        if (queuing) {
          Msg mtemp2 = apg.getnexthopmsg(dtemp, mtemp, itime2,
              dtemp2);
          if (!mtemp.equals(mtemp2)) {
            if(eventQ.remove(mtemp2)){
              eventQ.add_event(mtemp);
              if (itime2.timemin.after(mtemp.m_interval.life_time.timemin))
                mtemp2.m_interval.life_time.timemin.setTime(itime2.timemin
                  .getTime());
              mtemp = mtemp2;
            }
          }
        }
      /* if(!dtemp.meetStat.containsKey(tabdevList.indexOf(dtemp2.mac)))
       {
         System.out.println(ttemp.timemin.toString()+" "+ttemp.timemax.toString());
         System.out.println(ttemp2.timemin.toString()+" "+ttemp2.timemax.toString());
       }
       if(tabdevList.indexOf(dtemp.mac)==518 && tabdevList.indexOf(dtemp2.mac)==469)
       {
         System.out.println(ttemp.timemin.toString()+" "+ttemp.timemax.toString());
         System.out.println(ttemp2.timemin.toString()+" "+ttemp2.timemax.toString());
        }
*/
        if (!dtemp.meetStat.containsKey(sorted_list[pcount1])) {             
          dtemp.meetStat.put(new Integer(sorted_list[pcount1]), new MeetingStats(sorted_list[pcount1],new Integer(mtemp.custodian)));
          dtemp2.meetStat.put(mtemp.custodian, new MeetingStats(new Integer(mtemp.custodian),new Integer(sorted_list[pcount1])));             
        }
        vs.tstamp.setTime(itime2.timemin.getTime());
        //ttemp.add_simul_trans(ttemp2);
        //ttemp2.add_simul_trans(ttemp);
        //TSlot ttemp3= gApTable.get(ttemp.apname).contacttimeList.
         //   get(gApTable.get(ttemp.apname).find_slot(mtemp.m_interval.life_time.timemin));
        
        apg.recordUpdate( dtemp, dtemp2, mtemp, bw * duration/aptemp.contacttimeList.getFirst().Simul_Trans.size(),vs);
        adjust_timeslots(mtemp, dtemp, dtemp2, ttemp, ttemp2, itime2, bw);
        if(simul_users>1)
          adjustSimultimeslot(aptemp);
     
        
        //vs.tstamp = mtemp.m_interval.life_time.timemin;
        if (!attempt_transfer(mtemp, dtemp, dtemp2, bw * duration, ap_count))
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
  public float getSpectrumProbab(TSlot itime, String apname, Integer mID){
    Apoint aptemp = gApTable.get(apname);
    float competeDevCount=0;
    int totalDevCount=0;
    Hashtable <Integer,LinkedList<Integer>>exchMsg=new Hashtable<Integer,LinkedList<Integer>>();
    Hashtable <Integer,Device>compDev=new Hashtable<Integer,Device>();

   for(int i=0;i<aptemp.devlist.size();i++){
      Device dtemp = aptemp.devlist.get(i);
      if(dtemp.sorted_slots.size()<1)
        continue;
      
       /* if((aptemp.devlist.get(i).sorted_slots.getFirst().timemin.after(itime.timemin)) ){
        competeDevCount++;
        //System.out.println(aptemp.devlist.get(i).sorted_slots.getFirst().timemin.toString()+" "+itime.timemin.toString());
      }*/
      if((!dtemp.sorted_slots.getFirst().timemin.after(itime.timemin)) &&
          (dtemp.sorted_slots.getFirst().timemax.after(itime.timemin))  ){
          if(dtemp.sorted_slots.size()>0 && dtemp.aliveMsglist.size()>0){     
            compDev.put(tabdevList.indexOf(dtemp.mac), dtemp);
            Msg mtemp;
            for(int j=0;j<dtemp.aliveMsglist.size();j++){
              mtemp = dtemp.aliveMsglist.get(j);
              if(mtemp.m_interval.life_time.timemin.after(mtemp.m_interval.life_time.timemax))
                continue;
              if(dtemp.aliveMsglist.get(j).m_interval.life_time.timemin.after(itime.timemin))
                continue;
              if(!exchMsg.containsKey(dtemp.aliveMsglist.get(j).ID))
                exchMsg.put(dtemp.aliveMsglist.get(j).ID, new LinkedList<Integer>()); 
              exchMsg.get(dtemp.aliveMsglist.get(j).ID).add(tabdevList.indexOf(dtemp.mac)); 
              }
            totalDevCount++;
          //System.out.println(aptemp.devlist.get(i).sorted_slots.getFirst().timemin.toString()+" "+itime.timemin.toString());
          }
      }
    } 
   
   Enumeration <Integer>mEnum=exchMsg.keys();
   boolean mtagged;
   while(mEnum.hasMoreElements()){
     Integer tmID = mEnum.nextElement();
     Enumeration <Integer>dEnum = compDev.keys();
     mtagged=false;
     while(dEnum.hasMoreElements() && !mtagged){
       Device dtemp =compDev.get(dEnum.nextElement());
       int i=0;
       for(;i<dtemp.aliveMsglist.size();i++)
       {
         if(dtemp.aliveMsglist.get(i).ID.equals(tmID))
           break;
       }
       if(i>=dtemp.aliveMsglist.size()){
         competeDevCount++;
         mtagged=true;
         compDev.remove(tabdevList.indexOf(dtemp.mac));
       }        
     }     
   }
   
    if(competeDevCount==0)
      competeDevCount++;
  // System.out.println(competeDevCount+" "+totalDevCount);
    return 1/competeDevCount;
  }
  public boolean  backwardKill(Device dtemp, Device dtemp2,Msg mtemp ){
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
          + discovery_t * 1000 +(long) ((validSize * 1000) /bw) );
  //adjustSimultimeslot(dtemp, ttemp, ttemp2,mtemp);
  //adjustSimultimeslot(dtemp2, ttemp2, ttemp, mtemp);

    
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
  public void adjustSimultimeslot( Apoint aptemp){
  
    if(aptemp.contacttimeList.getFirst().Simul_Trans.size()==simul_users)
    {
      TSlot ttemp = aptemp.contacttimeList.getFirst();
      TSlot ttemp2 = new TSlot();
      ttemp2.timemin.setTime(ttemp.timemin.getTime());
      ttemp2.timemax.setTime(ttemp.timemax.getTime());
      int k=0;
      for(int i=0;i<simul_users;i++){
        if(ttemp2.timemax.after(ttemp.Simul_Trans.get(i).timemax)){
          ttemp2.timemin.setTime(ttemp.Simul_Trans.get(i).timemin.getTime());
          ttemp2.timemax.setTime(ttemp.Simul_Trans.get(i).timemax.getTime());
          k=i;
        }
      }
      ttemp.timemin.setTime(ttemp2.timemax.getTime()+1000);
      TSlot ttemp3 = ttemp.Simul_Trans.remove(k);
      if(aptemp.congestiontimeList.size()==0)
        aptemp.congestiontimeList.add(ttemp3);
      else
      {
        if(aptemp.congestiontimeList.getLast().timemax.after(ttemp2.timemin))
          aptemp.congestiontimeList.getLast().timemax.setTime(ttemp2.timemax.getTime());
        else
          aptemp.congestiontimeList.add(ttemp3);          
      }      
    }     
  }
  /*public void adjustSimultimeslot(Device dtemp, TSlot ttemp, TSlot ttemp2, Msg mtemp){
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
*/
  public boolean attempt_transfer(Msg mtemp, Device dtemp, Device dtemp2,
      double trans_bytes, int ap_count) {
    mtemp.attemptT++;

    if (resume) {
      if (mtemp.Ptrans.containsKey(dtemp2.mac)) {
        if (trans_bytes >= mtemp.size - mtemp.Ptrans.get(dtemp2.mac)) {
          mtemp.Ptrans.remove(dtemp2.mac);
          return transfer(mtemp, dtemp, dtemp2, ap_count);
        } else
          mtemp.Ptrans.put(dtemp2.mac, new Double(mtemp.Ptrans.get(dtemp2.mac)
              + trans_bytes));
      } else {
        if (trans_bytes >= mtemp.size)
          return transfer(mtemp, dtemp, dtemp2, ap_count);
        else
          mtemp.Ptrans.put(dtemp2.mac, new Double(trans_bytes));
      }
    } else if (trans_bytes >= mtemp.size){
      dtemp.dbytes+=mtemp.size;
      dtemp2.rbytes+=mtemp.size;
      dtemp.dmsgs++;
      
      return transfer(mtemp, dtemp, dtemp2, ap_count);
    }
    
    dtemp.tbytes+=trans_bytes;
    dtemp2.tbytes+=trans_bytes;
    return false;
  }

  public boolean transfer(Msg mtemp, Device dtemp, Device dtemp2, int ap_count) {

    vs.vchange = true;

    Msg mtemp2 = new Msg(mtemp);
    apg.adjustCustody(mtemp, mtemp2, dtemp, dtemp2);
    //dtemp2.aliveMsglist.add(mtemp2);
    if (tabdevList.indexOf(dtemp2.mac) == mtemp.dest) {
      mtemp2.del_time.setTime(mtemp2.m_interval.life_time.timemin.getTime());
      // dtemp.Msglist.remove(mtemp.ID);
      vs.volume -= mtemp.size;
      mtemp2.delivered=true;
      mtemp.delivered=true;
      mtemp.alive=false;
      mtemp2.alive=false;
      if(mtemp.size==1)
        eventQ.KillAll(mtemp);
      deliverdmsgs++;

      return true;
    }
    vs.volume += mtemp.size;
    eventQ.add_event(mtemp);
    eventQ.add_event(mtemp2);
    return true;
  }
}
