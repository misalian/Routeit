package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;

import java.util.Date;

import java.util.Hashtable;



public class VolumeStats {
  public Double volume;
  public Date tstamp;
  public boolean vchange;
  public boolean bwchange;
  public long consumed_bw;
  
  public Hashtable<String, Integer> msgs;
  
  
  public VolumeStats() {
     volume = new Double(0.0);
     tstamp = new Date();
     vchange = false;
     msgs = new Hashtable<String, Integer>();
     consumed_bw = 0;
     bwchange = false;
   }

}
