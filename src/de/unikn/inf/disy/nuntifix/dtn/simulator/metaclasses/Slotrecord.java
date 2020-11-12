package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;


public class Slotrecord {

  public int dev1,dev2;
  public TSlot ttemp,ttemp2;
  public  Slotrecord(int d1,int d2,TSlot t1,TSlot t2){
    
    dev1=d1;
    dev2 = d2;
    ttemp = t1;
    ttemp2=t2;
  }
}
