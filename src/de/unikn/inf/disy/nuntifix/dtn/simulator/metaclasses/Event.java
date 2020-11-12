
package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;



import java.util.LinkedList;

public class Event extends LinkedList<Msg> {

  //Hashtable<Integer, Hashtable<Integer, Hashtable<String,Msg>>> gMsgTable;

  public Event() {

    super();
    
   // gMsgTable = gm;
  }

  public void add_event(Msg mtemp) {

    int i = 0;
    while (i < super.size()) {
     Msg mtemp2 = super.get(i);
      if (!mtemp.m_interval.life_time.timemin.after(mtemp2.m_interval.life_time.timemin))
        break;
      i++;
    }
    while (i < super.size()) {
      Msg mtemp2 = super.get(i);
      if (mtemp.m_interval.life_time.timemin.before(mtemp2.m_interval.life_time.timemin))
        break;
      if(mtemp2.custodian.equals(mtemp.custodian) & mtemp.ID.equals(mtemp2.ID))
        if(mtemp2.tag>mtemp.tag)
          break;
       i++;     
    }
    super.add(i, mtemp);
  }
  public boolean remove(Msg mtemp){
    
    return super.remove(mtemp);
  }
  public boolean isFirstMsg(Msg mtemp){
    for(int i=0;i<super.size();i++){
      /*if(super.get(i).custodian.equals(mtemp.custodian))
        continue;*/
      if(super.get(i).m_interval.life_time.timemin.before(mtemp.m_interval.life_time.timemin))// & !super.get(i).custodian.equals(mtemp.custodian))
        return false;
      return true;
    }
    return true;
  }
 /* public Msg removeFirstFrag(){
    Msg mtemp = super.removeFirst();
  }*/
  public void KillAll(Msg mtemp){
    for(int i=0;i<super.size();i++){
      if(super.get(i).ID.equals(mtemp.ID)){
        super.remove(i);
        i--;
      }
    }
  }

}
