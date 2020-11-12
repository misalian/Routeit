package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.ArrayList;
import java.util.LinkedList;

import de.unikn.inf.disy.nuntifix.dtn.simulator.Global;



public class DVectorUnit {
  
  //public static int histLen=0;
 /* public static long weekMin = 7*24*60;
  public static long weekMSec = (weekMin+(24*60))*60*1000;*/
  
  public int depth;
  
  public int device;
  
 // public double []msgDelays; // to track the timestamp of this bloomval so that old history can be avoided 
  
  public ArrayList<TDelayFactor>msgDelaysList;
  public ArrayList<TDelayFactor>virtmsgDelaysList;
 // public int tsIndex;  
  //public double SumDelay;
  
  public Double AvgDelay;
  public Double AvgSize;
  public Double stdSize;
  public Double stdDelay;
  
  
  public Double VirtAvgDelay;
  public Double VirtAvgSize;
  public Double stdVSize;
  public Double stdVDelay;
  
  
  public Double VirtAvgDelayBckUp;
  public Double VirtAvgSizeBckUp;
  /*public double dDel;
  public double dAss;
  public double dAtt;*/
  
  public int count;
  
  public int cogDevices;
  
  public Date tstamp;

  //private double avstdvdelay;
  public  DVectorUnit(  ){
    
  } 
  public  DVectorUnit(Integer dev, Date t   ){
    
    AvgDelay = new Double(0);
    AvgSize = new Double(0);
    VirtAvgDelay = new Double(0);
    VirtAvgSize = new Double(0);
    stdDelay = new Double(0);;
    stdVDelay =new Double(0);
    stdSize = new Double(0);
    stdVSize = new Double(0);
    device = new Integer(dev);
    //this.depth = new Integer(depth);
   // msgDelays = new double[histLen];
   // SumDelay = new Double(0);
   // tsIndex = new Integer(0);
    msgDelaysList = new ArrayList<TDelayFactor>();
    virtmsgDelaysList= new ArrayList<TDelayFactor>();
    cogDevices=0;
    
 /*   for(int i=0;i<histLen;i++){
      msgDelays[i] = new Double(Global.weekMin);
      SumDelay+=msgDelays[i];
    }
   */ 
    depth = new Integer(1);   
    //bAvailable = 1;
    if(t!=null)
      tstamp = new Date(t.getTime());
  } 
 public  DVectorUnit(DVectorUnit dv1,DVectorUnit dv2 , Date t,boolean maxflow ){
    
    device = new Integer(dv2.device);
    //SumDelay = new Double(0);
    //msgDelays = new double[histLen];
    //tsIndex = new Integer(dv1.tsIndex);
    msgDelaysList = new ArrayList<TDelayFactor>();
    virtmsgDelaysList= new ArrayList<TDelayFactor>();
   /* for(int i=0;i<histLen;i++){
      msgDelays[i] = new Double(dv1.msgDelays[i]+dv2.msgDelays[i]);
      SumDelay+=msgDelays[i];
    }   
    */
    if(!maxflow)
    tstamp = new Date(t.getTime());
    dv2.calcVirAvDTime(tstamp,0);
    dv2.calcAvDTime(tstamp,0);
    AvgDelay = dv1.AvgDelay + dv2.AvgDelay;
    AvgSize = Math.max(dv1.AvgSize , dv2.AvgSize);
    stdSize = Math.max(dv1.stdSize, dv2.stdSize);
    stdDelay =  Math.max(dv1.stdDelay, dv2.stdDelay);
    
    VirtAvgDelay = dv1.VirtAvgDelay + dv2.VirtAvgDelay;
    VirtAvgSize = Math.min(dv1.VirtAvgSize , dv2.VirtAvgSize);
    stdVSize = Math.max(dv1.stdVSize, dv2.stdVSize);
    stdVDelay =  Math.max(dv1.stdVDelay, dv2.stdVDelay);
    
    
    depth = new Integer(dv1.depth+dv2.depth);
    
    cogDevices = dv1.cogDevices+dv2.cogDevices;
    
    
    /*if(dv1.dAss<1 | dv2.dAss<1)
      dAss= Math.max(dv1.dAss,dv2.dAss);
    else
      dAss = dv1.dAss*dv2.dAss;
    
    if(dv1.dDel<1 | dv2.dDel<1)
      dDel= Math.max(dv1.dDel,dv2.dDel);
    else
      dDel = dv1.dDel*dv2.dDel;
    
    if(dv1.dAtt<1 | dv2.dAtt<1)
      dAtt= Math.max(dv1.dAtt,dv2.dAtt);
    else
      dAtt = dv1.dAtt*dv2.dAtt;*/
    //bandWidth = 0.5*(dv1.bandWidth+dv2.bandWidth);
  } 
 public  DVectorUnit(DVectorUnit dv1,LinkedList<DVectorUnit> commPathMetric , Date t, boolean maxflow ){
   
   device = new Integer(commPathMetric.get(0).device);
   /*double adelay=0,avdelay=0,astdsize=commPathMetric.get(0).stdSize,
   astddelay=commPathMetric.get(0).stdDelay, asize=0, avsize=0,
   avstdsize=commPathMetric.get(0).stdVSize,avstddelay=commPathMetric.get(0).stdVDelay;
   */
   double adelay=0,avdelay=0,astdsize=0, astddelay=0,asize=0, avsize=0, avstdsize=0,avstddelay=0;
   
   DVectorUnit dvtemp;
  
   boolean std=false;
   for( int i=0;i<commPathMetric.size();i++){
   
     dvtemp = commPathMetric.get(i);
     /*if(Math.log10(dvtemp.VirtAvgSize)<1 |Math.log10(dvtemp.AvgDelay)<1| Math.log10(dvtemp.VirtAvgDelay)<1)
       i=i+1-1;*/
     adelay += dvtemp.AvgDelay;//*Math.pow((Math.log10(dvtemp.AvgSize)),0.4);
     avdelay += dvtemp.VirtAvgDelay;//*Math.pow((Math.log10(dvtemp.VirtAvgSize)),0.4);
     /*if(avdelay==Double.NaN){
       System.err.print("NAN Error "+ dvtemp.VirtAvgDelay+" "+maxVSize+" "+dvtemp.VirtAvgSize);
       System.exit(0);
     }*/
     
    /* asize = Math.min(asize,dvtemp.AvgSize*(maxDelay/dvtemp.AvgDelay));
     avsize = Math.max(asize,dvtemp.VirtAvgSize*(maxVDelay/dvtemp.VirtAvgDelay));
     */
     asize += dvtemp.AvgSize;//*Math.pow((Math.log10(dvtemp.AvgDelay)+1),0.4);
     avsize += dvtemp.VirtAvgSize;//*Math.pow((Math.log10(dvtemp.VirtAvgDelay)+1),0.4);
     
     
     /*astdsize = Math.min(astdsize,dvtemp.stdSize);
     avstdsize = Math.max(avstdsize,dvtemp.stdVSize);
     
     */
     if(dvtemp.virtmsgDelaysList.size()<2)
       continue;     
     avstddelay = Math.pow(Math.pow(avstddelay,2)+Math.pow(dvtemp.stdVDelay,2),0.5);
     avstdsize = Math.pow(Math.pow(avstdsize,2)+Math.pow(dvtemp.stdVSize,2),0.5);
     if(avstdsize==0 | avstddelay==0)
       i=i+1-1;
     std = true;
     if(dvtemp.msgDelaysList.size()<2)
       continue;
     astdsize = Math.pow(Math.pow(astdsize,2)+Math.pow(dvtemp.stdSize,2),0.5);
     astddelay = Math.pow(Math.pow(astddelay,2)+Math.pow(dvtemp.stdDelay,2),0.5);  
     
     }
   
  
   msgDelaysList = new ArrayList<TDelayFactor>();
   virtmsgDelaysList= new ArrayList<TDelayFactor>();
   if(!maxflow)
   tstamp = new Date(t.getTime());
   
   //double factor = Math.pow(commPathMetric.size(),1);
   double factor;
   if(!maxflow)
     factor= avsize/Math.pow(asize,0.9);
   else
     factor=commPathMetric.size();
   
   AvgDelay = dv1.AvgDelay + (adelay/factor);
   AvgSize = Math.max(dv1.AvgSize , (asize/factor));
   
   //factor=0;
   VirtAvgDelay = dv1.VirtAvgDelay + (avdelay/factor);
   VirtAvgSize = Math.min(dv1.VirtAvgSize , (avsize*factor));
   
   
   /*Collections.sort(commPathMetric, new VAvgTimeComparator());
   
   AvgDelay = dv1.AvgDelay + commPathMetric.get(0).AvgDelay;
   AvgSize = dv1.AvgSize + commPathMetric.get(0).AvgSize;
   
   VirtAvgDelay = dv1.VirtAvgDelay + commPathMetric.get(0).VirtAvgDelay;
   VirtAvgSize = dv1.VirtAvgSize + commPathMetric.get(0).VirtAvgSize;*/
   
   depth = new Integer(dv1.depth+commPathMetric.get(0).depth);
   
  
   stdSize = Math.pow(Math.pow(astdsize/factor,2)+Math.pow(dv1.stdSize,2),0.5);
   stdDelay =  Math.pow(Math.pow(astddelay/factor,2)+Math.pow(dv1.stdDelay,2),0.5);
   
   
   stdVSize = Math.pow(Math.pow(avstdsize/factor,2)+Math.pow(dv1.stdVSize,2),0.5);
   stdVDelay =  Math.pow(Math.pow(avstddelay/factor,2)+Math.pow(dv1.stdVDelay,2),0.5);
   
   
 } 
  public void AddDelay(long msec, double size, long tstamp){
    double min = ((double)msec)/(1000*60);
   /* SumDelay += min - msgDelays[tsIndex%histLen];
    msgDelays[tsIndex++%histLen] = min;*/
      msgDelaysList.add(new TDelayFactor(min,size,tstamp));
    
    
  }
  public void AddVirtDelay(double min, double size, long tstamp){
    //double min = ((double)msec)/(1000*60);
    virtmsgDelaysList.add(new TDelayFactor(min,size,tstamp));
    if(size<=0){
      System.out.println("0 size being inserted");
    }
    //msgDelaysList.add(new TDelayFactor(min,100,tstamp));
    
  }
  /*public void AddDelay(Double min){
    
    SumDelay += min - msgDelays[tsIndex%histLen];
    msgDelays[tsIndex++%histLen] = min;
    
  }*/
  //public void AddDelay(double min,double size, long tstamp ){
  //    msgDelaysList.add(new TDelayFactor(min,size,tstamp));
  //}
  public void calcAvDTime(Date tstamp, int choice){
    if(msgDelaysList.size()>0){
       AvgDelay = 0.0; 
       AvgSize = 0.0;
       
      for(int i=0;i<msgDelaysList.size();i++ ){
        if(choice==0){
          if(tstamp!=null){
            if(tstamp.getTime()-msgDelaysList.get(i).tstamp.getTime()>Global.weekMSec){
              msgDelaysList.remove(i--);
              continue;
            }
          }
        }
        if(choice==1)
          msgDelaysList.get(i).tstamp.setTime(tstamp.getTime());
        
        AvgDelay += msgDelaysList.get(i).time;
        AvgSize += msgDelaysList.get(i).size;       
      }
      
      AvgDelay /= msgDelaysList.size();
      AvgSize /= msgDelaysList.size();
      stdSize = 0.0;
      stdDelay = 0.0;
      for(int i=0;i<msgDelaysList.size();i++ ){
        stdSize += Math.pow(msgDelaysList.get(i).size-AvgSize,2);
        stdDelay += Math.pow(msgDelaysList.get(i).size-AvgDelay,2);
      }
      stdDelay = Math.pow(stdDelay/msgDelaysList.size(),0.5);
      stdSize = Math.pow(stdSize/msgDelaysList.size(),0.5);
      if(msgDelaysList.size()==0){
        AvgDelay = Global.weekMin*1.0; 
        //AvgSize = 10.0;
      }
    }
    
  }
  public void calcVirAvDTime(Date tstamp, int choice){
    if(virtmsgDelaysList.size()>0){
      
        VirtAvgDelay = 0.0; 
       VirtAvgSize = 0.0;
       
      for(int i=0;i<virtmsgDelaysList.size();i++ ){
        if(tstamp!=null){
          if(tstamp.getTime()-virtmsgDelaysList.get(i).tstamp.getTime()>Global.weekMSec){
            virtmsgDelaysList.remove(i--);
            continue;
          }
        }
        if(choice==1)
          virtmsgDelaysList.get(i).tstamp.setTime(tstamp.getTime());
        
        
        VirtAvgDelay += virtmsgDelaysList.get(i).time;
        VirtAvgSize += virtmsgDelaysList.get(i).size;
        
      }
        
      VirtAvgDelay /= virtmsgDelaysList.size();
      VirtAvgSize /= virtmsgDelaysList.size();
      stdVSize = 0.0;
      stdVDelay = 0.0;
      for(int i=0;i<virtmsgDelaysList.size();i++ ){
        stdVSize += Math.pow(virtmsgDelaysList.get(i).size-VirtAvgSize,2);
        stdVDelay += Math.pow(virtmsgDelaysList.get(i).time-VirtAvgDelay,2);
      }
      stdVDelay = Math.pow(stdVDelay/virtmsgDelaysList.size(),0.5);
      stdVSize = Math.pow(stdVSize/virtmsgDelaysList.size(),0.5);
      if(virtmsgDelaysList.size()==0){
        VirtAvgDelay = Global.weekMin*1.0; 
        //VirtAvgSize = 10.0;
      }
      if(VirtAvgSize==0){
        System.out.println("VAvgsizeerror "+ virtmsgDelaysList.size());
      }
      if(VirtAvgDelay==0){
        System.out.println("VAvgdelayerror "+ virtmsgDelaysList.size());
      }
    }   
  }
  public void calcAvDTime_Lstate(Date etstamp){
    
       AvgDelay = 0.0; 
       AvgSize = 0.0;
       int count =0;
      for(int i=0;i<msgDelaysList.size();i++ ){      
          
        if(msgDelaysList.get(i).tstamp.before(etstamp) & etstamp.getTime()-msgDelaysList.get(i).tstamp.getTime()<=Global.weekMSec){
          AvgDelay += msgDelaysList.get(i).time;
          AvgSize += msgDelaysList.get(i).size;  
          count++;
        }
      }
      if(count==0){
       AvgDelay = Global.weekMin*1.0; 
        AvgSize = Global.weekMin+15.0;
        stdDelay = 1.0;
        stdSize = 1.0;
        return;
      }
      AvgDelay /= count;
      AvgSize /= count;
      stdSize = 0.0;
      stdDelay = 0.0;
      for(int i=0;i<msgDelaysList.size();i++ ){
        if(msgDelaysList.get(i).tstamp.before(etstamp)){
          stdSize += Math.pow(msgDelaysList.get(i).size-AvgSize,2);
          stdDelay += Math.pow(msgDelaysList.get(i).time-AvgDelay,2);
        }
      }
      stdDelay = Math.pow(stdDelay/count,0.5);
      stdSize = Math.pow(stdSize/count,0.5);
      if(msgDelaysList.size()==0){
        AvgDelay = Global.weekMin*1.0; 
        AvgSize = 10.0;
      }
       
  }
  public Date calcVirAvDTime_Lstate(Date etstamp){
    int count=0;
    Date lasttstamp = new Date();
     VirtAvgDelay = 0.0; 
     VirtAvgSize = 0.0;
     stdVSize = 0.0;
     stdVDelay = 0.0;
       
      for(int i=0;i<virtmsgDelaysList.size();i++ ){
       
          
        if(virtmsgDelaysList.get(i).tstamp.before(etstamp) & tstamp.getTime()-virtmsgDelaysList.get(i).tstamp.getTime()<=Global.weekMSec){
          count++;
          VirtAvgDelay += virtmsgDelaysList.get(i).time;
          VirtAvgSize += virtmsgDelaysList.get(i).size;    
          if(lasttstamp.after(virtmsgDelaysList.get(i).tstamp))
            lasttstamp.setTime(virtmsgDelaysList.get(i).tstamp.getTime());
        }      
      }
       if(count==0){
         
         return lasttstamp;
       }
         
      VirtAvgDelay /= count;
      VirtAvgSize /= count;
      for(int i=0;i<virtmsgDelaysList.size();i++ ){
        if(virtmsgDelaysList.get(i).tstamp.before(etstamp)){
          stdVSize += Math.pow(virtmsgDelaysList.get(i).size-VirtAvgSize,2);
          stdVDelay += Math.pow(virtmsgDelaysList.get(i).time-VirtAvgDelay,2);
        }
      }
      
      stdVDelay = Math.pow(stdVDelay/count,0.5);
      stdVSize = Math.pow(stdVSize/count,0.5);
      if(virtmsgDelaysList.size()==0){
        VirtAvgDelay = Global.weekMin*1.0; 
        VirtAvgSize = Global.weekMin+15.0;
        stdVDelay = 1.0;
        stdVSize = 1.0;
      }
      
      if(VirtAvgSize==0){
        System.out.println("VAvgizeerror "+ virtmsgDelaysList.size());
      }
      return lasttstamp; 
  }
  public static double Round(double Rval, int Rpl) {
    double p = (double)Math.pow(10,Rpl);
    Rval = Rval * p;
    double tmp = Math.round(Rval);
    return (double)tmp/p;
      }
  public double getPathMetric(){    
    //return VirtAvgDelay
    return ((VirtAvgDelay/VirtAvgSize)*(AvgDelay*AvgSize));//*Math.pow(stdVDelay*stdVSize+1,0.5);
    //PVector.get(pvkey2).VirtAvgDelay/PVector.get(pvkey2).VirtAvgSize)*((PVector.get(pvkey2).stdDelay+1)*PVector.get(pvkey2).AvgSize)
  }  
 /* public void calcDeficit(){
    if(depth==1){
      dAtt=  bAttempted/(double)(bAvailable);
      dDel=  bDelivered/(double)(bAvailable);
      dAss=  bAssigned/(double)(bAvailable);
      
    }
    else{
      System.err.println("Deficit error");
      System.exit(-1);
      
    }    
 }
  
 public double getstdDev(){
    double avg = SumDelay/histLen;
    double stdDev = 0;
    for(int i=0;i<histLen;i++)
      stdDev += Math.pow(msgDelays[i]-avg,2);
   
    return Math.pow(stdDev/(double)histLen, 0.5);
  }*/
  public class VAvgTimeComparator implements Comparator {

    public int compare(Object o1, Object o2) {

      return ((DVectorUnit) o1).VirtAvgDelay
          .compareTo(((DVectorUnit) o2).VirtAvgDelay);
    }
  }
}
