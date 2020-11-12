
package de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;


//import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.LogRecord;

//import de.unikn.inf.disy.nuntifix.dtn.simulator.bloomfilter.*;
import de.unikn.inf.disy.nuntifix.dtn.simulator.Global;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.*;
import de.unikn.inf.disy.util.logging.LoggingFacility;


public class CognitiveDFlood {

  public Hashtable<String, Device> Peerstable;

  LinkedList<String> tabdevList;

  LinkedList<String> tabapList;

  Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>>gMsgTable;

  Event eventQ;

  int discovery_t;

  Active_PeerGenerator apg;

  Dijkstra dij;

  int b_thresh;

  History h;

  boolean resume;

  boolean queuing;

  int ap_count;

  double bw;

  VolumeStats vs;
 
  int metricSelect;
  
  public double frag_factor;

  int p = 0;
   
  LoggingFacility lf ;
  
  LogRecord record ;
  
  boolean enc;
 
  public int hours;
  
  int kfact;
  
  public int msgtrack=1; 
    
  public NodeList cNode ;

  public boolean dFlood;
  public CognitiveDFlood(Hashtable<String, Device> p, LinkedList<String> td,
      Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> 
      gm, Hashtable<String, Apoint> gapTab,boolean enc,int m) {

    Peerstable = p;
    tabdevList = td;
    gMsgTable = gm;
    apg = new Active_PeerGenerator(p, td, gm);
    

   // Continue to read lines while
    apg.set_aptable(gapTab);  
    vs = new VolumeStats();
    metricSelect=1;
    lf = LoggingFacility.getInstance(null);
      
    this.enc=enc;
    hours=1;
    this.kfact=m;
     
  }

  public void setParameters(int apc, double bw, boolean res, boolean que, int msgtrack, boolean Df) {

    discovery_t = 0;
    resume = res;
    queuing = que;
    this.bw = bw;
    ap_count = apc;
    this.msgtrack= msgtrack;
    dFlood=Df;
    
    if(dFlood)
      record = new LogRecord(Level.INFO, "Starting Cognitive Dflood encoding="+Boolean.toString(enc)+ " Fragament= "+Integer.toString(kfact));
    else
      record = new LogRecord(Level.INFO, "Starting Cognitive Nile encoding="+Boolean.toString(enc)+ " Fragament= "+Integer.toString(kfact));
      
    lf.log(record);
    //Peerstable.put("debug", new Device("debug"));    
  }

  public void Create_InitialEvents(Date init_T) {

    int devlistsize = Peerstable.size() - ap_count;
    Device dtemp;
   /* for (int i = 0; i < devlistsize; i++) {
      dtemp = Peerstable.get(tabdevList.get(i));
      dtemp.meetStat = new Hashtable<Integer, MeetingStats>();
     // dtemp.probab = new double[devlistsize];
      /*for (int j = 0; j < devlistsize; j++)
        dtemp.probab[j] = 1 / ((double) devlistsize );*/
    //}

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
  public void createFrags(Msg mtemp){

    mtemp.frag_count = kfact;
    mtemp.replicate = true;
    if(mtemp.ID.equals(msgtrack)){
      mtemp.cNode =  new NodeList(tabdevList.get(mtemp.src),mtemp.m_interval.life_time.timemin,-1);
      cNode=mtemp.cNode;
    }
    frag_factor = mtemp.size/mtemp.frag_count;
    mtemp.size=frag_factor;
    vs.volume+=mtemp.size;
    mtemp.ArrivalTime.setTime(mtemp.m_interval.life_time.timemin.getTime());
    if(vs.msgs.put(mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString(), mtemp.ID)!=null){
      record = new LogRecord(Level.FINEST,"Message born error "+ mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString()+" "+mtemp.m_interval.life_time.timemin+" "+mtemp.m_interval.life_time.timemax);
      lf.log(record);
    }
    Msg mtemp2;
    
    for(int i=1;i<mtemp.frag_count;i++){
      mtemp2 = new Msg(mtemp);
      mtemp2.born=true;
      mtemp2.replicate = true;
      mtemp2.frag_id = new String(Integer.toString(i));
      mtemp2.cFrags[0]=mtemp2.frag_id;
      mtemp2.size = frag_factor;
      mtemp2.ArrivalTime.setTime(mtemp.m_interval.life_time.timemin.getTime());
      gMsgTable.get(mtemp2.ID).get(mtemp2.custodian).put(mtemp2.frag_id, mtemp2);
    /*  if(Math.random()>0.5){
        if(!gMsgTable.get(mtemp.ID).containsKey(mtemp.dest))
          gMsgTable.get(mtemp.ID).put(mtemp.dest, new Hashtable<String, Msg>());
        gMsgTable.get(mtemp.ID).get(mtemp.dest).put(mtemp2.frag_id, mtemp2);
      }*/
      vs.volume+=mtemp.size;
      Peerstable.get(tabdevList.get(mtemp2.src)).aliveMsglist.add(mtemp2);
      if(vs.msgs.put(mtemp2.ID.toString()+" "+mtemp2.custodian.toString()+" "+mtemp2.frag_id.toString(), mtemp2.ID)!=null){
        
        record = new LogRecord(Level.FINEST,"Frag Creation error "+ mtemp2.ID.toString()+" "+mtemp2.custodian.toString()+" "+mtemp2.frag_id.toString()+" "+mtemp2.m_interval.life_time.timemin+" "+mtemp2.m_interval.life_time.timemax);
        lf.log(record);
      }
      //kimtemp2.MaxHopLimit = null;
      eventQ.add_event(mtemp2);
    }    
 
 
  }

  public void saveHistory(Date init_T,String trace, String bandwidth){
    
    h = new History(Peerstable, tabdevList, null, null);
    Create_InitialEvents(init_T);
    Date dtemp = new Date(init_T.getTime()+(eventQ.getFirst().m_interval.life_time.timemin.getTime()-init_T.getTime())/2);
    h.NileHistory( dtemp, init_T, ap_count,msgtrack,bw,dFlood);  
    h.NileHistory(eventQ.getFirst().m_interval.life_time.timemin , dtemp, ap_count,msgtrack,bw,dFlood); 
    h.NileHistory(new Date(eventQ.getFirst().m_interval.life_time.timemin.getTime()+(1000*60*60*5)) , 
        eventQ.getFirst().m_interval.life_time.timemin, ap_count,msgtrack,bw,dFlood); 
    WriteMsgsDelayGraph(trace, bandwidth, init_T.getTime());
    
  }
  public void readHistory(Date init_T,String trace, String bandwidth){
    ReadMsgDelaygraph(trace, bandwidth, init_T);
  }
  
  public void MyAlgo_Initial(Date init_T, String trace, String bandwidth) {
    
    h = new History(Peerstable, tabdevList, null, null);
    dij = new Dijkstra(Peerstable, tabdevList);
    Create_InitialEvents(init_T);
    if(!dFlood)
      ReadMsgDelaygraph(trace,bandwidth,eventQ.getFirst().m_interval.life_time.timemin);
    
    Msg mtemp = eventQ.getFirst();
    try {
      Date histT = new Date(mtemp.m_interval.life_time.timemin.getTime()+600000);
     if(dFlood)
       h.NileHistory( histT, init_T, ap_count,msgtrack,bw,dFlood);     
       //h.MaxFlowHistory(histT, init_T, ap_count, bw);
     
     DataOutputStream out, out3,out2= new DataOutputStream(new FileOutputStream("boogy"));;
      if(!dFlood)  {      
        out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
          + bandwidth + "CogNileVolume"+Integer.toString(kfact)+Boolean.toString(enc)+".txt"));
        out2 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + bandwidth + "CogNilePDF"+Integer.toString(kfact)+Boolean.toString(enc)+".txt"));
        out3 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + bandwidth + "CogNilebwVolume"+Integer.toString(kfact)+Boolean.toString(enc)+".txt"));
      }
      else{
        out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + bandwidth + "CogDirectedFloodVolume.txt")); 
        out3 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + bandwidth + "CogDirectedFloodbwVolume.txt"));      
      }   
      vs.tstamp.setTime(init_T.getTime());      
      while (eventQ.size() > 0) {
        vs.vchange = false;
        vs.bwchange = false;
        mtemp = eventQ.removeFirst();
        
        if (p++ % 500 == 0)
          System.out.println(eventQ.size() + " "+ mtemp.m_interval.life_time.timemin);
        
        if(!mtemp.born)
        {         
          Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.add(mtemp);
          Peerstable.get(tabdevList.get(mtemp.src)).hbytes+=mtemp.size;
          mtemp.born=true;
          mtemp.alive=true;          
          createFrags(mtemp);
          vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
          out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n"); 
          
        }       
        if(!mtemp.alive){
           record = new LogRecord(Level.SEVERE,"Phantom Message "+ mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString()+" "+mtemp.m_interval.life_time.timemin+" "+mtemp.m_interval.life_time.timemax);
           lf.log(record);          
         }
        MyAlgo_rout(mtemp, true, histT, out2);
              
        if (vs.vchange)
          out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");
        if (vs.bwchange)
          out3.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+Long.toString(vs.consumed_bw) + "\n");
                
        if (eventQ.size() > 0){          
          if(eventQ.getFirst().m_interval.life_time.timemin.after(histT)){            
            histT.setTime(eventQ.getFirst().m_interval.life_time.timemin.getTime()+600000); 
            h.NileHistory(histT,eventQ.getFirst().m_interval.life_time.timemin, ap_count,msgtrack,bw,dFlood);            
            //h.MaxFlowHistory(histT, init_T, ap_count, bw);
            vs.tstamp.setTime(histT.getTime());
          }
        }
        if(vs.volume<0){
            System.err.println("volume error "+p);
            //System.exit(0);
        }        
      }
      if(vs.msgs.size()>0){
        Enumeration <String>vsenum = vs.msgs.keys();
        while(vsenum.hasMoreElements()){
          System.out.println(vsenum.nextElement());
        }
      }
      out.close();
     /*if(dFlood)
        WriteMsgsDelayGraph(trace,bandwidth,histT.getTime()+(0));*/
      
    } catch (Exception e) {
      System.err.println(e.getMessage()+" "+e.toString());
      System.err.println(mtemp.ID+" "+mtemp.custodian+" "+mtemp.frag_id+" "+mtemp.m_interval.life_time.timemin);
      e.printStackTrace();
    }   
  }
   public void MyAlgo_rout(Msg mtemp, boolean applyDij, Date histT, DataOutputStream out) {  
    Device dtemp = Peerstable.get(tabdevList.get(mtemp.hoplist.getLast()));    
    if(mtemp.ID.equals(msgtrack)){
      cNode = mtemp.cNode;     
    }   
    TSlot ttemp;    
    {
      int i = dtemp.find_slot(mtemp.m_interval.life_time.timemin);
      if (i < 0 | i >= dtemp.sorted_slots.size()) {
        vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
        mtemp.m_interval.life_time.timemin
            .setTime(mtemp.m_interval.life_time.timemax.getTime()+50);
       vs.volume -= mtemp.size;    
       vs. vchange = true;
       if(vs.msgs.remove(mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString())==null){
         record = new LogRecord(Level.FINEST,"Message delete 1 error "+ mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString()+" "+mtemp.m_interval.life_time.timemin+" "+mtemp.m_interval.life_time.timemax);
         lf.log(record);
       }
       mtemp.alive=false;
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
        vs.vchange = true;
        if(vs.msgs.remove(mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString())==null){
          record = new LogRecord(Level.FINEST,"Message delete 2 error "+ mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString()+" "+mtemp.m_interval.life_time.timemin+" "+mtemp.m_interval.life_time.timemax);
          lf.log(record);
        }        
        mtemp.alive=false;
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
      if(!eventQ.isFirstMsg(mtemp)){
        eventQ.add_event(mtemp);
        return;
      }    
      int[] sorted_list;
        
      if(mtemp.update |mtemp.m_interval.life_time.timemin.compareTo(mtemp.rTime)>0 ){
       // if(mtemp.desRcount>mtemp.repCount){
        if(dFlood)
          dij.MyPVDijkstra(mtemp,bw,dFlood,1);
        else
          dij.FuzzyPVDijkstra(mtemp,out);
      }       
        
      sorted_list = apg.GetMyNexthop(itime, mtemp, ap_count);
  
      for (int pcount1 = 0; pcount1 < sorted_list.length; pcount1++) {
        Device dtemp2 = Peerstable.get(tabdevList.get(sorted_list[pcount1]));
        if(dtemp.channelFail/dtemp.totalmeetcount<dtemp2.channelFail/dtemp2.totalmeetcount)
          continue;
        TSlot itime2 = new TSlot(itime.timemin.getTime(), itime.timemax
            .getTime(), "");

        int scount2 = 0;
        TSlot ttemp2;
        
        while (!dtemp2.active_slots[scount2++]);
          ttemp2 = (TSlot) dtemp2.sorted_slots.get(scount2 - 1);
        
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
              
        if(duration<2)
          continue;
        MeetingStats ms1;
       /* if (!dtemp.meetStat.containsKey(sorted_list[pcount1])) {
          ms1 = new MeetingStats( sorted_list[pcount1],tabdevList.indexOf(dtemp.mac));
          MeetingStats ms2=new MeetingStats( tabdevList.indexOf(dtemp.mac),sorted_list[pcount1]);
         /* ms1.myNeighbor.get(ms1.devindex).PVector.put(ms1.devindex, new DVectorUnit(ms1.devindex,itime2.timemin));
          ms2.myNeighbor.get(ms2.devindex).PVector.put(ms2.devindex, new DVectorUnit(ms2.devindex,itime2.timemin));*/
         /* ms1.PVector.put(ms1.devindex, new DVectorUnit(ms1.devindex,itime2.timemin));
          ms2.PVector.put(ms2.devindex, new DVectorUnit(ms2.devindex,itime2.timemin));
          dtemp.meetStat.put(ms1.devindex, ms1);
          dtemp2.meetStat.put(ms2.devindex, ms2);         
        }*/
        
         ms1 = dtemp.meetStat.get(sorted_list[pcount1]);       
        int existIndex = dtemp2.if_repmsgexist(mtemp);
        if(existIndex>-1 ) {
           mtemp2 = dtemp2.aliveMsglist.get(existIndex);
           
           if(mtemp.ID.equals(msgtrack))
             cNode.addEdge(new NodeList(dtemp2.mac,mtemp.m_interval.life_time.timemin,3));           
           
          if(!ms1.msgStat.containsKey(mtemp.ID))
             ms1.msgStat.put(mtemp.ID, new Hashtable<String, Msg>());
          if( mtemp2.delivered  & !dFlood){
            if(mtemp.ID.equals(msgtrack)){
              cNode.setStaus(new NodeList(dtemp2.mac,mtemp.m_interval.life_time.timemin,4));
              mtemp2.cNode.addEdge(new NodeList(dtemp.mac,mtemp.m_interval.life_time.timemin,4));
            }
        
            mtemp.delivered=true;
            mtemp.alive=false;
            if(mtemp.m_interval.life_time.timemin.before(mtemp.m_interval.life_time.timemax))
              mtemp.m_interval.life_time.timemin.setTime(mtemp.m_interval.life_time.timemax.getTime()+50);
            vs.volume-=mtemp.size;
            vs.vchange=true;
            if(vs.msgs.remove(mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString())==null){
              record = new LogRecord(Level.FINEST,"Message delete 3 error "+ mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString()+" "+mtemp.m_interval.life_time.timemin+" "+mtemp.m_interval.life_time.timemax);
              lf.log(record);             
            }            
            return;
          }
          continue;            
        }
        if (queuing) {
           mtemp2 = apg.getnexthopmsg(dtemp, mtemp, itime2,
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
        double bytesTransferable =bw*duration;
       
        if(ms1==null)
          System.out.println(tabdevList.indexOf(dtemp.mac));
       // ms1.myNeighbor.get(ms1.devindex).PVector.get(ms1.devindex).AddDelay(mtemp.m_interval.life_time.timemin.getTime()-mtemp.ArrivalTime.getTime(),mtemp.size,mtemp.m_interval.life_time.timemin.getTime());
        ms1.PVector.get(ms1.devindex).AddDelay(mtemp.m_interval.life_time.timemin.getTime()-mtemp.ArrivalTime.getTime(),mtemp.size,mtemp.m_interval.life_time.timemin.getTime());
        
        apg.recordUpdate( dtemp, dtemp2, mtemp, bytesTransferable,vs);
        adjust_timeslots(mtemp, dtemp, dtemp2, ttemp, ttemp2, itime2, bw);
        if (!attempt_transfer(mtemp, dtemp, dtemp2, bytesTransferable)){
          eventQ.add_event(mtemp);
         // ms.TransferHistory.get(ms.transfHistC%ms.transfHistSize).failureC++;  
          if(mtemp.ID.equals(msgtrack))
            cNode.addEdge(new NodeList(dtemp2.mac,mtemp.m_interval.life_time.timemin,2));            
        }         
        
        return;
      }        
      
      if (dtemp.sorted_slots.size() > i + 1){
        mtemp.m_interval.life_time.timemin.setTime(dtemp.sorted_slots.get(i + 1).timemin.getTime()<=mtemp.m_interval.life_time.timemin.getTime()
            ? mtemp.m_interval.life_time.timemin.getTime()+1000:dtemp.sorted_slots.get(i + 1).timemin.getTime());
      }
      else
        mtemp.m_interval.life_time.timemin.setTime(ttemp.timemax.getTime() + 500);

      eventQ.add_event(mtemp);
    }
  }  
 
  public void adjust_timeslots(Msg mtemp, Device dtemp, Device dtemp2,
      TSlot ttemp, TSlot ttemp2, TSlot itime, double bw) {

   /* ttemp.real = false;
    ttemp2.real = false*/;
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

  public boolean attempt_transfer(Msg mtemp, Device dtemp, Device dtemp2, double trans_bytes) {
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
        else{
          mtemp.Ptrans.put(dtemp2.mac, new Double(trans_bytes));
          dtemp2.aliveMsglist.add(mtemp);
        }
      }
    } else if (trans_bytes >= mtemp.size){
      return transfer(mtemp, dtemp, dtemp2);
    }
    
    return false;
  }

  public boolean transfer(Msg mtemp, Device dtemp, Device dtemp2) {
    
    if (mtemp.replicate) {
      //mtemp.tag++;
      vs.vchange = true;      
      Msg  mtemp2= new Msg(mtemp); 
      mtemp2.tag2 = mtemp.tag2;
      mtemp2.replicate = true;
      
      
      apg.adjustCustody(mtemp, mtemp2, dtemp, dtemp2);
      if(mtemp.ID.equals(msgtrack)){
        cNode.addEdge(new NodeList(dtemp2.mac,mtemp.m_interval.life_time.timemin,1));
        mtemp2.cNode= cNode.eventList.getLast();
      }
      mtemp2.ArrivalTime = new Date(mtemp2.m_interval.life_time.timemin.getTime());
        
      //Integer devindex = new Integer(tabdevList.indexOf(dtemp2.mac));     
       
      //MeetingStats ms=dtemp.meetStat.get(devindex);

      dtemp2.aliveMsglist.add(mtemp2);
      /*if(!ms.msgStat.containsKey(mtemp2.ID))
        ms.msgStat.put(mtemp2.ID, new Hashtable<String, Msg>());
      //if(!ms.msgStat.get(mtemp2.ID).containsKey(mtemp2.frag_id))
      if(ms.msgStat.get(mtemp2.ID).put(mtemp2.frag_id,mtemp)!=null){
        System.err.println("Msg stat error 2 "+mtemp2.ID+" "+mtemp2.frag_id+" "+mtemp2.m_interval.life_time.timemin);
      }*/
      //if(!mtemp.custodian.equals(mtemp.src))
       // ms.PVector.get(devindex).AddDelay(mtemp.m_interval.life_time.timemin.getTime()-mtemp.ArrivalTime.getTime(),mtemp.size,mtemp.m_interval.life_time.timemin.getTime());
      
      if (tabdevList.indexOf(dtemp2.mac) == mtemp.dest) {
        
        mtemp2.del_time.setTime(mtemp2.m_interval.life_time.timemin.getTime());
        mtemp2.delivered=true;
        mtemp.delivered=true;
        mtemp.alive=false;
        mtemp2.alive=false;
        vs.volume -= mtemp.size;
        if(vs.msgs.remove(mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString())==null){
          record = new LogRecord(Level.FINEST,"Message delete 4 error "+ mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString()+" "+mtemp.m_interval.life_time.timemin+" "+mtemp.m_interval.life_time.timemax);
          lf.log(record);
          }
        if(mtemp.size==1)
          eventQ.KillAll(mtemp);
        //schufflefrags(mtemp, dtemp);
        return true;
      }      
     // if(!dFlood){
       // mtemp.cloneHoplist.add(tabdevList.indexOf(dtemp2.mac));
        //double remTime = (mtemp.m_interval.life_time.timemax.getTime()-mtemp.m_interval.life_time.timemin.getTime())/(1000*60*60*Math.log10(mtemp.size));
       // if(mtemp.custodian.equals(mtemp.src)){
         // mtemp2.desRcount = mtemp.mynhoplist.get(tabdevList.indexOf(dtemp2.mac)).dRCount;
        //mtemp.desRcount -=mtemp2.desRcount;
          //mtemp.repCount += mtemp2.desRcount;
          //mtemp.probab = ((mtemp.desRcount-mtemp.repCount)/(double)mtemp.desRcount) * (mtemp.maxPath-mtemp.cloneHoplist.size())/(double)mtemp.maxPath;
        //}
        //else
          //mtemp.repCount++;
      
     // }
      vs.volume += mtemp2.size;
      if(vs.msgs.put(mtemp2.ID.toString()+" "+mtemp2.custodian.toString()+" "+mtemp2.frag_id.toString(), mtemp.ID)!=null){
        record = new LogRecord(Level.FINEST,"Message copy 6 error "+ mtemp2.ID.toString()+" "+mtemp2.custodian.toString()+" "+mtemp2.frag_id.toString()+" "+mtemp2.m_interval.life_time.timemin+" "+mtemp2.m_interval.life_time.timemax);
        lf.log(record);
       }
   
      mtemp2.value = Double.MAX_VALUE-mtemp.mynhoplist.get(mtemp2.hoplist.getLast()).value;
      mtemp.value = mtemp2.value;
      eventQ.add_event(mtemp2);
      eventQ.add_event(mtemp);
      return true;
    }    
    apg.adjustCustody(mtemp,null, dtemp, dtemp2);
    
    if (tabdevList.indexOf(dtemp2.mac) == mtemp.dest) {
      mtemp.del_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
      vs.volume -= mtemp.size;
      if(vs.msgs.remove(mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString())==null){
        record = new LogRecord(Level.FINEST,"Message delete 7 error "+ mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString()+" "+mtemp.m_interval.life_time.timemin+" "+mtemp.m_interval.life_time.timemax);
        lf.log(record);
        }
      return true;
    }
    dtemp2.aliveMsglist.add(mtemp);
    eventQ.add_event(mtemp);

    return true;
  }
  
  
  public void schufflefrags(Msg mtemp, Device dtemp){
    if(dtemp.ifalivemorefrags(mtemp))
      mtemp.m_interval.life_time.timemin.setTime(
          mtemp.m_interval.life_time.timemin.getTime()+(long)(Math.random()*(1000*60*60*hours)));
    
    if(enc){
      if(mtemp.codeCount<1){
        Msg mtemp3 = new Msg(mtemp);
        if(dtemp.encode(mtemp3)){
          dtemp.aliveMsglist.add(mtemp3);
          vs.volume+=mtemp3.size;
          vs.vchange=true;
          if(vs.msgs.put(mtemp3.ID.toString()+" "+mtemp3.custodian.toString()+" "+mtemp3.frag_id.toString(), mtemp3.ID)!=null){
            record = new LogRecord(Level.FINEST,"Coded message copy 6 error "+ mtemp3.ID.toString()+" "+mtemp3.custodian.toString()+" "+mtemp3.frag_id.toString()+" "+mtemp3.m_interval.life_time.timemin+" "+mtemp3.m_interval.life_time.timemax);
            lf.log(record);
          }
          eventQ.add_event(mtemp3);  
          mtemp3.born=true;
          if(gMsgTable.get(mtemp3.ID).get(mtemp3.custodian).put(mtemp3.frag_id, mtemp3)!=null)
          {
            record = new LogRecord(Level.FINEST,"Coded message copy custody error "+ mtemp3.ID.toString()+" "+mtemp3.custodian.toString()+" "+mtemp3.frag_id.toString()+" "+mtemp3.m_interval.life_time.timemin+" "+mtemp3.m_interval.life_time.timemax);
            lf.log(record);          
          }           
        }
      }  
    }
  }
  public void WriteMsgsDelayGraph(String trace, String bandwidth, long  t ){
    int devlist = Peerstable.size()-ap_count;
    Device dtemp;
    try{
      DataOutputStream out = new DataOutputStream(new FileOutputStream("resource/" + trace + "/"+bandwidth
          +"MsgDelayNetwork.txt")); // Continue to read lines while*/
    
    
      for(int i=0;i<devlist;i++){
        dtemp = Peerstable.get(tabdevList.get(i));
        out.writeBytes(Integer.toString(i));
        Enumeration<Integer>mskenum = dtemp.meetStat.keys();
        while(mskenum.hasMoreElements())
        {
          MeetingStats ms = dtemp.meetStat.get(mskenum.nextElement());
          out.writeBytes("\t"+ms.devindex+"'"+Long.toString(ms.lastmeeting.getTime())+"'"+ms.meetingcnt+";");//+"'"+ms.bAttempted+"'"+ms.bDelivered+":");
           
        //  Enumeration<Integer>nhkenum = ms.myNeighbor.keys();      
          
         // while(nhkenum.hasMoreElements()){
          //  Integer nhkey = nhkenum.nextElement();
          //  out.writeBytes(":"+nhkey.toString());
          //  Enumeration<Integer>dvkenum = ms.myNeighbor.get(nhkey).PVector.keys();      
              
           // while(dvkenum.hasMoreElements()){
              
            //  DVectorUnit dvtemp = ms.myNeighbor.get(nhkey).PVector.get(dvkenum.nextElement());
           
            
          Enumeration<Integer>dvkenum = ms.PVector.keys();
          while(dvkenum.hasMoreElements()){
            
              DVectorUnit dvtemp = ms.PVector.get(dvkenum.nextElement());
                out.writeBytes(" "+dvtemp.device+"."+dvtemp.tstamp.getTime()/*);//+"."+dvtemp.tsIndex*/+"."+dvtemp.depth/*+"."+dvtemp.bAvailable*/);
              //+"."+dvtemp.bAttempted+"."+dvtemp.bDelivered );
              /* out.writeBytes("."+Double.toString(Round(dvtemp.dAss,2))
                  +","+Double.toString(Round(dvtemp.dAtt,2))
                  +","+Double.toString(Round(dvtemp.dDel,2)) );                
            
            out.writeBytes(" ");*/
                   
              dvtemp.calcAvDTime(new Date(t),0);
              dvtemp.calcVirAvDTime(new Date(t),0);
              out.writeBytes(":"+Integer.toString(dvtemp.msgDelaysList.size())+":"+Double.toString(dvtemp.stdVDelay)+":"+Double.toString(dvtemp.stdVSize)+":");
              
              if(dvtemp.msgDelaysList.size()>0){
              
                for(int j=0;j<dvtemp.msgDelaysList.size();j++){
                  
                  out.writeBytes(Double.toString(Round(dvtemp.msgDelaysList.get(j).time,2))+","+Double.toString(Round(dvtemp.msgDelaysList.get(j).size,2))+","
                      +Long.toString(dvtemp.msgDelaysList.get(j).tstamp.getTime())+",");  
                }
              }
              else{                        
                out.writeBytes(Double.toString(Round(dvtemp.AvgDelay,2))+","+Double.toString(Round(dvtemp.AvgSize,2))+",");
              }
            
              out.writeBytes(":"+Integer.toString(dvtemp.virtmsgDelaysList.size())+":");
              if(dvtemp.virtmsgDelaysList.size()>0){
              
                for(int j=0;j<dvtemp.virtmsgDelaysList.size();j++){
                  
                  out.writeBytes(Double.toString(Round(dvtemp.virtmsgDelaysList.get(j).time,2))+","+Double.toString(Round(dvtemp.virtmsgDelaysList.get(j).size,2))+","
                      +Long.toString(dvtemp.virtmsgDelaysList.get(j).tstamp.getTime())+",");  
                }
              }
              else{                        
                out.writeBytes(Double.toString(Round(dvtemp.VirtAvgDelay,2))+","+Double.toString(Round(dvtemp.VirtAvgSize,2))+",");
                if(Round(dvtemp.VirtAvgDelay,2)==0){
                  System.err.print("Round of=0  "+dvtemp.VirtAvgDelay+" "+dvtemp.virtmsgDelaysList.size()+" "+i+" "+ms.devindex+" ");
                  System.exit(0);
                }
              }            
            }
            out.writeBytes(";");
          //}
        }
        out.writeBytes("\n");
      }
      out.close();
      System.out.println("Msg delay file Writing done");
    }
    catch (Exception e) {
    System.err.println(e.getMessage()+" "+e.toString());
    e.printStackTrace();
    }
  }
  
  public void ReadMsgDelaygraph(String trace, String bandwidth, Date tstamp ){
    String line ="";
    int i=0;
    double factor=10;
    BufferedReader in;
    try{
      ///*if(trace.equals("IBM")){
      /*in = new BufferedReader(new FileReader("resource/" + trace + "/low"//+/* "/"+bandwidth
          + "MsgDelayNetwork.txt")); // Continue to read lines while*/
      //if(bandwidth.equals("High"))
        //factor=10;
    //}
   // else*/
      /*in = new BufferedReader(new FileReader("resource/" + trace + "/2High"//+/* "/"+bandwidth
          + "MsgDelayNetwork.txt")); // Continue to read lines while*/
      in = new BufferedReader(new FileReader("resource/" + trace + /*"/low"//+*/ "/"+(bandwidth.endsWith("Que") ? 
          bandwidth.substring(0,bandwidth.length()-3):bandwidth)
          + "MsgDelayNetwork.txt")); // Continue to read lines while*/
      
    while(in.ready()){
        line = in.readLine();
        String []frags1=line.split("\t");
        int dindex=Integer.parseInt(frags1[0]);
        Device dtemp = Peerstable.get(tabdevList.get(dindex));
        for( i=1;i<frags1.length;i++){
          int marker = frags1[i].indexOf("'");
          int todindex = Integer.parseInt(frags1[i].substring(0,marker));

          MeetingStats ms = new MeetingStats( todindex,dindex);
          
          ms.lastmeeting = new Date(Long.parseLong(frags1[i].substring(marker+1, marker=frags1[i].indexOf("'",++marker)).trim()));
        //to avoid getting -ve virtual time as dflood run last till 3rd week
          ms.meetingcnt=Integer.parseInt(frags1[i].substring(marker+1, marker=frags1[i].indexOf(";",++marker)).trim());
         /* ms.bAttempted=Long.parseLong(frags1[i].substring(marker+1, marker=frags1[i].indexOf("'",++marker)).trim());
          ms.bDelivered=Long.parseLong(frags1[i].substring(marker+1, marker=frags1[i].indexOf(":",++marker)).trim());
          */
          dtemp.meetStat.put(todindex,ms);
          String []queues = frags1[i].split(";");
          for(int l=1;l<queues.length;l++){
            marker = queues[l].indexOf(" ");
            //int nhkey = Integer.parseInt(queues[l].substring(queues[l].indexOf(":")+1, marker).trim());
           // ms.myNeighbor.put(nhkey, new NeighborQ());
            while(marker+1<queues[l].length()){
              Integer dev = Integer.parseInt(queues[l].substring(marker+1, marker=queues[l].indexOf(".",marker)).trim());
              DVectorUnit dvtemp = new DVectorUnit(dev, new Date(Long.parseLong(queues[l].substring(marker+1, marker=queues[l].indexOf(".",marker+1)).trim())));
              dvtemp.depth = Integer.parseInt(queues[l].substring(marker+1, marker=queues[l].indexOf(":",marker+1)).trim());
           
              
              /* dvtemp.tsIndex = Integer.parseInt(frags1[i].substring(marker+1, marker=queues[l].indexOf(".",marker+1)).trim());
            dvtemp.depth = Integer.parseInt(frags1[i].substring(marker+1, marker=frags1[i].indexOf(".",marker+1)).trim());
            dvtemp.bAvailable = Long.parseLong(frags1[i].substring(marker+1, marker=frags1[i].indexOf(".",marker+1)).trim());
            dvtemp.dAss = Double.parseDouble(frags1[i].substring(marker+1, marker=frags1[i].indexOf(",",marker+1)).trim());
            dvtemp.dAtt = Double.parseDouble(frags1[i].substring(marker+1, marker=frags1[i].indexOf(",",marker+1)).trim());
            dvtemp.dDel = Double.parseDouble(frags1[i].substring(marker+1, marker=frags1[i].indexOf(" ",marker+1)).trim());  
            dvtemp.dAss/=factor;
            dvtemp.dDel/=factor;
            dvtemp.dAtt/=factor;
            if(dvtemp.depth==1){
              dvtemp.bAssigned = (long) (dvtemp.bAvailable*dvtemp.dAss);
              dvtemp.bDelivered = (long) (dvtemp.bAvailable*dvtemp.dDel);
              dvtemp.bAttempted = (long) (dvtemp.bAvailable*dvtemp.dAtt);
            }
            
            
            ms.PVector.put(dev,dvtemp);
            String []dvTimes=frags1[i].substring(marker+1,marker=frags1[i].indexOf(":",marker+1)).split(",");
            for(int k=0;k<dvtemp.histLen;k++)
              dvtemp.AddDelay(Double.parseDouble(dvTimes[k]));*/  
              
              //ms.myNeighbor.get(nhkey).PVector.put(dvtemp.device, dvtemp);
              ms.PVector.put(dvtemp.device, dvtemp);
            
              int tDelayLen=Integer.parseInt(queues[l].substring(marker+1, marker=queues[l].indexOf(":",++marker)).trim());
              dvtemp.stdVDelay = Double.parseDouble(queues[l].substring(marker+1, marker=queues[l].indexOf(":",++marker)).trim());
              dvtemp.stdVSize = Double.parseDouble(queues[l].substring(marker+1, marker=queues[l].indexOf(":",++marker)).trim());
              
              if(tDelayLen>0){
                for(int k=0;k<tDelayLen;k++){
                  TDelayFactor tdftemp = new TDelayFactor(Double.parseDouble(queues[l].substring(marker+1, marker=queues[l].indexOf(",",marker+1)).trim()),
                      Double.parseDouble(queues[l].substring(marker+1, marker=queues[l].indexOf(",",marker+1)).trim()), Long.parseLong(queues[l].substring(marker+1, marker=queues[l].indexOf(",",marker+1)).trim()));
                  dvtemp.msgDelaysList.add(tdftemp);
                }
                dvtemp.calcAvDTime(tstamp,1);
              }
              else{
                
                dvtemp.AvgDelay = Double.parseDouble(queues[l].substring(marker+1, marker=queues[l].indexOf(",",marker+1)).trim());
                dvtemp.AvgSize = Double.parseDouble(queues[l].substring(marker+1, marker=queues[l].indexOf(",",marker+1)).trim());
                if(dvtemp.AvgDelay==0){
                  dvtemp.AvgDelay = 7*24*60.0;
                  dvtemp.AvgSize = 10.0;
                }
              }
              marker++;
              tDelayLen=Integer.parseInt(queues[l].substring(marker+1, marker=queues[l].indexOf(":",++marker)).trim());
              
              if(tDelayLen>0){
                for(int k=0;k<tDelayLen;k++){
                  TDelayFactor tdftemp = new TDelayFactor(Double.parseDouble(queues[l].substring(marker+1, marker=queues[l].indexOf(",",marker+1)).trim()),
                      Double.parseDouble(queues[l].substring(marker+1, marker=queues[l].indexOf(",",marker+1)).trim()), Long.parseLong(queues[l].substring(marker+1, marker=queues[l].indexOf(",",marker+1)).trim()));
                  dvtemp.virtmsgDelaysList.add(tdftemp);
                }
                dvtemp.calcVirAvDTime(tstamp,1);
              }
              else{
                
                dvtemp.VirtAvgDelay = Double.parseDouble(queues[l].substring(marker+1, marker=queues[l].indexOf(",",marker+1)).trim());
                dvtemp.VirtAvgSize = Double.parseDouble(queues[l].substring(marker+1, marker=queues[l].indexOf(",",marker+1)).trim());
                if(dvtemp.VirtAvgSize<dvtemp.AvgSize){
                  //dvtemp.VirtAvgDelay = 7*24*60;
                  dvtemp.VirtAvgSize = dvtemp.AvgSize+1;
                }
              }
              //marker=queues[l].indexOf(";",marker+1);
            } 
          }      
        }
      }
      in.close();
      System.out.println(bandwidth+" Msg delay file reading done");
    }
    
    catch (Exception e) {
      System.err.println(i+" "+e.getMessage()+" "+e.toString());
      e.printStackTrace();
    }
  }
  public static double Round(double Rval, int Rpl) {
    double p = (double)Math.pow(10,Rpl);
    Rval = Rval * p;
    double tmp = Math.round(Rval);
    return (double)tmp/p;
      }
  /*public void writemergednetwork(String trace){
  int devlist = Peerstable.size()-ap_count;
  Device dtemp;
  try{
    DataOutputStream out = new DataOutputStream(new FileOutputStream("resource/" + trace + "/"
        + "MergedNetwork.txt")); // Continue to read lines while
  
    for(int i=0;i<devlist;i++){
      dtemp = Peerstable.get(tabdevList.get(i));
      out.writeBytes(Integer.toString(i));
      Enumeration<Integer>mskenum = dtemp.meetStat.keys();
      while(mskenum.hasMoreElements())
      {
        MeetingStats ms = dtemp.meetStat.get(mskenum.nextElement());
        Enumeration<Integer>bvkenum = ms.mbf.hbloom.keys();
        out.writeBytes("\t"+ms.devindex+":");
        while(bvkenum.hasMoreElements()){
          Integer bvkey = bvkenum.nextElement();
          BloomValue bvtemp = ms.mbf.hget(bvkey);
          out.writeBytes(bvkey.toString()+","+bvtemp.depth.toString()+","+bvtemp.proximity.toString()+";");
        }
      }
      out.writeBytes("\n");
    }
    out.close();
  }
  catch (Exception e) {
  System.err.println(e.getMessage()+" "+e.toString());
  e.printStackTrace();
  }
}
public void readmergednetwork(String trace){
  String line ="";
  int i=0;
  try{
    BufferedReader in = new BufferedReader(new FileReader("resource/" + trace + "/"
        + "MergedNetwork.txt")); // Continue to read lines while
    while(in.ready()){
       line = in.readLine();
      String []frags1=line.split("\t");
      int dindex=Integer.parseInt(frags1[0]);
      Device dtemp = Peerstable.get(tabdevList.get(dindex));
      for( i=1;i<frags1.length;i++){
        int colonindex = frags1[i].indexOf(":");
        int todindex = Integer.parseInt(frags1[i].substring(0,colonindex));
        MeetingStats ms = new MeetingStats(bflen,hfun, dindex,todindex,dtemp.sorted_slots.getLast().timemax);
        dtemp.meetStat.put(todindex,ms);
        
       
        while(colonindex+1<frags1[i].length()){
          String []bvfrag=frags1[i].substring(colonindex+1,colonindex=frags1[i].indexOf(";",colonindex+1)).split(",");
          //ms.mbf.hput(Integer.parseInt(bvfrag[0]), new BloomValue(Integer.parseInt(bvfrag[1]),Double.parseDouble(bvfrag[2])));
        
        }      
      }
    }
  }
  catch (Exception e) {
  System.err.println(i+" "+e.getMessage()+" "+e.toString());
  e.printStackTrace();
  }
}*/
    
}
