package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.unikn.inf.disy.nuntifix.dtn.simulator.bloomfilter.*;


public class PathVector {

  HashMap<Integer,Integer> nodes;
  ArrayList<BloomValue>vector;
  
  public PathVector(){
    nodes= new HashMap<Integer, Integer>();
    vector = new ArrayList<BloomValue>(5);
  }
  public void add(Integer dev, Integer depth,Double prox, Date ts){
    nodes.put(dev, dev);
    vector.add(new BloomValue(dev,depth,prox,ts));
  }
  public boolean exist(Integer dev){
    return nodes.containsKey(dev);
  }
  public boolean updatevalue(Integer index, Integer depth, Double prox){
    if(nodes.containsKey(index)){
      for(int i=0;i<vector.size();i++)
      {
        if(vector.get(i).device.equals(index)){
          //vector.get(i)
        }
      }
    }
    return false;
  }
}
