
package de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;


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


public class Karez {

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

  //int bflen;

  double bw;

  //public Hash hfun;

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
  
  //public LinkedList<NodeList> eventList;
  
  //LinkedList<NodeList>tEList;
  public NodeList cNode ;

  public boolean dFlood;
  public Karez(Hashtable<String, Device> p, LinkedList<String> td,
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
      record = new LogRecord(Level.INFO, "Starting Dflood encoding="+Boolean.toString(enc)+ " Fragament= "+Integer.toString(kfact));
    else
      record = new LogRecord(Level.INFO, "Starting Nile encoding="+Boolean.toString(enc)+ " Fragament= "+Integer.toString(kfact));
      
    lf.log(record);
    //Peerstable.put("debug", new Device("debug"));
    
  }

  public void Create_InitialEvents(Date init_T) {

    int devlistsize = Peerstable.size() - ap_count;
    Device dtemp;
    for (int i = 0; i < devlistsize; i++) {
      dtemp = Peerstable.get(tabdevList.get(i));
      dtemp.meetStat = new Hashtable<Integer, MeetingStats>();
     // dtemp.probab = new double[devlistsize];
      /*for (int j = 0; j < devlistsize; j++)
        dtemp.probab[j] = 1 / ((double) devlistsize );*/
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
  public void MyAlgo_DJPaths(String trace){
    h = new History(Peerstable, tabdevList, null, null);
    dij = new Dijkstra(Peerstable, tabdevList);
    int devlist = tabdevList.size()-ap_count;
    for(int i=0;i<devlist;i++){
      Peerstable.get(tabdevList.get(i)).meetStat = new Hashtable<Integer, MeetingStats>();
    }
   h.construct_graphdjpaths(devlist);
    //readmergednetwork(trace);
    try {
      DataOutputStream out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
          + "BloomDJPaths.txt")); // Continue to read lines while
       
      Msg mtemp;
      int i=0;
      
      Enumeration<Integer> mkeys =gMsgTable.keys();
      while(mkeys.hasMoreElements()){
        Integer mkey =mkeys.nextElement();
        Enumeration<Integer> ckeys =gMsgTable.get(mkey).keys();
        
        while(ckeys.hasMoreElements()){
          Integer ckey = ckeys.nextElement();
          Enumeration<String> fkeys =gMsgTable.get(mkey).get(ckey).keys();
          while(fkeys.hasMoreElements()){
          
            mtemp=gMsgTable.get(mkey).get(ckey).get(fkeys.nextElement());
            //mtemp.hindex = hfun.hash(new Integer(mtemp.dest));
       
            if(i++%20==0)
              System.out.println(i);
            // h.fillinmycompressed(hfun, mtemp.src, mtemp.dest,devlist);
           // int dpaths=dij.MyBloomPVDijkstra(mtemp, hfun, bflen);
            out.writeBytes(mtemp.ID.toString()+ "\t"+mtemp.mynhoplist.size()+"\t"+Peerstable.get(tabdevList.get(mtemp.src)).meetStat.size()+"\n");
          }
        }
      }
      out.close();
    }
   catch (Exception e) {
    System.err.println(e.getMessage()+" "+e.toString());
    e.printStackTrace();
  }
  //writemergednetwork(trace);
  
   System.out.println("Bloomfilter DJ Path writing complete");
  }
  
  public void MyAlgo_Initial(Date init_T, String trace, String bandwidth) {

    
    h = new History(Peerstable, tabdevList, null, null);
    dij = new Dijkstra(Peerstable, tabdevList);
    Create_InitialEvents(init_T);
    if(!dFlood)
      ReadLinkStategraph(trace,bandwidth,eventQ.getFirst().m_interval.life_time.timemin);
    Msg mtemp = eventQ.getFirst();
    try {
      Date histT = new Date(mtemp.m_interval.life_time.timemin.getTime()+600000);
     if(dFlood)
       h.NileHistory( histT, init_T, ap_count,msgtrack,bw,dFlood);     
     
     DataOutputStream out, out2= new DataOutputStream(new FileOutputStream("boogy"));;
      if(!dFlood)  {      
        out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
          + bandwidth + "NileVolume"+Integer.toString(kfact)+Boolean.toString(enc)+".txt"));
        out2 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + bandwidth + "NilePDF"+Integer.toString(kfact)+Boolean.toString(enc)+".txt"));
      }
      else
        out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + bandwidth + "DirectedFloodVolume.txt")); // Continue to read lines while
 
    
      vs.tstamp.setTime(init_T.getTime());      
      while (eventQ.size() > 0) {
        vs.vchange = false;
        
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
              
        if (vs.vchange){
          vs.vchange=false;
          out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");
        }        
        if (eventQ.size() > 0){          
          if(eventQ.getFirst().m_interval.life_time.timemin.after(histT)){            
            h.NileHistory(histT,eventQ.getFirst().m_interval.life_time.timemin, ap_count,msgtrack,bw,dFlood);
            histT.setTime(eventQ.getFirst().m_interval.life_time.timemin.getTime()+600000);  
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
     if(dFlood)
        WriteLinkStateGraph(trace,bandwidth,histT.getTime()+(0));
      
    } catch (Exception e) {
      System.err.println(e.getMessage()+" "+e.toString());
      System.err.println(mtemp.ID+" "+mtemp.custodian+" "+mtemp.frag_id+" "+mtemp.m_interval.life_time.timemin);
      e.printStackTrace();
    }   
  }
  
  public void MyAlgo_Initialdummy(Date init_T, String trace, String bw) {

    h = new History(Peerstable, tabdevList, null, null);
    dij = new Dijkstra(Peerstable, tabdevList);

    try {
      Create_InitialEvents(init_T);
      int i = 0;
      h.construct_graphbloom(ap_count);  

      while (eventQ.size() > 0) {
        
        if (i++ % 500 == 0)
          System.out.println(eventQ.size() + " "
              + eventQ.getFirst().m_interval.life_time.timemin);
        MyAlgo_rout(eventQ.removeFirst(), true, null,null);

        
      }
    } catch (Exception e) {
      System.err.println(e.getMessage()+" "+e.toString());
      e.printStackTrace();
    }
    System.out.println(Peerstable.get("debug").aliveMsglist.size());

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
    
      /*if(dFlood)
        dij.MyPVDijkstra(mtemp,bw,dFlood,1);
      //else
      else if(mtemp.update |mtemp.m_interval.life_time.timemin.compareTo(mtemp.rTime)>0 ){
       // if(mtemp.desRcount>mtemp.repCount){
         // dij.FuzzyPVDijkstra(mtemp,out);
        dij.LinkStateDijkstra(mtemp,ap_count);          
      } */
      
      dij.LinkStateDijkstra(mtemp, ap_count, dFlood);
        
      sorted_list = apg.GetMyNexthop(itime, mtemp, ap_count);
  
      for (int pcount1 = 0; pcount1 < sorted_list.length; pcount1++) {
        Device dtemp2 = Peerstable.get(tabdevList.get(sorted_list[pcount1]));
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
        MeetingStats ms1;
        if (!dtemp.meetStat.containsKey(sorted_list[pcount1])) {
          ms1 = new MeetingStats( sorted_list[pcount1],tabdevList.indexOf(dtemp.mac));
          MeetingStats ms2=new MeetingStats( tabdevList.indexOf(dtemp.mac),sorted_list[pcount1]);
          ms1.myNeighbor.get(ms1.devindex).PVector.put(ms1.devindex, new DVectorUnit(ms1.devindex,itime2.timemin.getTime()));
          ms2.myNeighbor.get(ms2.devindex).PVector.put(ms2.devindex, new DVectorUnit(ms2.devindex,itime2.timemin.getTime()));
          dtemp.meetStat.put(ms1.devindex, ms1);
          dtemp2.meetStat.put(ms2.devindex, ms2);          
          
        }
         ms1 = dtemp.meetStat.get(sorted_list[pcount1]);
              
        double duration = ((double) itime2.timemax.getTime() - itime2.timemin
            .getTime()) / 1000;
        Msg mtemp2;
              
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
        double bytesTransferable =bw*duration;
        ms1.bAttempted+=mtemp.size;
        ms1.bAvailable+=bytesTransferable;
        ms1.bDelivered+= bytesTransferable>mtemp.size ? mtemp.size:bytesTransferable;
        ms1.msgsAttempt++;
        
        //ms1.myNeighbor.get(ms1.devindex).PVector.get(ms1.devindex).AddDelay(mtemp.m_interval.life_time.timemin.getTime()-mtemp.ArrivalTime.getTime(),mtemp.size,mtemp.m_interval.life_time.timemin.getTime());
        ms1.PVector.get(ms1.devindex).AddDelay(mtemp.m_interval.life_time.timemin.getTime()-mtemp.ArrivalTime.getTime(),mtemp.size,mtemp.m_interval.life_time.timemin.getTime());
        
        apg.recordUpdate( dtemp, dtemp2, mtemp, bytesTransferable,vs);
        adjust_timeslots(mtemp, dtemp, dtemp2, ttemp, ttemp2, itime2, bw);
        if (!attempt_transfer(mtemp, dtemp, dtemp2, bytesTransferable)){
          eventQ.add_event(mtemp);
         // ms.TransferHistory.get(ms.transfHistC%ms.transfHistSize).failureC++;  
          if(mtemp.ID.equals(msgtrack))
            cNode.addEdge(new NodeList(dtemp2.mac,mtemp.m_interval.life_time.timemin,2));
            
        }
        else{
          
          ms1.msgsTransf++;
        }
        return;
      }        
      
      if (dtemp.sorted_slots.size() > i + 1){
        mtemp.m_interval.life_time.timemin.setTime(dtemp.sorted_slots.get(i + 1).timemin.getTime()<=mtemp.m_interval.life_time.timemin.getTime()
            ? mtemp.m_interval.life_time.timemin.getTime()+1000:dtemp.sorted_slots.get(i + 1).timemin.getTime());
      }
      else
        mtemp.m_interval.life_time.timemin
            .setTime(ttemp.timemax.getTime() + 500);

      eventQ.add_event(mtemp);
    }
  }  
 
  public void adjust_timeslots(Msg mtemp, Device dtemp, Device dtemp2,
      TSlot ttemp, TSlot ttemp2, TSlot itime, double bw) {

    /*ttemp.real = false;
    ttemp2.real = false;*/
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
     if (resume) {     
       mtemp.attemptT++;

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
      mtemp.tag++;
      vs.vchange = true;      
      Msg  mtemp2= new Msg(mtemp); 
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
  
  public void WriteLinkStateGraph(String trace, String bandwidth, long  t ){
    int devlist = Peerstable.size()-ap_count;
    Device dtemp;
    System.out.println("writing started");
    try{
      DataOutputStream out = new DataOutputStream(new FileOutputStream("resource/" + trace + "/"+bandwidth
          +"MsgDelayNetwork.txt")); // Continue to read lines while
    
      for(int i=0;i<devlist;i++){
        dtemp = Peerstable.get(tabdevList.get(i));
        out.writeBytes(Integer.toString(i));
        Enumeration<Integer>mskenum = dtemp.meetStat.keys();
        while(mskenum.hasMoreElements())
        {
          MeetingStats ms = dtemp.meetStat.get(mskenum.nextElement());
                   
          out.writeBytes("\t"+ms.devindex+"'"+ms.meetingcnt);//+"'"+ms.bAttempted+"'"+ms.bDelivered+":");
          
          //DVectorUnit dvtemp = ms.myNeighbor.get(ms.devindex).PVector.get(ms.devindex);
          DVectorUnit dvtemp = ms.PVector.get(ms.devindex);
          
          //out.writeBytes(" "+dvtemp.device+"."+dvtemp.tstamp.getTime());//+"."+dvtemp.tsIndex+"."+dvtemp.depth+"."+dvtemp.bAvailable);
             
          dvtemp.calcAvDTime(new Date(t),0);
          dvtemp.calcVirAvDTime(new Date(t),0);
          out.writeBytes(":"+Integer.toString(Math.max(dvtemp.msgDelaysList.size(),1))+":");
          if(dvtemp.msgDelaysList.size()>0){
            
            for(int j=0;j<dvtemp.msgDelaysList.size();j++){                
              out.writeBytes(Double.toString(Round(dvtemp.msgDelaysList.get(j).time,2))+","+Double.toString(Round(dvtemp.msgDelaysList.get(j).size,2))+","
                  +Long.toString(dvtemp.msgDelaysList.get(j).tstamp.getTime())+",");  
            }
          }
          else{                        
            out.writeBytes(Double.toString(Round(dvtemp.AvgDelay,2))+","+Double.toString(Round(dvtemp.AvgSize,2))+","+Long.toString(t)+",");
          }
          
          out.writeBytes(":"+Integer.toString(Math.max(dvtemp.virtmsgDelaysList.size(),1))+":");
          if(dvtemp.virtmsgDelaysList.size()>0){
            
            for(int j=0;j<dvtemp.virtmsgDelaysList.size();j++){
                
              out.writeBytes(Double.toString(Round(dvtemp.virtmsgDelaysList.get(j).time,2))+","+Double.toString(Round(dvtemp.virtmsgDelaysList.get(j).size,2))+","
                  +Long.toString(dvtemp.virtmsgDelaysList.get(j).tstamp.getTime())+",");  
            }
          }
          else{                        
            //out.writeBytes(Double.toString(Round(dvtemp.VirtAvgDelay,2))+","+Double.toString(Round(dvtemp.VirtAvgSize,2))+",");
            out.writeBytes(Double.toString(Global.weekMin*1.0)+","+Double.toString(Global.weekMin*1.0)+","+Long.toString(t)+",");
            if(Round(dvtemp.VirtAvgDelay,2)==0){
              System.err.print(dvtemp.virtmsgDelaysList.size()+" "+dvtemp.VirtAvgSize+" "+dvtemp.VirtAvgDelay);
              //System.exit(0);
            }
          }            
         
        out.writeBytes(";");
        
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
  public void ReadLinkStategraph(String trace, String bandwidth, Date tstamp ){
    String line ="";
    int i=0;
   
    BufferedReader in;
    try{
      in = new BufferedReader(new FileReader("resource/" + trace + /*"/low"//+*/ "/"+bandwidth
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
          
         // ms.lastmeeting = new Date(Long.parseLong(frags1[i].substring(marker+1, marker=frags1[i].indexOf("'",++marker)).trim()));
        //to avoid getting -ve virtual time as dflood run last till 3rd week
          ms.meetingcnt=Integer.parseInt(frags1[i].substring(marker+1, marker=frags1[i].indexOf(":",++marker)).trim());
          dtemp.meetStat.put(todindex,ms);
          ms.myNeighbor.put(ms.devindex, new NeighborQ());
           
          DVectorUnit dvtemp = new DVectorUnit(ms.devindex, 0);
          
          ms.myNeighbor.get(ms.devindex).PVector.put(dvtemp.device, dvtemp);
            
          int tDelayLen=Integer.parseInt(frags1[i].substring(marker+1, marker=frags1[i].indexOf(":",++marker)).trim());
            
         // if(tDelayLen>0){
            for(int k=0;k<tDelayLen;k++){
              TDelayFactor tdftemp = new TDelayFactor(Double.parseDouble(frags1[i].substring(marker+1, marker=frags1[i].indexOf(",",marker+1)).trim()),
                  Double.parseDouble(frags1[i].substring(marker+1, marker=frags1[i].indexOf(",",marker+1)).trim()), Long.parseLong(frags1[i].substring(marker+1, marker=frags1[i].indexOf(",",marker+1)).trim()));
                dvtemp.msgDelaysList.add(tdftemp);
            }
            dvtemp.calcAvDTime(tstamp,1);
          //}
          /*else{
                
           dvtemp.AvgDelay = Double.parseDouble(frags1[i].substring(marker+1, marker=frags1[i].indexOf(",",marker+1)).trim());
           dvtemp.AvgSize = Double.parseDouble(frags1[i].substring(marker+1, marker=frags1[i].indexOf(",",marker+1)).trim());
           if(dvtemp.AvgDelay==0){
             dvtemp.AvgDelay = 7*24*60*1.0;
             dvtemp.AvgSize = 1.00;
           }
              }*/
          marker++;
          tDelayLen=Integer.parseInt(frags1[i].substring(marker+1, marker=frags1[i].indexOf(":",++marker)).trim());
              
          //if(tDelayLen>0){
            for(int k=0;k<tDelayLen;k++){
              TDelayFactor tdftemp = new TDelayFactor(Double.parseDouble(frags1[i].substring(marker+1, marker=frags1[i].indexOf(",",marker+1)).trim()),
                 Double.parseDouble(frags1[i].substring(marker+1, marker=frags1[i].indexOf(",",marker+1)).trim()), Long.parseLong(frags1[i].substring(marker+1, marker=frags1[i].indexOf(",",marker+1)).trim()));
              dvtemp.virtmsgDelaysList.add(tdftemp);
            }
            dvtemp.calcVirAvDTime(tstamp,1);
         // }
          /*else{
                
            dvtemp.VirtAvgDelay = Double.parseDouble(frags1[i].substring(marker+1, marker=frags1[i].indexOf(",",marker+1)).trim());
            dvtemp.VirtAvgSize = Double.parseDouble(frags1[i].substring(marker+1, marker=frags1[i].indexOf(",",marker+1)).trim());
            if(dvtemp.VirtAvgSize<dvtemp.AvgSize){
              //dvtemp.VirtAvgDelay = 7*24*60;
              dvtemp.VirtAvgSize = dvtemp.AvgSize+1;
            }
          } */            
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
  
    
}
