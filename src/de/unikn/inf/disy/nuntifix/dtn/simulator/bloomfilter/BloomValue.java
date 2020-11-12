
package de.unikn.inf.disy.nuntifix.dtn.simulator.bloomfilter;

import java.util.Date;



public class BloomValue {


  public Double proximity;

  public Integer depth;
  
  public Integer device;
  
  public Date []tstamps; // to track the timestamp of this bloomval so that old history can be avoided 
  
  public Integer tsIndex;
  
  public static Integer tsHistlen = new Integer(5);
  

  /*public BloomValue(int in, int d, double prox) {

    // TODO Auto-generated constructor stub
    index = in;
    count = d;
    proximity = prox;
  }*/

  /*public BloomValue(int in, int d,double prox) {

    // TODO Auto-generated constructor stub
    vindex = new Integer(in);

    proximity = new Double(prox);
    
    depth = new Integer(d);
  }*/
  /*public BloomValue( int d,double prox) {

    // TODO Auto-generated constructor stub
    //device =dev;
    
    proximity = new Double(prox);
    
    depth = new Integer(d);
  }*/
  public BloomValue( Integer dev,int d,double prox,Date ts) {

    // TODO Auto-generated constructor stub
   tstamps = new Date[tsHistlen];
   tsIndex = new Integer(0);
 
   tstamps[tsIndex++%tsHistlen] = new Date(ts.getTime());
    device =dev;
    
    proximity = new Double(prox);
    
    depth = new Integer(d);
  }
  public BloomValue( BloomValue bv) {

    // TODO Auto-generated constructor stub
   tstamps = new Date[tsHistlen];
   tsIndex = new Integer(bv.tsIndex);
 
   for(int i=0;i<(tsIndex>=tsHistlen ? tsHistlen:tsIndex%bv.tsHistlen);i++)
     tstamps[i] = new Date(bv.tstamps[i].getTime());
    device =bv.device;
    
    proximity = new Double(bv.proximity);
    
    depth = new Integer(bv.depth+1);
  }

}
