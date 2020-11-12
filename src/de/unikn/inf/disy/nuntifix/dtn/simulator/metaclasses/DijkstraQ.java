
package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;

import java.util.Date;
import java.util.LinkedList;

public class DijkstraQ {

  public int dev;

  public double cost;
  public double time;

  public Date T;

  public DijkstraQ from,to;

  public LinkedList<DijkstraQ> path;
  
  public long duration;
  
 // public long pduration;
  //TSlot ttemp,ttemp2;
  
  public DijkstraQ(int n) {

    dev = n;
    cost = Double.MAX_VALUE;
    time = Double.MAX_VALUE;
    T = new Date();
    duration = Long.MAX_VALUE;
    //pduration = Long.MAX_VALUE;
    path = new LinkedList<DijkstraQ>();    
  }

  public void set_path(LinkedList<DijkstraQ> p) {

    path = new LinkedList<DijkstraQ>();
    for (int i = 0; i < p.size(); i++){
      path.add(p.get(i));
      /*if(pduration>p.get(i).pduration)
        pduration=p.get(i).pduration;*/
    }
  }

  public void set_from(DijkstraQ p) {
    from = p;
  }

  /*public void set_to(DijkstraQ p) {
    to = p;
  }*/
 
}
