package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;


public class MsgFrag {
  public double start;
  public double size;
  public MsgFrag(double s, double b){
    start = s;
    size = b;    
  }
  public MsgFrag(MsgFrag mf){
    start = mf.start;
    size = mf.size;
  }
}
