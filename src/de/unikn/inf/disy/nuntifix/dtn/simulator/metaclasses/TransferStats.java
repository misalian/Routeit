package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;

import java.util.Date;


public class TransferStats {

  public Integer attempC;
  public Integer failureC;
  public Integer successC;
  public Date tstamp;
  
  public TransferStats(){
    attempC = new Integer(0);
    successC = new Integer(0);
    failureC = new Integer(0);
    tstamp = new Date();
    
  }
  
}
