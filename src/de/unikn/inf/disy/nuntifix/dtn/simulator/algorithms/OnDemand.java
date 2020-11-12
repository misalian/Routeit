package de.unikn.inf.disy.nuntifix.dtn.simulator.algorithms;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;

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

public class OnDemand {
  public Hashtable<String, Device> Peerstable;

  LinkedList<String> tabdevList;

  Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gMsgTable;

  Event eventQ;

  int discovery_t;

  Active_PeerGenerator apg; 

  History h;

  double bw;

  boolean resume;

  boolean queuing;

  

  //boolean EC;

  int ap_count; 
  
  public VolumeStats vs;


  public OnDemand(Hashtable<String, Device> p, LinkedList<String> td,
      Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gm, Hashtable<String, Apoint> gapTab) {

   
    Peerstable = p;
    tabdevList = td;
    gMsgTable = gm;
    apg = new Active_PeerGenerator(p, td, gm);
    apg.set_aptable(gapTab);    
    vs = new VolumeStats();
  }

  public void setParameters(double bw, int apc, boolean res, boolean que) {

    resume = res;
    queuing = que;
    discovery_t = 0;    
    ap_count = apc;
    
    this.bw = bw;
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
        if(gMsgTable.get(mid).get(cust).get("0").mynhoplist.size()>0)
          eventQ.add_event(gMsgTable.get(mid).get(cust).get("0"));  
      }      
    }  
  } 
  public void setbackTime(String trace, String bandwidth){
    String []frags1,frags2;
    try{
      BufferedReader in,in2;
      if(trace.equals("MITBT"))
        in = new BufferedReader(new FileReader("result/" + trace +"/2low"+ "PathFlooding.txt")); // Continue to read lines while*/
       else
         in = new BufferedReader(new FileReader("result/" + trace +"/low"+ "PathFlooding.txt")); // Continue to read lines while*/
      if(trace.equals("MITBT"))
        in2 = new BufferedReader(new FileReader("result/" + trace +"/"+bandwidth+ "OnDemand.txt")); // Continue to read lines while*/
      else
        in2 = new BufferedReader(new FileReader("result/" + trace +"/"+bandwidth+ "OnDemand.txt")); // Continue to read lines while*/
      String line,line2;
      //in.readLine();
    while(in.ready()){
        line = in.readLine();
        line2 = in2.readLine();
        
        frags1=line.split("\t");
        frags2=line2.split("\t");
        if(frags1.length<4)
          continue;
        Msg mtemp =gMsgTable.get(Integer.parseInt(frags1[0])).get(Integer.parseInt(frags1[1])).get("0");
        mtemp.del_time = new Date();
        mtemp.m_interval.life_time.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime()+(long)(Double.parseDouble(frags2[4])*60*1000));
        if(mtemp.m_interval.life_time.timemin.after(mtemp.m_interval.life_time.timemax))
          continue;
        //mtemp.m_interval.life_time.timemax.setTime(mtemp.m_interval.life_time.timemax.getTime()+(long)(Double.parseDouble(frags1[4])*60*1000));
        
        for(int i=6;i<frags1.length;i++)
          mtemp.mynhoplist.put(Integer.parseInt(frags1[i]), new PossibleHop()); 
        /*mtemp.mynhoplist.put(mtemp.dest,new PossibleHop());*/         
       
        Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.remove(mtemp);
        mtemp.dest=mtemp.src;
        mtemp.src=Integer.parseInt(frags1[frags1.length-1]);
        Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.add(mtemp);
    }
    in.close();
    in2.close();
    System.out.println(bandwidth+" Path reading done");
  }
  
  catch (Exception e) {
    System.err.println(" "+e.getMessage()+" "+e.toString());
    e.printStackTrace();
  }
    }
  public void getPaths(String trace, String bandwidth){
    String []frags1;
    try{
      BufferedReader in;
      
       if(trace.equals("MITBT"))
        in = new BufferedReader(new FileReader("result/" + trace +"/2low"+ "PathFlooding.txt")); // Continue to read lines while*/
       else
         in = new BufferedReader(new FileReader("result/" + trace +"/low"+ "PathFlooding.txt")); // Continue to read lines while*/  
        
      String line;
      //in.readLine();
    while(in.ready()){
        line = in.readLine();        
        
        frags1=line.split("\t");     
        if(frags1.length<4)
          continue;
        Msg mtemp =gMsgTable.get(Integer.parseInt(frags1[0])).get(Integer.parseInt(frags1[1])).get("0");
        mtemp.m_interval.life_time.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime()+(long)(Double.parseDouble(frags1[4])*60*1000));
        if(mtemp.m_interval.life_time.timemin.after(mtemp.m_interval.life_time.timemax))
          continue;
        //mtemp.m_interval.life_time.timemax.setTime(mtemp.m_interval.life_time.timemax.getTime()+(long)(Double.parseDouble(frags1[4])*60*1000));
        
        for(int i=6;i<frags1.length;i++)
          mtemp.mynhoplist.put(Integer.parseInt(frags1[i]), new PossibleHop()); 
       /* mtemp.mynhoplist.put(mtemp.dest,new PossibleHop());*/
          
        /*mtemp.mynhoplist.remove(mtemp.dest);
        mtemp.mynhoplist.put(mtemp.src, new PossibleHop());
        Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.remove(mtemp);
        mtemp.dest=mtemp.src;
        mtemp.src=Integer.parseInt(frags1[frags1.length-1]);
        Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.add(mtemp);*/
          
    
      }
      in.close();
      System.out.println(bandwidth+" Path reading done");
    }
      catch (Exception e) {
        System.err.println(" "+e.getMessage()+" "+e.toString());
        e.printStackTrace();
      }
  }
  public void backtrace(Date init_T, String trace, String bandwidth){
    getPaths(trace, bandwidth);
    
    Create_InitialEvents(init_T);
    int i = 0;
    try {
      DataOutputStream out,out2;
      out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + bandwidth + "OnDemandVolume.txt"));
      out2 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
          + bandwidth + "OnDemandbwVolume.txt"));
      
      while (eventQ.size() > 0) {
        vs.vchange=false;
        vs.bwchange = false;
        //i++;
        Msg mtemp  = eventQ.removeFirst();
        if (i++ % 1000 == 0)
          System.out.println(eventQ.size() + " "
              + mtemp.m_interval.life_time.timemin);
      
        if(!mtemp.born)
        {          
          Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.add(mtemp);
          mtemp.born=true;
          vs.volume+=mtemp.size;
          Peerstable.get(tabdevList.get(mtemp.src)).hbytes+=mtemp.size;
          
          
          vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
          out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");       
        }       
        OnDemand_Rout(mtemp);
        
        }
        if (vs.vchange)         
          out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");    
        if (vs.bwchange)         
          out2.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+Long.toString(vs.consumed_bw) + "\n");    
    
        out.close();
    } catch (Exception e) {
      System.err.println(e.getMessage()+" "+e.toString());
      e.printStackTrace();      
    } 
  }
  public void forward(Date init_T, String trace, String bandwidth){
   setbackTime(trace, bandwidth);
    
    Create_InitialEvents(init_T);
    int i = 0;
    try {
      DataOutputStream out;
      out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + bandwidth + "OnDemandVolume.txt"));
      
      while (eventQ.size() > 0) {
        vs.vchange=false;
        //i++;
        Msg mtemp  = eventQ.removeFirst();
        if (i++ % 1000 == 0)
          System.out.println(eventQ.size() + " "
              + mtemp.m_interval.life_time.timemin);
      
        if(!mtemp.born)
        {          
          Peerstable.get(tabdevList.get(mtemp.src)).aliveMsglist.add(mtemp);
          mtemp.born=true;
          vs.volume+=mtemp.size;
          Peerstable.get(tabdevList.get(mtemp.src)).hbytes+=mtemp.size;          
          
          vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());
          out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");       
        }        
        
        OnDemand_Rout(mtemp);
        
        }
        if (vs.vchange)         
          out.writeBytes(Double.toString((vs.tstamp.getTime()-init_T.getTime())/(double)(1000*60*60*24))+"\t"+vs.volume.toString() + "\n");    
    
        out.close();
    } catch (Exception e) {
      System.err.println(e.getMessage()+" "+e.toString());
      e.printStackTrace();      
    } 
  }
  public void OD_initial(Date init_T,String trace, String bandwidth) {
    backtrace(init_T, trace, bandwidth);
 //   forward(init_T, trace, bandwidth);
  }

  public void OnDemand_Rout(Msg mtemp) {

    Device dtemp = Peerstable.get(tabdevList.get(mtemp.custodian));

    TSlot ttemp;
    int i = dtemp.find_slot(mtemp.m_interval.life_time.timemin, 0,
        dtemp.sorted_slots.size() - 1);

      if (i < 0 | i >= dtemp.sorted_slots.size()) {
        vs.tstamp.setTime(mtemp.m_interval.life_time.timemin.getTime());        
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
    sorted_list = apg.GetMyNexthop(itime, mtemp, ap_count);
    
    for (int pcount1 = 0; pcount1 < sorted_list.length; pcount1++) {
      Device dtemp2 = Peerstable.get(tabdevList.get(sorted_list[pcount1]));
      
      
      TSlot itime2 = new TSlot(itime.timemin.getTime(),itime.timemax.getTime(), "");

      int scount2 = 0;
       while (!dtemp2.active_slots[scount2++]);

      TSlot ttemp2 = (TSlot) dtemp2.sorted_slots.get(scount2 - 1);
      dtemp2.active_slots[scount2 - 1] = false;
       double duration;
       if((duration = prepare_timeslots(dtemp, ttemp, dtemp2, ttemp2, itime, itime2, mtemp))>0){
         vs.tstamp.setTime(itime2.timemin.getTime());
         if(eventQ.isFirstMsg(mtemp)){
           if(!dtemp.meetStat.containsKey(sorted_list[pcount1])){
             dtemp.meetStat.put(sorted_list[pcount1], new MeetingStats(sorted_list[pcount1],tabdevList.indexOf(dtemp.mac)));
             dtemp2.meetStat.put(tabdevList.indexOf(dtemp.mac), new MeetingStats(tabdevList.indexOf(dtemp.mac),sorted_list[pcount1]));
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

  
  public double prepare_timeslots(Device dtemp,TSlot ttemp,Device dtemp2, TSlot ttemp2,TSlot itime, TSlot itime2, Msg mtemp){
    if (ttemp2.timemin.after(itime.timemin))
      itime2.timemin.setTime(ttemp2.timemin.getTime());
    if (ttemp2.timemax.before(itime.timemax))
      itime2.timemax.setTime(ttemp2.timemax.getTime());
    mtemp.m_interval.life_time.timemin.setTime(itime2.timemin.getTime());

    return ((double) itime2.timemax.getTime() - itime2.timemin.getTime()) / 1000;  
  }
  

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
    
      
    dtemp.aliveMsglist.remove(mtemp);
    //mtemp.hoplist.add(tabdevList.indexOf(dtemp2.mac));
    /*if (mtemp.nhoplist.size() > 0)
      mtemp.nhoplist.remove();*/
    //while(mtemp.nhoplist.remove()!=tabdevList.indexOf(dtemp2.mac));
      
    mtemp.mynhoplist.remove(tabdevList.indexOf(dtemp2.mac));
    apg.adjustCustody(mtemp, null, dtemp, dtemp2);
    if (tabdevList.indexOf(dtemp2.mac) == mtemp.dest) {
      mtemp.del_time.setTime(mtemp.m_interval.life_time.timemin.getTime());
      vs.vchange=true;
      vs.volume-=mtemp.size;      
      mtemp.delivered=true;;
      /*if(vs.msgs.remove(mtemp.ID.toString())==null)
      {
        System.out.println(mtemp.ID.toString()+" 3 "+mtemp.custodian+" ");
        //System.exit(-1);
      }*/
      
      return true;
    }
    dtemp2.aliveMsglist.add(mtemp);
    eventQ.add_event(mtemp);

    return true;
  }
}
