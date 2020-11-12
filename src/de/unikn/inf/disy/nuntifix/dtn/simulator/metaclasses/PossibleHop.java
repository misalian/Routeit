package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;

public class PossibleHop {

  
  public Double value;
  //public Double expDelay;
  public Integer depth;
  //public Long tstamp;
  
  public int qindex;
  public Integer dRCount;
  public Integer index;
  
  
  public Double stdDev;
  public Double AvgDtime;
  public double AvgSize;
 
  public Double VAvgDtime;
  public double VAvgSize;
 
  public Double vStdDev;
  public int count;
  
  public Double channelFailProbab;

  public PossibleHop(int i, int q,int d, double val,double  sSize,double sVsize, double at,
      double as, double vat, double vas,float cfp) {

    index = i;
   // expDelay = new Double(v);
    qindex= q;
    dRCount = new Integer(0);
    depth = new Integer(d);
    //tstamp= new Long(t); 
    
    AvgDtime = new Double(at);
    AvgSize = new Double(as);
    VAvgDtime = vat;
    VAvgSize = vas;
    vStdDev= sVsize+10;
    stdDev = sSize+10;
    /*stdDev = new Double(1);
    vStdDev = new Double(1);*/
    value = new Double(val);  
  //  value = new Double(VAvgDtime);  
    channelFailProbab = new Double(cfp);
    
  }
  /*public PossibleHop(PossibleHop ptemp) {

    index = new Integer(ptemp.index);
    expDelay = new Double(ptemp.expDelay);
    depth = new Integer(ptemp.depth);
    tstamp= new Long(0); 
    deficit=ptemp.deficit;
    
    dRCount = new Integer(0);
  }*/
  public PossibleHop(){
    value = new Double(0);    
  }
  public PossibleHop(double metric){
    value = new Double(metric);
    count=1;
  }

}
