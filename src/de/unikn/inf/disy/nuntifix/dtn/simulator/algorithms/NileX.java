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
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.*;
import de.unikn.inf.disy.util.logging.LoggingFacility;


public class NileX {

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
 
 // int metricSelect;
  
  public double frag_factor;
  int p = 0;

   
  LoggingFacility lf ;
  
  LogRecord record ;
  
  boolean enc;
 
  public int hours;
  
  int kfact;
  int rfact;
  public int msgtrack=1; 
  
  //public LinkedList<NodeList> eventList;
  
  //LinkedList<NodeList>tEList;
  public NodeList cNode ;

  //public boolean dFlood;
  public NileX(Hashtable<String, Device> p, LinkedList<String> td,
      Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> 
      gm, Hashtable<String, Apoint> gapTab,boolean enc,int m, int r) {

    Peerstable = p;
    tabdevList = td;
    gMsgTable = gm;
    apg = new Active_PeerGenerator(p, td, gm);
    

   // Continue to read lines while
    apg.set_aptable(gapTab);

    //int k;
    
    //k = (int)(Math.log(2)*(bflen/(float)(tabdevList.size()-ap_count)));
   // hfun = new Hash(k, 256, bflen);

     vs = new VolumeStats();
     
     lf = LoggingFacility.getInstance(null);
    
     
    this.enc=enc;
     hours=1;
     kfact=m;
     rfact=r;
     //cNode =  new NodeList("",new Date());
     //eventList = tList;
     
  }

  public void setParameters(int apc, double bw, boolean res, boolean que ) {

    discovery_t = 0;
    resume = res;
    queuing = que;
    this.bw = bw;
    ap_count = apc;    
    
    record = new LogRecord(Level.INFO, "Starting NileX encoding="+Boolean.toString(enc)+ " Fragament= "+Integer.toString(kfact)+" "+rfact);
      
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

    //dij.FuzzyPVDijkstra(mtemp,null);
    /*kfact=Math.max(mtemp.mynhoplist.size(),1);
    rfact=1;
    
    if(kfact>3)
      rfact=2;
    if(kfact>7)
      rfact=3;
    if(kfact>15)
      rfact =4;
    if(kfact>31)
     rfact =5;
    if(kfact>47)
      rfact=6;
    if(kfact>56)
      rfact=7;
    */
      
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
    
    for(int i=1;i<mtemp.frag_count*rfact;i++){
      mtemp2 = new Msg(mtemp);
      mtemp2.born=true;
      mtemp2.replicate = true;
      mtemp2.frag_id = new String(Integer.toString(i));
      mtemp2.cFrags[0]=mtemp2.frag_id;
      mtemp2.size = frag_factor;
      mtemp2.ArrivalTime.setTime(mtemp.m_interval.life_time.timemin.getTime());
      gMsgTable.get(mtemp2.ID).get(mtemp2.custodian).put(mtemp2.frag_id, mtemp2);
      vs.volume+=mtemp.size;
      Peerstable.get(tabdevList.get(mtemp2.src)).aliveMsglist.add(mtemp2);
      if(vs.msgs.put(mtemp2.ID.toString()+" "+mtemp2.custodian.toString()+" "+mtemp2.frag_id.toString(), mtemp2.ID)!=null){        
        record = new LogRecord(Level.FINEST,"Frag Creation error "+ mtemp2.ID.toString()+" "+mtemp2.custodian.toString()+" "+mtemp2.frag_id.toString()+" "+mtemp2.m_interval.life_time.timemin+" "+mtemp2.m_interval.life_time.timemax);
        lf.log(record);
      }     
      eventQ.add_event(mtemp2);
    }  
  }
      
  public void nileXInitial(Date init_T, String trace, String bandwidth) {

    
    h = new History(Peerstable, tabdevList, null, null);
    dij = new Dijkstra(Peerstable, tabdevList);
    Create_InitialEvents(init_T);
    ReadMsgDelaygraph(trace,bandwidth,eventQ.getFirst().m_interval.life_time.timemin);
    Msg mtemp = eventQ.getFirst();
    try {
   
      
       Date histT = new Date();
      
      histT.setTime(mtemp.m_interval.life_time.timemin.getTime()+600000);
      DataOutputStream out;
         
      out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
          + bandwidth + "NileXVolume"+Integer.toString(kfact)+Boolean.toString(enc)+".txt"));
        
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
        nileXRoute(mtemp, true, histT, null);

              
        if (vs.vchange){
          vs.vchange=false;
          out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");
        }       
        if (eventQ.size() > 0){          
          if(eventQ.getFirst().m_interval.life_time.timemin.after(histT)){            
            if(new Date(histT.getTime()+600000).toString()=="Thu Aug 01 09:57:56 CEST 2002")
              p=p+1-1;
            
            histT.setTime(eventQ.getFirst().m_interval.life_time.timemin.getTime()+600000); 
            h.NileHistory(histT,eventQ.getFirst().m_interval.life_time.timemin, ap_count,msgtrack,bw,false);
            
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
     
    } catch (Exception e) {
      System.err.println(e.getMessage()+" "+e.toString());
      System.err.println(mtemp.ID+" "+mtemp.custodian+" "+mtemp.frag_id+" "+mtemp.m_interval.life_time.timemin);
      e.printStackTrace();
    }
   
    //System.out.println(Peerstable.get("debug").Msglist.size());
  }
  
 
  public void nileXRoute(Msg mtemp, boolean applyDij, Date histT, DataOutputStream out) {
  
  
    Device dtemp = Peerstable.get(tabdevList.get(mtemp.hoplist.getLast()));
    
    if(mtemp.ID.equals(msgtrack)){
      cNode = mtemp.cNode;;
     
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
     // mtemp.update = false;
      if(mtemp.m_interval.life_time.timemin.after(histT)){
        if(!eventQ.isFirstMsg(mtemp)){
          eventQ.add_event(mtemp);
          return;
        }  
      }
      int[] sorted_list;
    
       if(mtemp.replicate & (mtemp.update | mtemp.m_interval.life_time.timemin.compareTo(mtemp.rTime)>0) ){
          dij.FuzzyPVDijkstra(mtemp,out);     
       }
       
       sorted_list = apg.GetMyNexthop(itime, mtemp, ap_count);
     
      
      for (int pcount1 = 0; pcount1 < sorted_list.length; pcount1++) {
        boolean priomsg = false;
        Msg mtemp2 = mtemp;  
        mtemp=dtemp.getFirstMsg(mtemp, sorted_list[pcount1],eventQ);
        if(!mtemp2.equals(mtemp))
          priomsg= true; 
        
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
          ms1 = new MeetingStats( tabdevList.indexOf(dtemp2.mac),tabdevList.indexOf(dtemp.mac));
          MeetingStats ms2=new MeetingStats( tabdevList.indexOf(dtemp.mac),tabdevList.indexOf(dtemp2.mac));
          dtemp.meetStat.put(ms1.devindex, ms1);
          dtemp2.meetStat.put(ms2.devindex, ms2); 
          ms1.lastmeeting = new Date(itime2.timemin.getTime());
          ms2.lastmeeting = new Date(ms1.lastmeeting.getTime());
          ms1.meetingcnt++;
          ms2.meetingcnt++;
                  
          ms1.myNeighbor.get(ms1.devindex).PVector.put(ms1.devindex, new DVectorUnit(ms1.devindex,ms1.lastmeeting.getTime()));
          ms2.myNeighbor.get(ms2.devindex).PVector.put(ms2.devindex, new DVectorUnit(ms1.devindex,ms1.lastmeeting.getTime()));
          
        }
         ms1 = dtemp.meetStat.get(sorted_list[pcount1]);
         //ms.transfHistC++;
         
         //ms.TransferHistory.get(ms.transfHistC%ms.transfHistSize).attempC++;
         //ms.TransferHistory.get(ms.transfHistC%ms.transfHistSize).tstamp.setTime(itime2.timemin.getTime());
          
        
        double duration = ((double) itime2.timemax.getTime() - itime2.timemin
            .getTime()) / 1000;
       
          
           
        for(int k=0;k<dtemp2.aliveMsglist.size();k++){
          mtemp2 = dtemp2.aliveMsglist.get(k);
          if(!mtemp2.ID.equals(mtemp.ID))
            continue;
          if(!mtemp2.reconst)
            continue;        
              
          mtemp.delivered = true;
          mtemp.alive = false;
          mtemp.reconst = true;
          vs.volume-=mtemp.size;
          vs.vchange=true;
          if(vs.msgs.remove(mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString())==null){
            record = new LogRecord(Level.FINEST,"Message delete 3_1 error "+ mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+
                mtemp.frag_id.toString()+" "+mtemp.m_interval.life_time.timemin+" "+mtemp.m_interval.life_time.timemax);
            lf.log(record);             
          }          
          return;
        }
        
        int existIndex = dtemp2.if_repmsgexist(mtemp);
        if(existIndex>-1 ) {
           mtemp2 = dtemp2.aliveMsglist.get(existIndex);
                     
          if(!ms1.msgStat.containsKey(mtemp.ID))
             ms1.msgStat.put(mtemp.ID, new Hashtable<String, Msg>());
          if( mtemp2.delivered ){
            
            mtemp.delivered=true;
            mtemp.alive=false;
            if(mtemp.m_interval.life_time.timemin.before(mtemp.m_interval.life_time.timemax))
              mtemp.m_interval.life_time.timemin.setTime(mtemp.m_interval.life_time.timemax.getTime()+50);
            vs.volume-=mtemp.size;
            vs.vchange=true;
            if(vs.msgs.remove(mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString())==null){
              record = new LogRecord(Level.FINEST,"Message delete 3 error "+ mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+
                  mtemp.frag_id.toString()+" "+mtemp.m_interval.life_time.timemin+" "+mtemp.m_interval.life_time.timemax);
              lf.log(record);             
            }           
            return;
          }
          if(priomsg){
            mtemp.m_interval.life_time.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime()+50);
            eventQ.add_event(mtemp);
            return;
          }
          continue;
        }   
        
        double bytesTransferable =bw*duration;
        ms1.bAttempted+=mtemp.size;
        ms1.bAvailable+=bytesTransferable;
        ms1.bDelivered+= bytesTransferable>mtemp.size ? mtemp.size:bytesTransferable;
        ms1.msgsAttempt++;
        
        ms1.myNeighbor.get(ms1.devindex).PVector.get(ms1.devindex).AddDelay(mtemp.m_interval.life_time.timemin.getTime()-mtemp.ArrivalTime.getTime(),mtemp.size,mtemp.m_interval.life_time.timemin.getTime());
        
        apg.recordUpdate( dtemp, dtemp2, mtemp, bytesTransferable);
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
        mtemp.m_interval.life_time.timemin.setTime(ttemp.timemax.getTime() + 500);
      eventQ.add_event(mtemp);
    }
  }  
 
  public void adjust_timeslots(Msg mtemp, Device dtemp, Device dtemp2,
      TSlot ttemp, TSlot ttemp2, TSlot itime, double bw) {

  /*  ttemp.real = false;
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
    mtemp.tag2++;
    return false;
  }

  public boolean transfer(Msg mtemp, Device dtemp, Device dtemp2) {
    
   /*if(!mtemp.replicate){
    
     if(vs.msgs.remove(mtemp.ID.toString()+" "+mtemp.custodian+" "+mtemp.frag_id.toString())==null){
       record = new LogRecord(Level.FINEST,"Message delete 9 error "+ mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString()+" "+mtemp.m_interval.life_time.timemin+" "+mtemp.m_interval.life_time.timemax);
       lf.log(record);
     }
    
     apg.adjustCustody(mtemp,null, dtemp, dtemp2);
     mtemp.update = true;
        
    if(vs.msgs.put(mtemp.ID.toString()+" "+mtemp.custodian+" "+mtemp.frag_id.toString(),mtemp.ID)!=null){
      record = new LogRecord(Level.FINEST,"Message add 5 error "+ mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString()+" "+mtemp.m_interval.life_time.timemin+" "+mtemp.m_interval.life_time.timemax);
      lf.log(record);
    }
    if (tabdevList.indexOf(dtemp2.mac) == mtemp.dest) {
      mtemp.del_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
      vs.volume -= mtemp.size;
      mtemp.delivered=true;
      mtemp.alive=false;
      if(dtemp2.msgcountec(mtemp)>=mtemp.frag_count){
        for(int i=0;i<dtemp2.aliveMsglist.size();i++){
          if(dtemp2.aliveMsglist.get(i).ID.equals(mtemp.ID))
            mtemp.reconst = true;
        }
      }
      if(vs.msgs.remove(mtemp.ID.toString()+" "+mtemp.custodian+" "+mtemp.frag_id.toString())==null){
        record = new LogRecord(Level.FINEST,"Message delete 7 error "+ mtemp.ID.toString()+" "+mtemp.custodian.toString()+" "+mtemp.frag_id.toString()+" "+mtemp.m_interval.life_time.timemin+" "+mtemp.m_interval.life_time.timemax);
        lf.log(record);
      }
      return true;
    }  
    
    
    
    //mtemp.value = mtemp.mynhoplist.get(tabdevList.indexOf(dtemp2.mac)).value;
    /*Enumeration <Integer>denum = mtemp.mynhoplist.keys();
    while (denum.hasMoreElements()){
      int dev = denum.nextElement();
      if(dev==mtemp.dest)
        continue;
      if(mtemp.mynhoplist.get(dev).value>=val)
        mtemp.bannedHops.add(dev);
    }*/
    
   // dtemp2.aliveMsglist.add(mtemp);
    /*eventQ.add_event(mtemp);
    
    return true;
   }*/
      //Integer devindex = new Integer(tabdevList.indexOf(dtemp2.mac));    
    mtemp.tag++;
    if(mtemp.replicate)
        vs.vchange = true;      
    Msg  mtemp2= new Msg(mtemp); 
    mtemp2.replicate = mtemp.replicate;
    apg.adjustCustody(mtemp, mtemp2, dtemp, dtemp2);
    if(mtemp.ID.equals(msgtrack)){
      cNode.addEdge(new NodeList(dtemp2.mac,mtemp.m_interval.life_time.timemin,1));
      mtemp2.cNode= cNode.eventList.getLast();
    }
    mtemp2.ArrivalTime = new Date(mtemp2.m_interval.life_time.timemin.getTime());
      
      //if(!mtemp.custodian.equals(mtemp.src))
       // ms.PVector.get(devindex).AddDelay(mtemp.m_interval.life_time.timemin.getTime()-mtemp.ArrivalTime.getTime(),mtemp.size,mtemp.m_interval.life_time.timemin.getTime());
      
    if (tabdevList.indexOf(dtemp2.mac) == mtemp.dest) {        
      mtemp2.del_time.setTime(mtemp2.m_interval.life_time.timemin.getTime());
      mtemp2.delivered=true;
      mtemp.delivered=true;
      mtemp.alive=false;
      mtemp2.alive=false;
      if(dtemp2.msgcountec(mtemp)>=mtemp.frag_count){
        for(int i=0;i<dtemp2.aliveMsglist.size();i++){
          if(dtemp2.aliveMsglist.get(i).ID.equals(mtemp.ID))
            dtemp2.aliveMsglist.get(i).reconst = true;            
        }
        for(int i=0;i<dtemp.aliveMsglist.size();i++){
          if(dtemp.aliveMsglist.get(i).ID.equals(mtemp.ID))
            dtemp.aliveMsglist.get(i).reconst = true;            
        }
      }
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
    //mtemp.value = mtemp.mynhoplist.get(mtemp2.hoplist.getLast()).value;
   
    //Enumeration <Integer>denum = mtemp.mynhoplist.keys();
    eventQ.add_event(mtemp2);
    eventQ.add_event(mtemp);
    return true;
  } 
  
  
  public void ReadMsgDelaygraph(String trace, String bandwidth, Date tstamp ){
    String line ="";
    int i=0;
    double factor=10;
    BufferedReader in;
    try{
      ///*if(trace.equals("IBM")){
      //in = new BufferedReader(new FileReader("resource/" + trace + "/low"//+/* "/"+bandwidth
        //  + "MsgDelayNetwork.txt")); // Continue to read lines while
      //if(bandwidth.equals("High"))
        //factor=10;
    //}
   // else*/
      /*in = new BufferedReader(new FileReader("resource/" + trace + "/2High"//+/* "/"+bandwidth
          + "MsgDelayNetwork.txt")); // Continue to read lines while*/
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
            int nhkey = Integer.parseInt(queues[l].substring(queues[l].indexOf(":")+1, marker).trim());
            ms.myNeighbor.put(nhkey, new NeighborQ());
            while(marker+1<queues[l].length()){
              Integer dev = Integer.parseInt(queues[l].substring(marker+1, marker=queues[l].indexOf(".",marker)).trim());
              DVectorUnit dvtemp = new DVectorUnit(dev, Long.parseLong(queues[l].substring(marker+1, marker=queues[l].indexOf(":",marker+1)).trim()));
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
              ms.myNeighbor.get(nhkey).PVector.put(dvtemp.device, dvtemp);
            
              int tDelayLen=Integer.parseInt(queues[l].substring(marker+1, marker=queues[l].indexOf(":",++marker)).trim());
            
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
                  dvtemp.AvgDelay = 7*24*60*1.0;
                  dvtemp.AvgSize = 10*1.0;
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
