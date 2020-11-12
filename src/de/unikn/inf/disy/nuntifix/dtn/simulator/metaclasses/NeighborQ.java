package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;

import de.unikn.inf.disy.nuntifix.dtn.simulator.Global;
public class NeighborQ {
  public Hashtable<Integer,DVectorUnit> PVector;
  
  
  public NeighborQ(){
    
    PVector = new Hashtable<Integer, DVectorUnit>();
    
    //PVector.put(tindex,new DVectorUnit(tindex, new Date().getTime()));   
  }
  
 public void pvExchange(MeetingStats ms1,MeetingStats ms2,DVectorUnit cdvtemp,boolean dFlood, Date tstamp){
  
   //Hashtable<Integer,DVectorUnit>pathMetricTable = new Hashtable<Integer, DVectorUnit>();
     
   Enumeration <Integer>nhkenum2= ms2.myNeighbor.keys();
   while(nhkenum2.hasMoreElements()){
     NeighborQ nh2 = ms2.myNeighbor.get(nhkenum2.nextElement());
     Enumeration<Integer>pvkenum2 = nh2.PVector.keys();
     Integer pvkey2;
     while(pvkenum2.hasMoreElements()){
       pvkey2 = pvkenum2.nextElement();
       if(pvkey2.equals(ms1.devindex) | pvkey2.equals(ms1.myindex))
         continue;        
       /*if(tstamp.getTime()-nh2.PVector.get(pvkey2).tstamp.getTime()>Global.weekMSec)
         continue;*/        
       //if(pathMetricTable.containsKey(pvkey2))
         //continue;
       DVectorUnit dvtemp = new DVectorUnit(-1,null);
       if(dFlood){
         if(!PVector.containsKey(pvkey2))
           PVector.put(pvkey2, new DVectorUnit());
         continue;
       }
       else{
         
         //if(!pathMetricTable.containsKey(pvkey2)){
           LinkedList<DVectorUnit>commPathMetric = new LinkedList<DVectorUnit>();
           Enumeration<Integer>nhkenum3 = ms2.myNeighbor.keys();
           while(nhkenum3.hasMoreElements()){
             NeighborQ nh3 = ms2.myNeighbor.get(nhkenum3.nextElement());
             if(nh3.PVector.containsKey(pvkey2)){            
               commPathMetric.add(nh3.PVector.get(pvkey2));
               commPathMetric.getLast().calcAvDTime(tstamp,0);
               commPathMetric.getLast().calcVirAvDTime(tstamp,0);                   
             }
           }
           dvtemp = new DVectorUnit(cdvtemp,commPathMetric,tstamp);
           //pathMetricTable.put(pvkey2,dvtemp);
           if(dvtemp.VirtAvgDelay<1)
             System.out.print("error virt delay");
         //}
         //DVectorUnit dvtemp = new DVectorUnit(cdvtemp,ms2.PVector.get(pvkey2),tstamp);
          //dvtemp = pathMetricTable.get(pvkey2);
          //pathMetricTable = new Hashtable<Integer, DVectorUnit>();
     
          if(dvtemp.VirtAvgDelay>Global.dayMin*8 )
            continue;
          if(PVector.containsKey(pvkey2))   {        
            if(PVector.get(pvkey2).getPathMetric()<dvtemp.getPathMetric())             
              continue;
          }     
         PVector.put(pvkey2, dvtemp);
         ms1.alreadyChecked = new HashMap<Integer, Integer>();
       }
     }
   }     
 }
  
  /*public void mypvexchange(Device dtemp2, boolean dFlood, Date tstamp){
    
    Integer pvkey2;
    MeetingStats ms2;    
   //exchaning path vectors for 2 devices. 
   
    
    DVectorUnit cdvtemp = PVector.get(devindex);
    //cdvtemp.calcDeficit();
    cdvtemp.calcVirAvDTime(tstamp);
    cdvtemp.calcAvDTime(tstamp);
    //removeCongestion(dtemp2);
    Hashtable<Integer,DVectorUnit>pathMetricTable = new Hashtable<Integer, DVectorUnit>();
    
    Enumeration<Integer>mskenum2 = dtemp2.meetStat.keys();
    while(mskenum2.hasMoreElements()){
      ms2=dtemp2.meetStat.get(mskenum2.nextElement());
      if(ms2.devindex.equals(myindex))
          continue;
      if(ms2.alreadyChecked.containsKey(myindex))
        continue;
      if(ms2.congested){
        if(PVector.containsKey(ms2.devindex))
          PVector.remove(ms2.devindex);
        continue;
      }
      
      ms2.alreadyChecked.put(myindex, dtemp2);
          
      Enumeration <Integer>pvkenum2= ms2.PVector.keys();
      while(pvkenum2.hasMoreElements()){
        pvkey2 = pvkenum2.nextElement();
        if(pvkey2.equals(devindex) | pvkey2.equals(myindex))
          continue;        
        if(tstamp.getTime()-ms2.PVector.get(pvkey2).tstamp.getTime()>Global.weekMSec)
            continue;        
        if(pathMetricTable.containsKey(pvkey2))
          continue;
       
        if(dFlood){
          if(!PVector.containsKey(pvkey2))
            PVector.put(pvkey2, new DVectorUnit());
          continue;
         }
         else{
           DVectorUnit dvtemp;
          if(!pathMetricTable.containsKey(pvkey2)){
            LinkedList<DVectorUnit>commPathMetric = new LinkedList<DVectorUnit>();
            Enumeration<Integer>mskenum3 = dtemp2.meetStat.keys();
            while(mskenum3.hasMoreElements()){
              MeetingStats ms3 = dtemp2.meetStat.get(mskenum3.nextElement());
              if(ms3.PVector.containsKey(pvkey2)){            
                commPathMetric.add(ms3.PVector.get(pvkey2));
                commPathMetric.getLast().calcAvDTime(tstamp);
                commPathMetric.getLast().calcVirAvDTime(tstamp);                
              }
            }
            dvtemp = new DVectorUnit(cdvtemp,commPathMetric,tstamp);
            pathMetricTable.put(pvkey2,dvtemp);
          }
          //DVectorUnit dvtemp = new DVectorUnit(cdvtemp,ms2.PVector.get(pvkey2),tstamp);
          dvtemp = pathMetricTable.get(pvkey2);
          
         /* if(dvtemp.VirtAvgDelay>Global.dayMin*7 )
            continue;*/
          /*if(PVector.containsKey(pvkey2))   {        
            if(PVector.get(pvkey2).getPathMetric()<dvtemp.getPathMetric())             
              continue;
          }
            
          PVector.put(pvkey2, dvtemp);
          alreadyChecked = new HashMap<Integer, Device>();
        }
      }     
    } 
    //removeInvalidPaths(dtemp2);
  }*/
  
}
