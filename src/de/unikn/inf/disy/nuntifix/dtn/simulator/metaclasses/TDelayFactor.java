package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;

import java.util.Date;



public class TDelayFactor {

  public Double size;
  public Double time;
  public Date tstamp;
  public TDelayFactor(double t, double s, long ts){
    size = new Double(s);
    time = new Double(t);
    tstamp = new Date(ts);
  }
}
