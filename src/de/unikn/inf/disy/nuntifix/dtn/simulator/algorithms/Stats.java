package de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms;

//import java.util.Date;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collections;
import java.util.Comparator;
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
//import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.VolumeStats;

public class Stats {

  public Hashtable<String, Device> Peerstable;

  LinkedList<String> tabdevList;

  Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gMsgTable;

  Event eventQ;

  Active_PeerGenerator apg;

  Dijkstra dij;

  int discovery_t; 

  boolean perfect;

  int ap_count;

  double bw;  
  int [][]pairs = {{0,20},{1,19},{2,18},{3,17},{4,16},{5,15},{6,14},{7,13},{8,12},{9,11}}; 
  
  public Stats(Hashtable<String, Device> p, LinkedList<String> td,
      Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>>gm) {   
    Peerstable = p;
    tabdevList = td;
    gMsgTable = gm;
   
 }
  public void setParameters(int apc, double bw) {

    
    this.bw = bw;
    ap_count = apc;
    discovery_t = 0;
  }
  public void createMsgStat(int set,int week, int N) {
    gMsgTable = new Hashtable<Integer, Hashtable<Integer,Hashtable<String,Msg>>>();
    
    long minute = 1000 * 60;    
    int day = 42;
    long st = Peerstable.get(tabdevList.get(pairs[set][0])).starttime.getTime()+((week)*10080*minute);
    
    for (int i = 0; i < day; i++) {
      for(int k=0;k<N;k++){
        Msg  mtemp = new Msg(pairs[set][0], pairs[set][1], 0, st+((i)*240*minute), st+(((i)*240*minute)+(10080*minute)), 0, 0, 1);
        mtemp.start_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
        mtemp.rTime.setTime(Peerstable.get(tabdevList.get(pairs[set][0])).starttime.getTime());
        gMsgTable.put(mtemp.ID, new Hashtable<Integer, Hashtable<String,Msg>>());
        gMsgTable.get(mtemp.ID).put(mtemp.custodian, new Hashtable<String, Msg>());
        gMsgTable.get(mtemp.ID).get(mtemp.custodian).put(mtemp.frag_id, mtemp);
        mtemp.hoplist.add(mtemp.src);
        Peerstable.get(tabdevList.get(mtemp.src)).src = true;
        Peerstable.get(tabdevList.get(mtemp.dest)).dest = true;       
      }
    }
  }
  
  public void Create_InitialEvents(int set,int week, int pCount) {

    dij = new Dijkstra(Peerstable, tabdevList);
    dij.cloneall(ap_count, perfect);
    createMsgStat(set,week,pCount);
    apg = new Active_PeerGenerator(Peerstable, tabdevList, gMsgTable);
    eventQ = new Event();
    Enumeration<Integer> mh_enum = gMsgTable.keys();
    while (mh_enum.hasMoreElements()) {
      Integer mid =   mh_enum.nextElement();
      Enumeration <Integer>c_enum =gMsgTable.get(mid).keys();
      Integer cust= new Integer(0);
      while(c_enum.hasMoreElements()){
        cust =c_enum.nextElement();
        eventQ.add_event(gMsgTable.get(mid).get(cust).get("0"));    
     //   System.out.println(eventQ.getLast().m_interval.life_time.timemin);
      }      
    }   
    for(int i=0;i<eventQ.size();i++)
      System.out.println(eventQ.get(i).m_interval.life_time.timemin);
  } 
  public Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> statsOracle_Initial(){
    
    while (eventQ.size() > 0) {      
      System.out.println(eventQ.size());
      Msg mtemp = eventQ.removeFirst();
     
        if(dij.dijkstraEPO(mtemp, bw, ap_count,true))
        {
          while(mtemp.custodian!=mtemp.dest) {           
            STBR(mtemp,null,null);                 
        }
      }
    }
    return gMsgTable;
  }
  public Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> stats_Initial(String trace, String bw,int set, int week,int pCount,int oweek){
    ReadMsgPath(trace,bw,set,week,pCount,oweek);
    while (eventQ.size() > 0) {      
      System.out.println(eventQ.size());
      Msg mtemp = eventQ.removeFirst();
        
      while(mtemp.custodian!=mtemp.dest & mtemp.m_interval.life_time.timemin.before(mtemp.m_interval.life_time.timemax))            
        STBR(mtemp,null,null);
    }
    return gMsgTable;
  }
 
public void STBR(Msg mtemp,  Hashtable<String, TSlot>slotBuffer, Hashtable<String, TSlot>inslotBuffer) {

  Device dtemp = Peerstable.get(tabdevList.get(mtemp.hoplist.getLast()));
  
  TSlot ttemp = new TSlot();
  /*if(mtemp.nhoplist.getFirst()!=mtemp.dest)
    if(mtemp.m_interval.life_time.timemin.before(mtemp.nhop.T))
        mtemp.m_interval.life_time.timemin.setTime(mtemp.nhop.T.getTime());
   */
  int i = dtemp.find_slot(mtemp.m_interval.life_time.timemin);
  if (i < 0 | i >= dtemp.sorted_slots.size()){
    mtemp.m_interval.life_time.timemin
    .setTime(mtemp.m_interval.life_time.timemax.getTime()+50);
    return;
  }
  ttemp = dtemp.sorted_slots.get(i);
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

    /*if(!eventQ.isFirstMsg(mtemp)){
      eventQ.add_event(mtemp);
      return;
    }*/
    mtemp.validSize=mtemp.size;
    if(mtemp.dest==tabdevList.indexOf(dtemp2.mac)){
      int index = dtemp2.if_repmsgexist(mtemp);
      if(index>-1)
      mtemp.validSize = mtemp.realSize-dtemp2.aliveMsglist.get(index).size;
      
    }      
    double duration = ((double) itime2.timemax.getTime() - itime2.timemin.getTime()) / 1000;
    if(duration<1){
      mtemp.m_interval.life_time.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime()+1000);
      continue;
    }
    if (!dtemp.meetStat.containsKey(sorted_list[pcount1])) {
           
      dtemp.meetStat.put(new Integer(sorted_list[pcount1]), new MeetingStats(sorted_list[pcount1],new Integer(mtemp.custodian)));
      dtemp2.meetStat.put(mtemp.custodian, new MeetingStats(new Integer(mtemp.custodian),new Integer(sorted_list[pcount1])));               
    }
    apg.recordUpdate( dtemp, dtemp2, mtemp, bw * duration,null);
    
    adjust_timeslots(mtemp, dtemp, dtemp2, ttemp, ttemp2, itime2, bw,duration);
    transfer(mtemp, dtemp, dtemp2 );        
      //eventQ.add_event(mtemp);
    
    return;
  }
  if(sorted_list.length>0)
    return;
 
    if (dtemp.sorted_slots.size() > i + 1)
      mtemp.m_interval.life_time.timemin
      .setTime(dtemp.sorted_slots.get(i + 1).timemin.getTime() + 5);
    else
      mtemp.m_interval.life_time.timemin.setTime(ttemp.timemax.getTime() + 500);
  
 // eventQ.add_event(mtemp);
}

public void adjust_timeslots(Msg mtemp, Device dtemp, Device dtemp2,
    TSlot ttemp, TSlot ttemp2, TSlot itime, double bw,double duration) {

  // check if partial delivery has already happened.

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
    mtemp.delivered = true;
    return true;
  }
 // mtemp.nhop = mtemp.nhop.to;  
   // eventQ.add_event(mtemp); 

  return true;
  }
  public void ReadMsgPath(String trace, String bw,int set,int week, int pCount, int oweek){
    gMsgTable = new Hashtable<Integer, Hashtable<Integer,Hashtable<String,Msg>>>();    
    long minute = 1000 * 60;    
    long st = Peerstable.get(tabdevList.get(pairs[set][0])).starttime.getTime()+((week)*10080*minute);
    String line ="";    
    BufferedReader in;
    
    try{     
      in = new BufferedReader(new FileReader("result/" + trace + "/"+ bw+ Integer.toString(pCount)+ "ForwardingOracleStats.txt")); 
      if(oweek!=0)
        in = new BufferedReader(new FileReader("result/" + trace + "/"+ bw+ Integer.toString(pCount)+ "ForwardingOracleStats"+Integer.toString(oweek)+".txt")); 
     long time;
     while(in.ready()){
        line = in.readLine();
        String []frags1=line.split("\t");
        if(frags1.length<5)
          continue;
        Integer src = Integer.parseInt(frags1[1]);
        if(pairs[set][0]!=src)
          continue;
        /*if((week-1+count)*240!=Double.parseDouble(frags1[5]))
          continue;*/
        time= (long)Double.parseDouble(frags1[5]);
        
        Msg  mtemp = new Msg(pairs[set][0], pairs[set][1], 0, st+(time*minute), st+(time*minute)+(10080*minute), 0, 0, 1);
        mtemp.start_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
        mtemp.rTime.setTime(Peerstable.get(tabdevList.get(pairs[set][0])).starttime.getTime());
        mtemp.del_time.setTime(mtemp.start_time.getTime()+(long)Double.parseDouble(frags1[4])*minute);
        gMsgTable.put(mtemp.ID, new Hashtable<Integer, Hashtable<String,Msg>>());
        gMsgTable.get(mtemp.ID).put(mtemp.custodian, new Hashtable<String, Msg>());
        gMsgTable.get(mtemp.ID).get(mtemp.custodian).put(mtemp.frag_id, mtemp);
        mtemp.hoplist.add(mtemp.src);
        Peerstable.get(tabdevList.get(mtemp.src)).src = true;
        Peerstable.get(tabdevList.get(mtemp.dest)).dest = true;       
       
        mtemp.size = Double.parseDouble(frags1[3]);
          for(int i=8;i<frags1.length;i++)
            mtemp.nhoplist.add(Integer.parseInt(frags1[i]));      
          
      }
      in.close();
    }    
    catch (Exception e) {
      System.err.println(e.getMessage()+" "+e.toString());
      e.printStackTrace();
    }   
    
    apg = new Active_PeerGenerator(Peerstable, tabdevList, gMsgTable);
    eventQ = new Event();
    Enumeration<Integer> mh_enum = gMsgTable.keys();
    while (mh_enum.hasMoreElements()) {
      Integer mid =   mh_enum.nextElement();
      Enumeration <Integer>c_enum =gMsgTable.get(mid).keys();
      Integer cust= new Integer(0);
      while(c_enum.hasMoreElements()){
        cust =c_enum.nextElement();
        eventQ.add_event(gMsgTable.get(mid).get(cust).get("0"));    
     //   System.out.println(eventQ.getLast().m_interval.life_time.timemin);
      }      
    }   
    Collections.sort(eventQ, new CreationTimeComparator());
    for(int i=0;i<eventQ.size();i++){
      System.out.println(eventQ.get(i).m_interval.life_time.timemin+" "+eventQ.get(i).del_time);
      eventQ.get(i).del_time = new Date();
    }
    
  }
  public static double Round(double Rval, int Rpl) {
    double p = (double)Math.pow(10,Rpl);
    Rval = Rval * p;
    double tmp = Math.round(Rval);
    return (double)tmp/p;
  }
  public class CreationTimeComparator implements Comparator {

    public int compare(Object o1, Object o2) {
      if( (((Msg)o1).m_interval.life_time.timemin.compareTo(((Msg)o2).m_interval.life_time.timemin))==0)
        return (((Msg)o1).del_time.compareTo(((Msg)o2).del_time));
      return 0;
    }
  }
}
