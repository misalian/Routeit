
package de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.LogRecord;

//import de.unikn.inf.disy.nuntifix.dtn.simulator.bloomfilter.*;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.*;
import de.unikn.inf.disy.util.logging.LoggingFacility;


public class NileAdaptive {

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
 
  
  
  //public double frag_factor;
  int p = 0;

   
  LoggingFacility lf ;
  
  LogRecord record ;
  
 
 
  public int hours;
  
  
  
  public int msgtrack=1; 
  
  //public LinkedList<NodeList> eventList;
  
  //LinkedList<NodeList>tEList;
  public NodeList cNode ;

 
  public NileAdaptive(Hashtable<String, Device> p, LinkedList<String> td,
      Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> 
      gm, Hashtable<String, Apoint> gapTab,boolean enc,int m) {

    Peerstable = p;
    tabdevList = td;
    gMsgTable = gm;
    apg = new Active_PeerGenerator(p, td, gm);
    

   // Continue to read lines while
    apg.set_aptable(gapTab);

    
     vs = new VolumeStats();
     lf = LoggingFacility.getInstance(null);
    
     
    
    hours=1;
     
     
  }

  public void setParameters(int apc, double bw, boolean res, boolean que, int msgtrack, boolean Df) {

    discovery_t = 0;
    resume = res;
    queuing = que;
    this.bw = bw;
    ap_count = apc;
    this.msgtrack= msgtrack;
    
    
      record = new LogRecord(Level.INFO, "Starting Nileplus encoding");
      
    lf.log(record);
    //Peerstable.put("debug", new Device("debug"));
    
  }

  public void Create_InitialEvents(Date init_T) {

    int devlistsize = Peerstable.size() - ap_count;
    Device dtemp;
    for (int i = 0; i < devlistsize; i++) {
      dtemp = Peerstable.get(tabdevList.get(i));
      dtemp.meetStat = new Hashtable<Integer, MeetingStats>();
    
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
  public void createFrags(Msg mtemp, double deficit, int pCount){
  
    
   double frag_factor;
   mtemp.frag_count= (int)(Round(Math.log(mtemp.size),2)*(Math.pow(deficit,2)/pCount))+1;  
       
   mtemp.fragmented = true;
   mtemp.replicate = true;
    
   frag_factor = mtemp.size/mtemp.frag_count;
   mtemp.size=frag_factor;
       
    Msg mtemp2;
   
    for(int i=1;i<mtemp.frag_count;i++){
      mtemp2 = new Msg(mtemp);
      mtemp2.born=true;
      mtemp2.replicate = true;
      mtemp2.fragmented = true;
      mtemp2.frag_id = new String(Integer.toString(i));
      mtemp2.cFrags[0]=mtemp2.frag_id;
      mtemp2.size = frag_factor;
      gMsgTable.get(mtemp2.ID).get(mtemp2.custodian).put(mtemp2.frag_id, mtemp2);
 
      Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.add(mtemp2);
      if(vs.msgs.put(mtemp2.ID.toString()+" "+mtemp2.custodian.toString()+" "+mtemp2.frag_id.toString(), mtemp2.ID)!=null){
        
        record = new LogRecord(Level.FINEST,"Frag Creation error "+ mtemp2.ID.toString()+" "+mtemp2.custodian.toString()+" "+mtemp2.frag_id.toString()+" "+mtemp2.m_interval.life_time.timemin+" "+mtemp2.m_interval.life_time.timemax);
        lf.log(record);
      }
     
      eventQ.add_event(mtemp2);
    }   
  }
  
  
  public void MyAlgo_Initial(Date init_T, String trace, String bandwidth) {

    
    h = new History(Peerstable, tabdevList, null, null);
    dij = new Dijkstra(Peerstable, tabdevList);
    Create_InitialEvents(init_T);
   
    ReadMsgDelaygraph(trace,bandwidth);
    Msg mtemp = eventQ.getFirst();
    try {    
      
      Date histT = new Date();
      
      histT.setTime(mtemp.m_interval.life_time.timemin.getTime());
      DataOutputStream out;
            
      out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
          + bandwidth + "NileplusVolume.txt")); // Continue to read lines while
          
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
          if(mtemp.ID.equals(msgtrack)){
            if(mtemp.cNode==null){
              mtemp.cNode =  new NodeList(tabdevList.get(mtemp.src),mtemp.m_interval.life_time.timemin,-1);
              cNode=mtemp.cNode;
            }
          }
         
          vs.volume+=mtemp.size;
          if(vs.msgs.put(mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString(), mtemp.ID)!=null){
            record = new LogRecord(Level.FINEST,"Message born error "+ mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString()+" "+mtemp.m_interval.life_time.timemin+" "+mtemp.m_interval.life_time.timemax);
            lf.log(record);
          }
                  
          //createFrags(mtemp);
          vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
          out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n"); 
          
        }       
       
        MyAlgo_rout(mtemp, true,histT);
     
              
        if (vs.vchange){
          vs.vchange=false;
          out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");
        }        
        if (eventQ.size() > 0){
          
          if(eventQ.getFirst().m_interval.life_time.timemin.getTime()-histT.getTime()>(1000*60*10)){
            
            h.NileHistory(eventQ.getFirst().m_interval.life_time.timemin,   histT, ap_count,msgtrack,bw,false);
            histT.setTime(eventQ.getFirst().m_interval.life_time.timemin.getTime());  
            vs.tstamp.setTime(histT.getTime());
          }
        }
        if(vs.volume<0){
            System.err.println("volume error "+vs.volume+" "+p);
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
   
      
    }catch (Exception e) {
      System.err.println(e.getMessage()+" "+e.toString());
      System.err.println(mtemp.ID+" "+mtemp.custodian+" "+mtemp.frag_id+" "+mtemp.m_interval.life_time.timemin);
      e.printStackTrace();
    }    
  }
  
 
  public void MyAlgo_rout(Msg mtemp, boolean applyDij, Date histT) {  
  
    Device dtemp = Peerstable.get(tabdevList.get(mtemp.hoplist.getLast()));
    
    if(mtemp.ID.equals(msgtrack)){
      cNode = mtemp.cNode;;
      
    }
    if(!mtemp.born){
     
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
      int[] sorted_list;
      if(mtemp.update |mtemp.m_interval.life_time.timemin.compareTo(mtemp.rTime)>0){
        ArrayList<Integer>vDevices = new ArrayList<Integer>(); 
        Integer maxDepth = new Integer(0);
        LinkedList<PossibleHop> PVectorProxList =   new LinkedList<PossibleHop>();;
        
        double def =dij.Nileplusfilter(mtemp,PVectorProxList, vDevices, maxDepth)+1;
        if(!mtemp.fragmented){         
          
            createFrags(mtemp,def,PVectorProxList.size()+1);
             
        }
        dij.NileplusDijkstra(PVectorProxList, vDevices, dtemp, mtemp, maxDepth);
        apg.duplicatePath(dtemp,mtemp,histT);
       }
        
      
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
        /*
         * } else { if(itime2.timemin.after(limtT) &(eventQ.size()>0))
         * if(itime2.timemin.after(gMsgTable.get(eventQ.getFirst()).m_interval.life_time.timemin)) {
         * eventQ.add_event(mtemp.ID); return; } }
         */
        if (!dtemp.meetStat.containsKey(sorted_list[pcount1])) {
          MeetingStats ms1 = new MeetingStats( tabdevList.indexOf(dtemp2.mac),tabdevList.indexOf(dtemp.mac),bw);
          MeetingStats ms2=new MeetingStats(tabdevList.indexOf(dtemp.mac),tabdevList.indexOf(dtemp2.mac),bw);
          dtemp.meetStat.put(tabdevList.indexOf(dtemp2.mac), ms1);
          dtemp2.meetStat.put(tabdevList.indexOf(dtemp.mac), ms2);          
        }
         MeetingStats ms = dtemp.meetStat.get(sorted_list[pcount1]);
         //ms.transfHistC++;
         
         //ms.TransferHistory.get(ms.transfHistC%ms.transfHistSize).attempC++;
         //ms.TransferHistory.get(ms.transfHistC%ms.transfHistSize).tstamp.setTime(itime2.timemin.getTime());
          
        
        double duration = ((double) itime2.timemax.getTime() - itime2.timemin
            .getTime()) / 1000;
        Msg mtemp2;
        if (queuing) {
          mtemp2 = apg
              .getnexthopmsg(dtemp, mtemp, itime2, dtemp2);
          if (mtemp != mtemp2) {
            if(eventQ.remove(mtemp2)){
              eventQ.add_event(mtemp);
              if (itime2.timemin.after(mtemp.m_interval.life_time.timemin))
                mtemp2.m_interval.life_time.timemin.setTime(itime2.timemin
                  .getTime());
              mtemp = mtemp2;
            }
          }
        }
       
        int existIndex = dtemp2.if_repmsgexist(mtemp);
        if(existIndex>-1 ) {
           mtemp2 = dtemp2.aliveMsglist.get(existIndex);
           if(mtemp.ID.equals(msgtrack))
             cNode.addEdge(new NodeList(dtemp2.mac,mtemp.m_interval.life_time.timemin,3));           
           
          if(!ms.msgStat.containsKey(mtemp.ID))
             ms.msgStat.put(mtemp.ID, new Hashtable<String, Msg>());
          if( mtemp2.delivered){
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
        ms.PVector.get(tabdevList.indexOf(dtemp2.mac)).bAttempted+=mtemp.size;
        ms.PVector.get(tabdevList.indexOf(dtemp2.mac)).bAvailable+=bytesTransferable;
        ms.PVector.get(tabdevList.indexOf(dtemp2.mac)).bDelivered+= bytesTransferable>mtemp.size ? mtemp.size:bytesTransferable;
        ms.msgsAttempt++;
        
        apg.recordUpdate( dtemp, dtemp2, mtemp, bytesTransferable);
        adjust_timeslots(mtemp, dtemp, dtemp2, ttemp, ttemp2, itime2, bw);
        if (!attempt_transfer(mtemp, dtemp, dtemp2, bytesTransferable)){
          eventQ.add_event(mtemp);
         // ms.TransferHistory.get(ms.transfHistC%ms.transfHistSize).failureC++;  
          if(mtemp.ID.equals(msgtrack))
            cNode.addEdge(new NodeList(dtemp2.mac,mtemp.m_interval.life_time.timemin,2));
            
        }
        else{
          
          ms.msgsTransf++;
        }
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
  public void addMsgFrag(Msg mtemp,Msg mtemp2, double bytes){
    if(bytes>mtemp.size & mtemp.fragList.size()==1)
    {
      for(int i=0;i<mtemp.fragList.size();i++)
        mtemp2.fragList.add(new MsgFrag(mtemp.fragList.get(i)));
      return;
    }
    
  }
  public boolean transfer(Msg mtemp, Device dtemp, Device dtemp2, double trans_bytes) {
    
    if (mtemp.replicate) {
      vs.vchange = true;    
      Msg  mtemp2 = dtemp2.get_ECmsg(mtemp);
      if(mtemp2==null){
         mtemp2= new Msg(mtemp);
        
        mtemp2.replicate = true;
        mtemp2.fragmented = true;
      
        apg.adjustCustody(mtemp, mtemp2, dtemp, dtemp2);
        if(mtemp.ID.equals(msgtrack)){
          cNode.addEdge(new NodeList(dtemp2.mac,mtemp.m_interval.life_time.timemin,1));
          mtemp2.cNode= cNode.eventList.getLast();
        }
        mtemp2.ArrivalTime = new Date(mtemp2.m_interval.life_time.timemin.getTime());
          
        Integer devindex = new Integer(tabdevList.indexOf(dtemp2.mac));     
         
        MeetingStats ms=dtemp.meetStat.get(devindex);
  
        dtemp2.aliveMsglist.add(mtemp2);
        if(!ms.msgStat.containsKey(mtemp2.ID))
          ms.msgStat.put(mtemp2.ID, new Hashtable<String, Msg>());
        //if(!ms.msgStat.get(mtemp2.ID).containsKey(mtemp2.frag_id))
        if(ms.msgStat.get(mtemp2.ID).put(mtemp2.frag_id,mtemp)!=null){
          System.err.println("Msg stat error 2 "+mtemp2.ID+" "+mtemp2.frag_id+" "+mtemp2.m_interval.life_time.timemin);
        }
        if(!mtemp.custodian.equals(mtemp.src))
          ms.PVector.get(devindex).AddDelay(mtemp.m_interval.life_time.timemin.getTime()-mtemp.ArrivalTime.getTime());
      }
      else
        mtemp2.fragList
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
      //if(kfact>1)
        //schufflefrags(mtemp, dtemp);
     // mtemp.repCount++;
      //mtemp2.repCount=mtemp.repCount;
      vs.volume += mtemp2.size;
      if(vs.msgs.put(mtemp2.ID.toString()+" "+mtemp2.custodian.toString()+" "+mtemp2.frag_id.toString(), mtemp.ID)!=null){
        record = new LogRecord(Level.FINEST,"Message copy 6 error "+ mtemp2.ID.toString()+" "+mtemp2.custodian.toString()+" "+mtemp2.frag_id.toString()+" "+mtemp2.m_interval.life_time.timemin+" "+mtemp2.m_interval.life_time.timemax);
        lf.log(record);
       }
   
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
    
    /*if(enc){
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
    }*/
  }
  
  public void ReadMsgDelaygraph(String trace, String bandwidth ){
    String line ="";
    int i=0;
    double factor=1;
    BufferedReader in;
    try{if(trace.equals("IBM")){
      in = new BufferedReader(new FileReader("resource/" + trace + "/low"//+/* "/"+bandwidth
          + "MsgDelayNetwork.txt")); // Continue to read lines while
      if(bandwidth.contains("High"))
        factor=5;
    }
    else{
      if(bandwidth.contains("low"))
        in = new BufferedReader(new FileReader("resource/" + trace + "/low"//+/* "/"+bandwidth
            + "MsgDelayNetwork.txt")); // Continue to read lines while
      else
        in = new BufferedReader(new FileReader("resource/" + trace + "/High"//+/* "/"+bandwidth
            + "MsgDelayNetwork.txt")); // Continue to read lines while
    }
     // in = new BufferedReader(new FileReader("resource/" + trace + "/2High"//+/* "/"+bandwidth
       //   + "MsgDelayNetwork.txt")); // Continue to read lines while
    //  in = new BufferedReader(new FileReader("resource/" + trace + /*"/low"//+*/ "/"+bandwidth
      //    + "MsgDelayNetwork.txt")); // Continue to read lines while*/
      
    while(in.ready()){
        line = in.readLine();
        String []frags1=line.split("\t");
        int dindex=Integer.parseInt(frags1[0]);
        Device dtemp = Peerstable.get(tabdevList.get(dindex));
        for( i=1;i<frags1.length;i++){
          int marker = frags1[i].indexOf("'");
          int todindex = Integer.parseInt(frags1[i].substring(0,marker));
          MeetingStats ms = new MeetingStats( todindex,dindex,bw);
          
          ms.duration=Double.parseDouble(frags1[i].substring(marker+1, marker=frags1[i].indexOf("'",++marker)).trim());
          ms.meetingcnt=Integer.parseInt(frags1[i].substring(marker+1, marker=frags1[i].indexOf(":",++marker)).trim());
         /* ms.bAttempted=Long.parseLong(frags1[i].substring(marker+1, marker=frags1[i].indexOf("'",++marker)).trim());
          ms.bDelivered=Long.parseLong(frags1[i].substring(marker+1, marker=frags1[i].indexOf(":",++marker)).trim());
          */
          dtemp.meetStat.put(todindex,ms);
         // LinkedList<DVectorUnit>pVector = new LinkedList<DVectorUnit>();
         while(marker+1<frags1[i].length()){
            Integer dev = Integer.parseInt(frags1[i].substring(marker+1, marker=frags1[i].indexOf(".",marker)).trim());
            DVectorUnit dvtemp = new DVectorUnit(dev,bw);
            dvtemp.tsIndex = Integer.parseInt(frags1[i].substring(marker+1, marker=frags1[i].indexOf(".",marker+1)).trim());
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
            /*dvtemp.bAssigned = Long.parseLong(frags1[i].substring(marker+1, marker=frags1[i].indexOf(".",marker+1)).trim());
            dvtemp.bAttempted = Long.parseLong(frags1[i].substring(marker+1, marker=frags1[i].indexOf(".",marker+1)).trim());
            if(dvtemp.depth!=1)       
            {
              dvtemp.bDelivered = Long.parseLong(frags1[i].substring(marker+1, marker=frags1[i].indexOf(".",marker+1)).trim());
              dvtemp.dAss = Double.parseDouble(frags1[i].substring(marker+1, marker=frags1[i].indexOf(".",marker+1)).trim());
              dvtemp.dAtt = Double.parseDouble(frags1[i].substring(marker+1, marker=frags1[i].indexOf(".",marker+1)).trim());
              dvtemp.dDel = Double.parseDouble(frags1[i].substring(marker+1, marker=frags1[i].indexOf(" ",marker+1)).trim());              
            }
            else{
              dvtemp.bDelivered = Long.parseLong(frags1[i].substring(marker+1, marker=frags1[i].indexOf(" ",marker+1)).trim());
              dvtemp.dAss = dvtemp.bAssigned/(double)dvtemp.bAvailable;
              dvtemp.dAtt = dvtemp.bAttempted/(double)dvtemp.bAvailable;
              dvtemp.dDel = dvtemp.bDelivered/(double)dvtemp.bAvailable;
              
            }*/
           // dvtemp. = Long.parseLong(frags1[i].substring(marker+1, marker=frags1[i].indexOf(" ",marker+1)).trim());
            
            ms.PVector.put(dev,dvtemp);
            String []dvTimes=frags1[i].substring(marker+1,marker=frags1[i].indexOf(";",marker+1)).split(",");
            for(int k=0;k<dvtemp.histLen;k++)
              dvtemp.AddDelay(Double.parseDouble(dvTimes[k]));  
            ms.PVector.put(dvtemp.device, dvtemp);
           // pVector.add(dvtemp);
          }      
        /* Collections.sort(pVector, new DepthComparator());
         for(int m=0;m<pVector.size();m++){
           DVectorUnit dvtemp = pVector.get(m);
           
           if(dvtemp.depth==1)
             dvtemp.calcDeficit();
           else
             
         }*/
        }
      }
      in.close();
      System.out.println("Msg delay file reading done");
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
