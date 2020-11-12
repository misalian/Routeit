package de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
//import java.io.DataOutputStream;
//import java.io.FileOutputStream;
import java.io.FileReader;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
//import java.util.ListIterator;

import java.util.LinkedList;



import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.*;

public class HistoryMaxFlow {

  public Hashtable<String, Device> Peerstable;

  LinkedList<String> tabdevList;

  Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gMsgTable;

  Event eventQ;

  Active_PeerGenerator apg;

  Dijkstra dij;

  int discovery_t;

  boolean resume;

  boolean queuing;

  int ap_count;

  double bw;
  
  public VolumeStats vs;  
  boolean maxflow;

  public HistoryMaxFlow(Hashtable<String, Device> p, LinkedList<String> td,
      Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>>gm) {

    /*Peerstable = new Hashtable<String, Device>();
    tabdevList = new LinkedList<String>();
    gMsgTable = new Hashtable<BigDecimal, Msg>();*/
    Peerstable = p;
    tabdevList = td;
    gMsgTable = gm;

    apg = new Active_PeerGenerator(p, td, gm);
    vs = new VolumeStats();
  }

  public void setParameters(int apc, double bw, boolean res, boolean que) {

    resume = res;
    queuing = que;
    this.bw = bw;
    ap_count = apc;
/*    STBR = D;
    EPOX = M;
    perfect = P;*/
    discovery_t = 1;
    //History h = new History(Peerstable,tabdevList,null,null);
    //h.GenHistory(ap_count, bw);

  }

  public void MaxFlow(Date Init_T) {

    dij = new Dijkstra(Peerstable, tabdevList);
   maxflow=true;
    eventQ = new Event();
    Enumeration<Integer> mh_enum = gMsgTable.keys();
    int i=0;
    while (mh_enum.hasMoreElements()) {
      Integer mid =   mh_enum.nextElement();
      Enumeration <Integer>c_enum =gMsgTable.get(mid).keys();
      Integer cust= new Integer(0);
      while(c_enum.hasMoreElements()){
        cust =c_enum.nextElement();        
        Integer frag =new Integer(0);
        Msg mtemp = gMsgTable.get(mid).get(cust).get(frag.toString());
        mtemp.start_time.setTime(Init_T.getTime());
        eventQ.add_event(mtemp);
      }
    }
    Date stTime = new Date(eventQ.getFirst().m_interval.life_time.timemin.getTime());
        
    if(!resume)
      stTime.setTime(Init_T.getTime());
    for(i=0;i<eventQ.size();i++){
      int k=0;
      Msg mtemp=eventQ.get(i);
      System.out.println(i+" "+mtemp.ID.toString());
      /*if(!mtemp.ID.equals(99))
        continue;*/
      double size = 0;//dij.Maxflow_dijkstra(mtemp, ap_count)*bw;
      do
      { 
        k++;
          mtemp.m_interval.life_time.timemin.setTime(stTime.getTime());
          mtemp.m_interval.life_time.timemax.setTime(mtemp.m_interval.life_time.timemin.getTime()+(1000*60*60*24*7));  
          mtemp.size=0;
          mtemp.hoplist = new LinkedList<Integer>();
          mtemp.hoplist.add(mtemp.src);
          mtemp.custodian=mtemp.src;
          mtemp.size=dij.Maxflow_dijkstra(mtemp, ap_count,size,bw/1000,k)*bw;
          double t=mtemp.size;
          
          if(mtemp.size>0){
            while(!mtemp.custodian.equals(mtemp.dest)){
              if(mtemp.m_interval.life_time.timemin.before(mtemp.nhop.T))
                mtemp.m_interval.life_time.timemin.setTime(mtemp.nhop.T.getTime());
              MaxFlowHistory(mtemp, null, null,null);
              
              if(mtemp.m_interval.life_time.timemin.after(mtemp.m_interval.life_time.timemax)){
                System.out.println("time gone "+k);
                break;
                //System.exit(0);
              }
            }
            if(t!=mtemp.size)
              System.out.println(mtemp.size+" "+t+" "+k+" "+i);
            size+=mtemp.size;              
          }
        }while(mtemp.size>0);
        mtemp.size = size;
        mtemp.realSize = size;    
        System.out.println(mtemp.ID+" "+mtemp.size);
    }       
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
  public void createMaxFlowMsgs(Date Init_T, String bandwidth, String trace,boolean path){
    Create_InitialEvents();
    Hashtable<String, TSlot>slotBuffer = new Hashtable<String, TSlot>();
    dij = new Dijkstra(Peerstable, tabdevList); 
    Date stTime = new Date(eventQ.getFirst().m_interval.life_time.timemin.getTime());
    
    ReadMsgDelaygraph(trace, bandwidth, Init_T);
    if(!resume)
      stTime.setTime(Init_T.getTime());
    LinkedList<Integer>nhoplist;
   
     HashMap<String, Integer>pathlist;
    double predictedSize;
    while(eventQ.size()>0){  
      /*if(resume & histT.getTime()!=stTime.getTime()+1000*60*60*12)
        ReadMsgDelaygraph(trace, bandwidth, Init_T);*/
      Msg mtemp = eventQ.removeFirst();
      /*if(!mtemp.ID.equals(99))
        continue;*/
      LinkedList<Integer>CollectivePath = new LinkedList<Integer>();      
     
      mtemp.size=Double.MAX_VALUE;
      double size=0;
      System.out.println(eventQ.size());
      mtemp.start_time.setTime(stTime.getTime());
     pathlist = new HashMap<String, Integer>();
      int j=0;
      while(true)
      {        
        j++;
        slotBuffer = new Hashtable<String, TSlot>();
        mtemp.m_interval.life_time.timemin.setTime(stTime.getTime());
        mtemp.m_interval.life_time.timemax.setTime(mtemp.m_interval.life_time.timemin.getTime()+(1000*60*60*24*7));        
        mtemp.hoplist = new LinkedList<Integer>();
        mtemp.hoplist.add(mtemp.src);
        mtemp.custodian = mtemp.src;
        gMsgTable.get(mtemp.ID).put(mtemp.custodian, new Hashtable<String, Msg>());
        gMsgTable.get(mtemp.ID).get(mtemp.custodian).put(mtemp.frag_id, mtemp);                
              
        if((predictedSize=dij.MaxflowHistory_dijkstra(mtemp, ap_count,path,queuing,CollectivePath,0))==0)
          break;
        mtemp.size=predictedSize;        
        nhoplist = new LinkedList<Integer>();
        nhoplist.add(mtemp.src);
        String pathsting = new String("");
        for(int i=0;i<mtemp.nhoplist.size();i++){
          nhoplist.add(mtemp.nhoplist.get(i));
          pathsting=pathsting.concat(mtemp.nhoplist.get(i).toString());
        }
        /*if(pathlist.containsKey(pathsting))
          System.err.println("Path dupli "+ pathlist.get(pathsting)+" "+pathsting+" "+j);
        pathlist.put(pathsting, j);*/
        //sizePath.put(Double.toString(predictedSize)+" "+Integer.toString(j) , nhoplist);
        //histT.setTime(mtemp.m_interval.life_time.timemin.getTime()+1000*60*60*12);
        while(mtemp.m_interval.life_time.timemin.before(mtemp.m_interval.life_time.timemax)&mtemp.custodian!=mtemp.dest){
          MaxFlowHistory(mtemp,  slotBuffer , null, CollectivePath);                 
         /* if(resume & mtemp.m_interval.life_time.timemin.after(histT)){            
            histT.setTime(mtemp.m_interval.life_time.timemin.getTime()+1000*60*60*12); 
            h.MaxFlowHistory(histT,mtemp.m_interval.life_time.timemin, ap_count,bw);            
            vs.tstamp.setTime(histT.getTime());
          }*/
        }
        /*if(path & mtemp.custodian.equals(mtemp.dest))
          break;*/
       
        if(mtemp.custodian.equals(mtemp.dest)){
          size += mtemp.size;  
          resettHistory(nhoplist,mtemp.size,slotBuffer);
          /*if(sizePath.get(Double.toString(predictedSize)+" "+Integer.toString(j)).size()==1)
            sizePath.remove(predictedSize);*/          
        }
        else{               
          /*Integer meeting_count = Integer.MAX_VALUE;
          Integer hop_id = -1;
          for(int i=0;i<mtemp.hoplist.size()-1;i++){
            if(Peerstable.get(tabdevList.get(mtemp.hoplist.get(i))).meetStat.get(mtemp.hoplist.get(i+1)).PVector.get(mtemp.hoplist.get(i+1)).count<meeting_count){
              meeting_count = Peerstable.get(tabdevList.get(mtemp.hoplist.get(i))).meetStat.get(mtemp.hoplist.get(i+1)).PVector.get(mtemp.hoplist.get(i+1)).count;
             hop_id = i; 
            }           
          }*/
          
          //if(Peerstable.get(tabdevList.get(mtemp.hoplist.getLast())).meetStat.get(mtemp.nhoplist.getFirst()).PVector.get(mtemp.nhoplist.getFirst()).count<meeting_count){
            CollectivePath.add(mtemp.hoplist.getLast()); 
            CollectivePath.add(mtemp.nhoplist.getFirst());              
         /* }           
          else{
            CollectivePath.add(mtemp.hoplist.get(hop_id)); 
            CollectivePath.add(mtemp.hoplist.get(hop_id+1));  
          }*/
        
         
          restoreSlots(slotBuffer);    
          //restoreHistory(nhoplist, predictedSize,mtemp.src);
        }
      }     
      mtemp.realSize=size;      
    }      
  }
  public void MaxFlowHistory_Initial(String trace, Date init_T,String bandwidth) {
    
    History h =  new History(Peerstable, tabdevList, null, null);
    Create_InitialEvents();
    Date histT = new Date(init_T.getTime()+1000*60*60*8);
    while(histT.before(eventQ.getLast().m_interval.life_time.timemin)){
      h.MaxFlowHistory(histT,init_T, ap_count,bw);   
      init_T.setTime(histT.getTime());
      histT.setTime(init_T.getTime()+1000*60*10);
    }
    System.out.println("His Calc Done");
    WriteMsgsDelayGraph(trace, bandwidth, 0);   
  }

  
  public void MaxFlowHistory(Msg mtemp,  Hashtable<String, TSlot>slotBuffer, Hashtable<String, TSlot>inslotBuffer,LinkedList<Integer>Collectivepaths) {

    Device dtemp = Peerstable.get(tabdevList.get(mtemp.custodian));
    
    TSlot ttemp;   
    
    int i = dtemp.find_slot(mtemp.m_interval.life_time.timemin);
    
    //1
      if (i < 0 | i >= dtemp.sorted_slots.size()) {
        vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
        mtemp.m_interval.life_time.timemin
            .setTime(mtemp.m_interval.life_time.timemax.getTime()+5);
        vs.volume -= mtemp.size;       
        vs. vchange = true;
        return;
      }
      //if(!epo){
      ttemp = dtemp.sorted_slots.get(i);
    
      if (mtemp.msg_status(ttemp) == -1){
        vs.volume -= mtemp.size;
        vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
        mtemp.m_interval.life_time.timemin
        .setTime(mtemp.m_interval.life_time.timemax.getTime()+5);
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
    int[] sorted_list;
    if(maxflow)
      sorted_list = apg.GetStrictNexthop(itime, mtemp, ap_count);
    else
      sorted_list = apg.GetNexthop(itime, mtemp, ap_count);
    
    for (int pcount1 = 0; pcount1 < sorted_list.length; pcount1++) {
     /* if(!maxflow){
        int k=0;
        for(k=0;k<Collectivepaths.size();k+=2)
        {
          if(mtemp.custodian.equals(Collectivepaths.get(k)) & sorted_list[pcount1]==Collectivepaths.get(k+1))
            break;
        }   
        if(k<Collectivepaths.size())
          continue;
      }*/
      
      
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
     
      if (!dtemp.meetStat.containsKey(sorted_list[pcount1])) {
        MeetingStats ms1 = new MeetingStats( sorted_list[pcount1],tabdevList.indexOf(dtemp.mac));
        MeetingStats ms2=new MeetingStats( tabdevList.indexOf(dtemp.mac),sorted_list[pcount1]);
        
        ms1.PVector.put(ms1.devindex, new DVectorUnit(ms1.devindex,itime2.timemin));
        ms2.PVector.put(ms2.devindex, new DVectorUnit(ms2.devindex,itime2.timemin));
        dtemp.meetStat.put(ms1.devindex, ms1);
        dtemp2.meetStat.put(ms2.devindex, ms2);          
        
      }      
      
      vs.tstamp.setTime( itime2.timemin.getTime());
      double duration = ((double) itime2.timemax.getTime() - itime2.timemin.getTime()) / 1000;
      if(duration<1){
        mtemp.m_interval.life_time.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime()+1000);
        continue;
      }     
      if(mtemp.size>bw*duration)
        mtemp.size = bw*duration;
      /*if(mtemp.ID.equals(15))*/
      /*System.out.println("transfer "+ mtemp.ID+" "+mtemp.hoplist.getLast()+" "+sorted_list[pcount1]+" "+itime2.timemin+" "+
          itime2.timemax+" "+mtemp.size+" "+bw*duration);*/
      apg.recordUpdate( dtemp, dtemp2, mtemp, bw * duration,vs);
      saveslots(tabdevList.indexOf(dtemp.mac), sorted_list[pcount1], ttemp, ttemp2, slotBuffer,inslotBuffer);
      adjust_timeslots(mtemp, dtemp, dtemp2, ttemp, ttemp2, itime2, bw);
       attempt_transfer(mtemp, dtemp, dtemp2, bw * duration);        
        //eventQ.add_event(mtemp);
      
      return;
    }
    if(sorted_list.length>0)
      return;
   
      if (dtemp.sorted_slots.size() > i + 1)
        mtemp.m_interval.life_time.timemin
        .setTime(dtemp.sorted_slots.get(i + 1).timemin.getTime() );
      else
        mtemp.m_interval.life_time.timemin.setTime(ttemp.timemax.getTime() + 500);
    
   // eventQ.add_event(mtemp);
  }
  
  
  public void adjust_timeslots(Msg mtemp, Device dtemp, Device dtemp2,
      TSlot ttemp, TSlot ttemp2, TSlot itime, double bw) {

    // check if partial delivery has already happened.
    
   /* System.out.println(itime.timemin.toString()+" "+itime.timemax.toString()+" "+mtemp.validSize);
    System.out.println(mtemp.m_interval.life_time.timemin.toString()+" "+mtemp.m_interval.life_time.timemax.toString()+" "+mtemp.validSize);
    System.out.println();
*/
    if (itime.timemax.getTime() < itime.timemin.getTime() + discovery_t * 1000
        + (mtemp.size / bw) * 1000)
      mtemp.m_interval.life_time.timemin.setTime(itime.timemax.getTime()
          + discovery_t * 1000);
    else
      mtemp.m_interval.life_time.timemin.setTime(itime.timemin.getTime()
          + discovery_t * 1000 + (long) ((mtemp.size * 1000)/bw));

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
    
    if(mtemp.size==mtemp.realSize)
      dtemp.aliveMsglist.remove(mtemp);
    //mtemp.hoplist.add(tabdevList.indexOf(dtemp2.mac));
    while(mtemp.nhoplist.remove()!=tabdevList.indexOf(dtemp2.mac));
  
      apg.adjustCustody(mtemp,null, dtemp, dtemp2);
    
    if (dtemp2.mac.equals(tabdevList.get(mtemp.dest))) {
     if(mtemp.del_time.before(mtemp.m_interval.life_time.timemax)){
       if(mtemp.del_time.before(mtemp.m_interval.life_time.timemin))
         mtemp.del_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
     }
     else
      mtemp.del_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
      vs.vchange=true;
      vs.volume-=mtemp.size;
      mtemp.delivered = true;
      if(mtemp.size==1)
        eventQ.KillAll(mtemp);
      return true;
    }   
    mtemp.nhop = mtemp.nhop.to;
    //eventQ.add_event(mtemp);
    return true;
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
  public void restoreHistory(Hashtable<String, LinkedList<Integer>>sizePath){
    Enumeration<String>senum = sizePath.keys();
    while(senum.hasMoreElements()){
      String key = senum.nextElement();;
      double predictedSize= Double.parseDouble(key.substring(0, key.indexOf(" ")));
      LinkedList<Integer>nhoplist = sizePath.get(key);
    for(int i=0;i<nhoplist.size()-1;i++){
      
      if(Peerstable.get(tabdevList.get(nhoplist.get(i))).meetStat.get(nhoplist.get(i+1)).PVector.get(nhoplist.get(i+1)).VirtAvgSize==predictedSize){
        Peerstable.get(tabdevList.get(nhoplist.get(i))).meetStat.get(nhoplist.get(i+1)).PVector.get(nhoplist.get(i+1)).count++;
        Peerstable.get(tabdevList.get(nhoplist.get(i+1))).meetStat.get(nhoplist.get(i)).PVector.get(nhoplist.get(i)).count++;        
      }
      
      else
      {
        Peerstable.get(tabdevList.get(nhoplist.get(i))).meetStat.get(nhoplist.get(i+1)).PVector.get(nhoplist.get(i+1)).VirtAvgSize
        = Peerstable.get(tabdevList.get(nhoplist.get(i))).meetStat.get(nhoplist.get(i+1)).PVector.get(nhoplist.get(i+1)).VirtAvgSizeBckUp;
        Peerstable.get(tabdevList.get(nhoplist.get(i+1))).meetStat.get(nhoplist.get(i)).PVector.get(nhoplist.get(i)).VirtAvgSize
        = Peerstable.get(tabdevList.get(nhoplist.get(i+1))).meetStat.get(nhoplist.get(i)).PVector.get(nhoplist.get(i)).VirtAvgSizeBckUp;
      }
    }
    }
  }
  public void restoreHistory(LinkedList<Integer>nhoplist, double predictedSize, int src){
    for(int i=0;i<nhoplist.size()-1;i++){
      
      if(Peerstable.get(tabdevList.get(nhoplist.get(i))).meetStat.get(nhoplist.get(i+1)).PVector.get(nhoplist.get(i+1)).VirtAvgSize==predictedSize){
        Peerstable.get(tabdevList.get(nhoplist.get(i))).meetStat.get(nhoplist.get(i+1)).PVector.get(nhoplist.get(i+1)).count++;
        Peerstable.get(tabdevList.get(nhoplist.get(i+1))).meetStat.get(nhoplist.get(i)).PVector.get(nhoplist.get(i)).count++;        
      }      
      else
      {
        Peerstable.get(tabdevList.get(nhoplist.get(i))).meetStat.get(nhoplist.get(i+1)).PVector.get(nhoplist.get(i+1)).VirtAvgSize
        = Peerstable.get(tabdevList.get(nhoplist.get(i))).meetStat.get(nhoplist.get(i+1)).PVector.get(nhoplist.get(i+1)).VirtAvgSizeBckUp;
        Peerstable.get(tabdevList.get(nhoplist.get(i+1))).meetStat.get(nhoplist.get(i)).PVector.get(nhoplist.get(i)).VirtAvgSize
        = Peerstable.get(tabdevList.get(nhoplist.get(i+1))).meetStat.get(nhoplist.get(i)).PVector.get(nhoplist.get(i)).VirtAvgSizeBckUp;
      }
    }
   /* for(int i=0;i<mtemp.hoplist.size()-1;i++){
      Peerstable.get(tabdevList.get(mtemp.hoplist.get(i))).meetStat.get(mtemp.hoplist.get(i+1)).PVector.get(mtemp.hoplist.get(i+1)).VirtAvgDelay
        = Peerstable.get(tabdevList.get(mtemp.hoplist.get(i))).meetStat.get(mtemp.hoplist.get(i+1)).PVector.get(mtemp.hoplist.get(i+1)).VirtAvgDelayBckUp;
      Peerstable.get(tabdevList.get(mtemp.hoplist.get(i))).meetStat.get(mtemp.hoplist.get(i+1)).PVector.get(mtemp.hoplist.get(i+1)).VirtAvgSize
      = Peerstable.get(tabdevList.get(mtemp.hoplist.get(i))).meetStat.get(mtemp.hoplist.get(i+1)).PVector.get(mtemp.hoplist.get(i+1)).VirtAvgSizeBckUp;    
    }
    Peerstable.get(tabdevList.get(mtemp.hoplist.getLast())).meetStat.get(mtemp.nhoplist.getFirst()).PVector.get(mtemp.nhoplist.getFirst()).VirtAvgDelay
    =  Peerstable.get(tabdevList.get(mtemp.hoplist.getLast())).meetStat.get(mtemp.nhoplist.getFirst()).PVector.get(mtemp.nhoplist.getFirst()).VirtAvgDelayBckUp;
    Peerstable.get(tabdevList.get(mtemp.hoplist.getLast())).meetStat.get(mtemp.nhoplist.getFirst()).PVector.get(mtemp.nhoplist.getFirst()).VirtAvgSize
    =  Peerstable.get(tabdevList.get(mtemp.hoplist.getLast())).meetStat.get(mtemp.nhoplist.getFirst()).PVector.get(mtemp.nhoplist.getFirst()).VirtAvgSizeBckUp;
    
    for(int i=0;i<mtemp.nhoplist.size()-1;i++){
      
      Peerstable.get(tabdevList.get(mtemp.nhoplist.get(i))).meetStat.get(mtemp.nhoplist.get(i+1)).PVector.get(mtemp.nhoplist.get(i+1)).VirtAvgDelay
      =  Peerstable.get(tabdevList.get(mtemp.nhoplist.get(i))).meetStat.get(mtemp.nhoplist.get(i+1)).PVector.get(mtemp.nhoplist.get(i+1)).VirtAvgDelayBckUp;
      Peerstable.get(tabdevList.get(mtemp.nhoplist.get(i))).meetStat.get(mtemp.nhoplist.get(i+1)).PVector.get(mtemp.nhoplist.get(i+1)).VirtAvgSize
      =  Peerstable.get(tabdevList.get(mtemp.nhoplist.get(i))).meetStat.get(mtemp.nhoplist.get(i+1)).PVector.get(mtemp.nhoplist.get(i+1)).VirtAvgSizeBckUp;
    }*/
   /* for(int i=0;i<mtemp.hoplist.size()-1;i++){
      if(Peerstable.get(tabdevList.get(mtemp.hoplist.get(i))).meetStat.get(mtemp.hoplist.get(i+1)).PVector.get(mtemp.hoplist.get(i+1)).VirtAvgSize==predictedSize){
        Peerstable.get(tabdevList.get(mtemp.hoplist.get(i))).meetStat.get(mtemp.hoplist.get(i+1)).PVector.get(mtemp.hoplist.get(i+1)).count++;
        Peerstable.get(tabdevList.get(mtemp.hoplist.get(i+1))).meetStat.get(mtemp.hoplist.get(i)).PVector.get(mtemp.hoplist.get(i)).count++;
        
      }
      
      else
      {
        Peerstable.get(tabdevList.get(mtemp.hoplist.get(i))).meetStat.get(mtemp.hoplist.get(i+1)).PVector.get(mtemp.hoplist.get(i+1)).VirtAvgSize
        = Peerstable.get(tabdevList.get(mtemp.hoplist.get(i))).meetStat.get(mtemp.hoplist.get(i+1)).PVector.get(mtemp.hoplist.get(i+1)).VirtAvgSizeBckUp;
        Peerstable.get(tabdevList.get(mtemp.hoplist.get(i+1))).meetStat.get(mtemp.hoplist.get(i)).PVector.get(mtemp.hoplist.get(i)).VirtAvgSize
        = Peerstable.get(tabdevList.get(mtemp.hoplist.get(i+1))).meetStat.get(mtemp.hoplist.get(i)).PVector.get(mtemp.hoplist.get(i)).VirtAvgSizeBckUp;
      }
    }
    if(Peerstable.get(tabdevList.get(mtemp.hoplist.getLast())).meetStat.get(mtemp.nhoplist.getFirst()).PVector.get(mtemp.nhoplist.getFirst()).VirtAvgSize==predictedSize){
      Peerstable.get(tabdevList.get(mtemp.hoplist.getLast())).meetStat.get(mtemp.nhoplist.getFirst()).PVector.get(mtemp.nhoplist.getFirst()).count++;
      Peerstable.get(tabdevList.get(mtemp.nhoplist.getFirst())).meetStat.get(mtemp.hoplist.getLast()).PVector.get(mtemp.hoplist.getLast()).count++;
    }
    
    else
    {
      Peerstable.get(tabdevList.get(mtemp.hoplist.getLast())).meetStat.get(mtemp.nhoplist.getFirst()).PVector.get(mtemp.nhoplist.getFirst()).VirtAvgSize
      =  Peerstable.get(tabdevList.get(mtemp.hoplist.getLast())).meetStat.get(mtemp.nhoplist.getFirst()).PVector.get(mtemp.nhoplist.getFirst()).VirtAvgSizeBckUp;
      Peerstable.get(tabdevList.get(mtemp.nhoplist.getFirst())).meetStat.get(mtemp.hoplist.getLast()).PVector.get(mtemp.hoplist.getLast()).VirtAvgSize
      =  Peerstable.get(tabdevList.get(mtemp.nhoplist.getFirst())).meetStat.get(mtemp.hoplist.getLast()).PVector.get(mtemp.hoplist.getLast()).VirtAvgSizeBckUp;
    }   
  
    for(int i=0;i<mtemp.nhoplist.size()-1;i++){
    
      if(Peerstable.get(tabdevList.get(mtemp.nhoplist.get(i))).meetStat.get(mtemp.nhoplist.get(i+1)).PVector.get(mtemp.nhoplist.get(i+1)).VirtAvgSize==predictedSize){
        Peerstable.get(tabdevList.get(mtemp.nhoplist.get(i))).meetStat.get(mtemp.nhoplist.get(i+1)).PVector.get(mtemp.nhoplist.get(i+1)).count++;
        Peerstable.get(tabdevList.get(mtemp.nhoplist.get(i+1))).meetStat.get(mtemp.nhoplist.get(i)).PVector.get(mtemp.nhoplist.get(i)).count++;
      }
      
      else
      {
        Peerstable.get(tabdevList.get(mtemp.nhoplist.get(i))).meetStat.get(mtemp.nhoplist.get(i+1)).PVector.get(mtemp.nhoplist.get(i+1)).VirtAvgSize
        =  Peerstable.get(tabdevList.get(mtemp.nhoplist.get(i))).meetStat.get(mtemp.nhoplist.get(i+1)).PVector.get(mtemp.nhoplist.get(i+1)).VirtAvgSizeBckUp;
        Peerstable.get(tabdevList.get(mtemp.nhoplist.get(i+1))).meetStat.get(mtemp.nhoplist.get(i)).PVector.get(mtemp.nhoplist.get(i)).VirtAvgSize
        =  Peerstable.get(tabdevList.get(mtemp.nhoplist.get(i+1))).meetStat.get(mtemp.nhoplist.get(i)).PVector.get(mtemp.nhoplist.get(i)).VirtAvgSizeBckUp;
      }    
    }*/
  }
  public void resettHistory(LinkedList<Integer>hoplist, double predictedSize,Hashtable<String, TSlot>slotBuffer){
    slotBuffer = new Hashtable<String, TSlot>();
    /*for(int i=0;i<mtemp.hoplist.size()-1;i++){
      
      
      //nhopList.remove(new Integer(mtemp.hoplist.get(i)));
      
      Peerstable.get(tabdevList.get(mtemp.hoplist.get(i))).meetStat.get(mtemp.hoplist.get(i+1)).PVector.get(mtemp.hoplist.get(i+1)).VirtAvgSizeBckUp
      = Peerstable.get(tabdevList.get(mtemp.hoplist.get(i))).meetStat.get(mtemp.hoplist.get(i+1)).PVector.get(mtemp.hoplist.get(i+1)).VirtAvgSize;    
      
    Peerstable.get(tabdevList.get(mtemp.hoplist.get(i+1))).meetStat.get(mtemp.hoplist.get(i)).PVector.get(mtemp.hoplist.get(i)).VirtAvgSizeBckUp
    = Peerstable.get(tabdevList.get(mtemp.hoplist.get(i+1))).meetStat.get(mtemp.hoplist.get(i)).PVector.get(mtemp.hoplist.get(i)).VirtAvgSize;    
    }*/
    
    for(int i=0;i<hoplist.size()-1;i++){
   
      if(Peerstable.get(tabdevList.get(hoplist.get(i))).meetStat.get(hoplist.get(i+1))
          .PVector.get(hoplist.get(i+1)).VirtAvgSize<=predictedSize){
        
        Peerstable.get(tabdevList.get(hoplist.get(i))).meetStat.get(hoplist.get(i+1))
        .PVector.get(hoplist.get(i+1)).count--;   
        Peerstable.get(tabdevList.get(hoplist.get(i+1))).meetStat.get(hoplist.get(i))
        .PVector.get(hoplist.get(i)).count--;
      }
      else
      {
        Peerstable.get(tabdevList.get(hoplist.get(i))).meetStat.get(hoplist.get(i+1))
        .PVector.get(hoplist.get(i+1)).VirtAvgSize-=predictedSize/Peerstable.get(tabdevList.get(hoplist.get(i))).meetStat.get(hoplist.get(i+1))
        .PVector.get(hoplist.get(i+1)).count;  
        Peerstable.get(tabdevList.get(hoplist.get(i+1))).meetStat.get(hoplist.get(i))
        .PVector.get(hoplist.get(i)).VirtAvgSize-=predictedSize/Peerstable.get(tabdevList.get(hoplist.get(i+1))).meetStat.get(hoplist.get(i))
        .PVector.get(hoplist.get(i)).count;
      }
    }
  }
  
  public void WriteMsgsDelayGraph(String trace, String bandwidth, long  t ){
    int devlist = Peerstable.size()-ap_count;
    Device dtemp;
    try{
      DataOutputStream out = new DataOutputStream(new FileOutputStream("resource/" + trace + "/"+bandwidth
          +"MsgDelayNetworkMaxFlow.txt")); // Continue to read lines while*/
    
    
      for(int i=0;i<devlist;i++){
        dtemp = Peerstable.get(tabdevList.get(i));
        out.writeBytes(Integer.toString(i));
        Enumeration<Integer>mskenum = dtemp.meetStat.keys();
        while(mskenum.hasMoreElements())
        {
          MeetingStats ms = dtemp.meetStat.get(mskenum.nextElement());
          out.writeBytes("\t"+ms.devindex+"'"+Long.toString(ms.lastmeeting.getTime())+"'"+ms.meetingcnt+";");//+"'"+ms.bAttempted+"'"+ms.bDelivered+":");
           
        
            
          Enumeration<Integer>dvkenum = ms.PVector.keys();
          while(dvkenum.hasMoreElements()){
            
              DVectorUnit dvtemp = ms.PVector.get(dvkenum.nextElement());
                out.writeBytes(" "+dvtemp.device+"."+t/*);//+"."+dvtemp.tsIndex*/+"."+dvtemp.depth/*+"."+dvtemp.bAvailable*/);
              
              dvtemp.calcAvDTime(null,0);
              dvtemp.calcVirAvDTime(null,0);
              out.writeBytes(":"+Integer.toString(dvtemp.msgDelaysList.size())+":"+Double.toString(dvtemp.stdVDelay)+":"+Double.toString(dvtemp.stdVSize)+":");
              
              if(dvtemp.msgDelaysList.size()>0){
              
                for(int j=0;j<dvtemp.msgDelaysList.size();j++){
                  
                  out.writeBytes(Double.toString(Round(dvtemp.msgDelaysList.get(j).time,2))+","+Double.toString(Round(dvtemp.msgDelaysList.get(j).size,2))+","
                      +Long.toString(t)+",");  
                }
              }
              else{                        
                out.writeBytes(Double.toString(Round(dvtemp.AvgDelay,2))+","+Double.toString(Round(dvtemp.AvgSize,2))+",");
              }
            
              out.writeBytes(":"+Integer.toString(dvtemp.virtmsgDelaysList.size())+":");
              if(dvtemp.virtmsgDelaysList.size()>0){
              
                for(int j=0;j<dvtemp.virtmsgDelaysList.size();j++){
                  
                  out.writeBytes(Double.toString(Round(dvtemp.virtmsgDelaysList.get(j).time,2))+","+Double.toString(Round(dvtemp.virtmsgDelaysList.get(j).size,2))+","
                      +Long.toString(t)+",");  
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
  public static double Round(double Rval, int Rpl) {
    double p = (double)Math.pow(10,Rpl);
    Rval = Rval * p;
    double tmp = Math.round(Rval);
    return (double)tmp/p;
      }
  public void ReadMsgDelaygraph(String trace, String bandwidth, Date tstamp ){
    String line ="";
    int i=0;
    
    String bwidth = (bandwidth.endsWith("Path") ?  bandwidth.substring(0,bandwidth.indexOf("Path")) : bandwidth);
    bwidth = (bwidth.endsWith("Que") ?  bwidth.substring(0,bwidth.indexOf("Que")) : bwidth);
    bwidth = (bwidth.endsWith("Res") ? bwidth.substring(0,bwidth.indexOf("Res")) : bwidth);
    BufferedReader in;
    try{
    if(queuing)
      in = new BufferedReader(new FileReader("resource/" + trace + /*"/low"//+*/ "/"+ bwidth + "MsgDelayNetworkMaxFlow.txt")); // Continue to read lines while*/
    else
      in = new BufferedReader(new FileReader("resource/" + trace + /*"/low"//+*/ "/"+ bwidth + "MsgDelayNetwork.txt")); // Continue to read lines while*/
       
    while(in.ready()){
        line = in.readLine();
        String []frags1=line.split("\t");
        int dindex=Integer.parseInt(frags1[0]);
        Device dtemp = Peerstable.get(tabdevList.get(dindex));
        dtemp.meetStat = new Hashtable<Integer, MeetingStats>();
        for( i=1;i<frags1.length;i++){
          int marker = frags1[i].indexOf("'");
          int todindex = Integer.parseInt(frags1[i].substring(0,marker));

          MeetingStats ms = new MeetingStats( todindex,dindex);
          
          ms.lastmeeting = new Date(Long.parseLong(frags1[i].substring(marker+1, marker=frags1[i].indexOf("'",++marker)).trim()));
          ms.meetingcnt=Integer.parseInt(frags1[i].substring(marker+1, marker=frags1[i].indexOf(";",++marker)).trim());
        
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
                dvtemp.calcAvDTime(null,0);
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
                dvtemp.calcVirAvDTime(null,0);
                dvtemp.count = dvtemp.virtmsgDelaysList.size();
              }
              else{
                
                dvtemp.VirtAvgDelay = Double.parseDouble(queues[l].substring(marker+1, marker=queues[l].indexOf(",",marker+1)).trim());
                dvtemp.VirtAvgSize = Double.parseDouble(queues[l].substring(marker+1, marker=queues[l].indexOf(",",marker+1)).trim());
                if(dvtemp.VirtAvgSize<dvtemp.AvgSize){
                  //dvtemp.VirtAvgDelay = 7*24*60;
                  dvtemp.VirtAvgSize = dvtemp.AvgSize+1;
                }
              } 
              dvtemp.VirtAvgDelayBckUp = dvtemp.VirtAvgDelay;
              dvtemp.VirtAvgSizeBckUp = dvtemp.VirtAvgSize;
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
}

