package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import de.unikn.inf.disy.nuntifix.dtn.simulator.Global;
import java.util.LinkedList;



import de.unikn.inf.disy.nuntifix.dtn.simulator.Global;
/*import de.unikn.inf.disy.nuntifix.dtn.simulator.bloomfilter.*;*/


public class MeetingStats {
  
  
  public int meetingcnt;

  public double waitTime;
  
  public double maxWaitTime;
  
  public double minwaitTime;

  public double duration;
  
  public double stabiltiy;
  // public double bandwidth;
  public Date lastmeeting;

  //public float[] waitingtime;

  // public Integer[] hindex;

 // public SimpleBloomFilter<Integer> sbf;

  //ThomasBloomFilter tbf;

  //public MyBloomFilter mbf;
  
  public Integer devindex;
  
  public Integer myindex;
  
  public int msgsAttempt;
  
  public int msgsTransf;
  
  //public Hashtable<Integer, NeighborQ>myNeighbor;
  
  public Hashtable<Integer,DVectorUnit> PVector;
  
  
  public HashMap<Integer, Integer>alreadyChecked;

  public HashMap<Integer, Msg> mAssigned;
     
  public Hashtable<Integer, Hashtable<String, Msg>>msgStat;
  
  public long bDelivered;
  public long bAvailable;
  public long bAssigned;
  public long bAttempted;
  boolean congested;
  public MeetingStats( Integer tindex, Integer findex) {

    lastmeeting = new Date();
    meetingcnt = 0;
    waitTime = 0;
    minwaitTime=Double.MAX_VALUE;
    stabiltiy = 1;    
    devindex = tindex;
    myindex = findex;    
    /*myNeighbor = new Hashtable<Integer, NeighborQ>();
    myNeighbor.put(devindex, new NeighborQ());*/
    
    PVector = new Hashtable<Integer, DVectorUnit>();     
    
    alreadyChecked = new HashMap<Integer, Integer>();    
   
    /*DVectorUnit dvtemp = new DVectorUnit(findex);
    for(int i=0;i<dvtemp.histLen;i++)
      dvtemp.msgDelays[i] = new Double(0);
    dvtemp.SumDelay = new Double(0);
    PVector.put(findex,dvtemp);  */
     
    mAssigned = new HashMap<Integer, Msg>();
    msgsAttempt=0;
    msgsTransf=0;
    msgStat = new Hashtable<Integer, Hashtable<String, Msg>>();
    congested = false;
    maxWaitTime = 0;
  }
  
  /*public void neighborExchange(Device dtemp2,Date tstamp,boolean dFlood){
    //if(myNeighbor.get(devindex).PVector.containsKey(devindex)){
    DVectorUnit cdvtemp = myNeighbor.get(devindex).PVector.get(devindex);
    
    cdvtemp.tstamp.setTime(tstamp.getTime());
     
    cdvtemp.calcVirAvDTime(tstamp,0);
    cdvtemp.calcAvDTime(tstamp,0);
    
    MeetingStats ms2;
    Enumeration<Integer>mskenum2 = dtemp2.meetStat.keys();
    //removeCongestion(dtemp2);
    while(mskenum2.hasMoreElements()){
      ms2 = dtemp2.meetStat.get(mskenum2.nextElement());
      if(ms2.devindex.equals(myindex))
        continue;
      if(ms2.alreadyChecked.containsKey(myindex))
        continue;
      ms2.alreadyChecked.put(myindex, devindex);
      if(ms2.congested){
        if(myNeighbor.containsKey(ms2.devindex))
          myNeighbor.remove(ms2.devindex);
        continue;
      }
      ms2.myNeighbor.get(ms2.devindex).PVector.get(ms2.devindex).calcAvDTime(tstamp,0);
      ms2.myNeighbor.get(ms2.devindex).PVector.get(ms2.devindex).calcVirAvDTime(tstamp,0);
      if(!dFlood)
        if(cdvtemp.VirtAvgDelay+ms2.myNeighbor.get(ms2.devindex).PVector.get(ms2.devindex).VirtAvgDelay>Global.dayMin*8)
          continue;
      if(myNeighbor.containsKey(ms2.devindex))
        myNeighbor.get(ms2.devindex).PVector.get(ms2.devindex).tstamp.setTime(tstamp.getTime());
      else{
        myNeighbor.put(ms2.devindex, new NeighborQ( ));
        myNeighbor.get(ms2.devindex).PVector.put(ms2.devindex, new DVectorUnit(ms2.devindex,tstamp));
        
      }     
      //myNeighbor.get(ms2.devindex).pvExchange(this,ms2,myNeighbor.get(devindex).PVector.get(devindex),dFlood, tstamp);
      myNeighbor.get(ms2.devindex).pvExchange(this,ms2,cdvtemp,dFlood, tstamp);
    }
    
  }*/
  public void mypvexchange(Device dtemp2, boolean dFlood, Date tstamp, boolean maxflow){
    
    Integer pvkey2;
    MeetingStats ms2;    
    //Hashtable<Integer, DVectorUnit>tempPVector = new Hashtable<Integer, DVectorUnit>();
     
    
    DVectorUnit cdvtemp = PVector.get(devindex);
    //cdvtemp.calcDeficit();
    cdvtemp.calcVirAvDTime(tstamp,0);
    cdvtemp.calcAvDTime(tstamp,0);
    //removeCongestion(dtemp2);
    PVector = new Hashtable<Integer, DVectorUnit>();
    PVector.put(devindex, cdvtemp);
    Hashtable<Integer,DVectorUnit>pathMetricTable = new Hashtable<Integer, DVectorUnit>();
    
    Enumeration<Integer>mskenum2 = dtemp2.meetStat.keys();
    while(mskenum2.hasMoreElements()){
      ms2=dtemp2.meetStat.get(mskenum2.nextElement());
      if(ms2.devindex.equals(myindex))
          continue;
      if(ms2.alreadyChecked.containsKey(myindex))
        continue;
      /*if(ms2.congested){
        if(PVector.containsKey(ms2.devindex))
          PVector.remove(ms2.devindex);
        continue;
      }*/
      
      ms2.alreadyChecked.put(myindex, devindex);
          
      Enumeration <Integer>pvkenum2= ms2.PVector.keys();
      while(pvkenum2.hasMoreElements()){
        pvkey2 = pvkenum2.nextElement();
        if(pvkey2.equals(devindex) | pvkey2.equals(myindex))
          continue;        
        if(!(dFlood | maxflow)&tstamp!=null)
        if(tstamp.getTime()-ms2.PVector.get(pvkey2).tstamp.getTime()>Global.weekMSec)
            continue;        
        if(pathMetricTable.containsKey(pvkey2))
          continue;
       
        if(dFlood){
          if(!PVector.containsKey(pvkey2))
            PVector.put(pvkey2, new DVectorUnit());
          continue;
         }
         else{
           DVectorUnit dvtemp;
          /*if(!pathMetricTable.containsKey(pvkey2)){
            LinkedList<DVectorUnit>commPathMetric = new LinkedList<DVectorUnit>();
            Enumeration<Integer>mskenum3 = dtemp2.meetStat.keys();
            while(mskenum3.hasMoreElements()){
              MeetingStats ms3 = dtemp2.meetStat.get(mskenum3.nextElement());
              if(ms3.PVector.containsKey(pvkey2)){            
                commPathMetric.add(ms3.PVector.get(pvkey2));
                commPathMetric.getLast().calcAvDTime(tstamp,0);
                commPathMetric.getLast().calcVirAvDTime(tstamp,0);                
              }
            }
            dvtemp = new DVectorUnit(cdvtemp,commPathMetric,tstamp, maxflow);
            pathMetricTable.put(pvkey2,dvtemp);
          }*/
          dvtemp = new DVectorUnit(cdvtemp,ms2.PVector.get(pvkey2),tstamp,maxflow);
          //dvtemp = pathMetricTable.get(pvkey2);
          if(maxflow){
            if(dvtemp.VirtAvgDelay>Global.dayMin*14 )
              continue;
          }else
            if(dvtemp.VirtAvgDelay>Global.dayMin*7 )
              continue;
          
         /* if(dvtemp.VirtAvgDelay==0)
            pvkey2 = pvkey2 +1 -1;*/
          if(PVector.containsKey(pvkey2))   {        
            if(PVector.get(pvkey2).getPathMetric()<dvtemp.getPathMetric())             
              continue;
          }
            
          PVector.put(pvkey2, dvtemp);
          alreadyChecked = new HashMap<Integer, Integer>();
        }
      }     
    } 
    //PVector = tempPVector;
    //removeInvalidPaths(dtemp2);
  }
  public void removeCongestion(Device dtemp){
    int msglistSize = dtemp.aliveMsglist.size();
    for(int j=0;j<msglistSize;j++){
      Msg mtemp = dtemp.aliveMsglist.get(j);
      if(!mtemp.alive)
        continue;
      Enumeration<Integer> nHopenum = mtemp.mynhoplist.keys();
      while(nHopenum.hasMoreElements()){
        int nHop = nHopenum.nextElement();
        if(!dtemp.meetStat.containsKey(nHop))
          continue;
        if(dtemp.meetStat.get(nHop).msgStat.containsKey(mtemp.ID)){
          if(dtemp.meetStat.get(nHop).msgStat.get(mtemp.ID).containsKey(mtemp.frag_id))
              continue;
        }
        double burden= mtemp.size;
        for(int k=j+1;k<msglistSize;k++){
          Msg mtemp2 = dtemp.aliveMsglist.get(k);
          if(!mtemp2.alive)
            continue;
          if(!mtemp2.mynhoplist.containsKey(nHop))
            continue;
          
          if(dtemp.meetStat.get(nHop).msgStat.containsKey(mtemp2.ID)){
            if(dtemp.meetStat.get(nHop).msgStat.get(mtemp2.ID).containsKey(mtemp2.frag_id))
                continue;
          }
          burden += mtemp2.size;            
        }
        //if(burden/3>dtemp.meetStat.get(nHop).myNeighbor.get(devindex).PVector.get(devindex).VirtAvgSize){
        if(burden/3>dtemp.meetStat.get(nHop).PVector.get(devindex).VirtAvgSize){
          dtemp.meetStat.get(nHop).congested = true;
          //dtemp.meetStat.remove(nHop);
          //Peerstable.get(tabdevList.get(nHop)).meetStat.remove(tabdevList.indexOf(dtemp.mac));
        }
        else
          dtemp.meetStat.get(nHop).congested = false;
      }
    }
  }

  public void removeInvalidPaths(Device  dtemp2){
   /* Enumeration<Integer>nhenum = myNeighbor.keys();
    //for propogation of removal of paths... 
    while(nhenum.hasMoreElements()){
      Integer pvkey = nhenum.nextElement();
      if(pvkey.equals(devindex))
        continue;
      Enumeration<Integer>mskenum2 = dtemp2.meetStat.keys();
      boolean found = false;
      while(mskenum2.hasMoreElements()){
        if(dtemp2.meetStat.get(mskenum2.nextElement()).myNeighbor.containsKey(pvkey)){
          found = true;
          break;
        }
      }
      if(!found)
        myNeighbor.remove(pvkey);
    }   */
    Enumeration<Integer>nhenum = PVector.keys();
    //for propogation of removal of paths... 
    while(nhenum.hasMoreElements()){
      Integer pvkey = nhenum.nextElement();
      if(pvkey.equals(devindex))
        continue;
      Enumeration<Integer>mskenum2 = dtemp2.meetStat.keys();
      boolean found = false;
      while(mskenum2.hasMoreElements()){
        if(dtemp2.meetStat.get(mskenum2.nextElement()).PVector.containsKey(pvkey)){
          found = true;
          break;
        }
      }
      if(!found)
        PVector.remove(pvkey);
    }  
  }

 /* public void mypvInsertVal(MeetingStats ms,int c,Double tmeetingCount,Date ts){
    BloomValue bvtemp;
    Double proximity;
    proximity = getBvalue(c, tmeetingCount);
    //adding proximity value for immediate neighbours
    if(PVector.containsKey(devindex)){
     
      bvtemp=PVector.get(devindex);
      if(bvtemp.proximity/(float)bvtemp.depth<= proximity){
        bvtemp.proximity = proximity;
        bvtemp.depth=1;            
      }
      bvtemp.tstamps[bvtemp.tsIndex++%bvtemp.tsHistlen] = new Date(ts.getTime());
    }
    else{
      bvtemp = new BloomValue(devindex,1,proximity,ts); 
      PVector.put(devindex, bvtemp);
    }  
  }
  */
  /*public void removeoldData(Date tstamp){
   
    Enumeration<Integer>pvkenum = PVector.keys();
    while(pvkenum.hasMoreElements()){
      int pvkey =pvkenum.nextElement();
        if(PVector.get(pvkey).tstamp.getTime()-tstamp.getTime()>Global.weekMSec)
          PVector.remove(pvkey);
    }
  }*/
  
  
  public void myMsgStatsExchange(Device dtemp, Device dtemp2, int msgtrack){
    MeetingStats ms2;
    Msg mtemp;
    Enumeration <Integer>msgenum = msgStat.keys();
    while(msgenum.hasMoreElements()){
      Integer mid = msgenum.nextElement();
      Enumeration <String>fid =msgStat.get(mid).keys();
      
      while(fid.hasMoreElements()){
        mtemp = msgStat.get(mid).get(fid.nextElement());
        if(mtemp.delivered)
          continue;
        
        Enumeration<Integer>mskenum2 = dtemp2.meetStat.keys();
        while(mskenum2.hasMoreElements()){
          ms2=dtemp2.meetStat.get(mskenum2.nextElement());
        
          if(!ms2.msgStat.containsKey(mtemp.ID))
            continue;
          if(!ms2.msgStat.get(mtemp.ID).containsKey(mtemp.frag_id))
            continue;
          msgStat.get(mid).put(mtemp.frag_id, mtemp);
          if(mtemp.ID.equals(msgtrack))
            mtemp.cNode.addEdge(new NodeList(dtemp2.mac,mtemp.m_interval.life_time.timemin,3));  
          
          if(ms2.msgStat.get(mtemp.ID).get(mtemp.frag_id).delivered){
            mtemp.delivered=true;
            mtemp.m_interval.life_time.timemax.setTime(mtemp.m_interval.life_time.timemin.getTime()-5);
            //mtemp.m_interval.life_time.timemin.setTime(itime.getTime());
            if(mtemp.ID.equals(msgtrack)){
              ms2.msgStat.get(mtemp.ID).get(mtemp.frag_id).cNode.addEdge(new NodeList(dtemp.mac,mtemp.m_interval.life_time.timemin,4));
            }            
          }
          break;
        }     
      }     
    }
  }
  
  /*public void sInsertval(Device dtemp2) {

    Enumeration<Integer> denum2 = dtemp2.meetStat.keys();
    MeetingStats ms2;
    Integer key;
    while (denum2.hasMoreElements()){
      key =denum2.nextElement();
      ms2=dtemp2.meetStat.get(key);
      
      this.sbf.add( key);  
      sbf.bitSet.or(ms2.sbf.bitSet);         
    }    
  }
  

  /*public void tinsertval(Device dtemp2, Hash hfun, LinkedList tabdevList,
      Device dtemp) {

    Enumeration denum2 = dtemp2.mstat.keys();
    MeetingStats ms2;
    Integer i;
    Double val;
    if (dtemp.indsize > 3000)
      System.out.println();
    double total = normalisingfact(dtemp2);
    while (denum2.hasMoreElements()) {
      i = (Integer) denum2.nextElement();
      ms2 = dtemp2.mstat.get(i);

      Integer[] index = hfun.hash(i);
      val = ms2.meetingcnt / total;

      for (int j = 0; j < index.length; j++) {
        if ((Double) tbf.get(index[j]) < val) {
          if ((Double) tbf.get(index[j]) == 0)
            dtemp.indexes[dtemp.indsize++] = index[j];
          tbf.put(new Integer(index[j]), new Double(val));

        }
      }
    }
  }

  /*public void mylinsertval(Device dtemp2, Hash hfun, LinkedList tabdevList,
      Device dtemp) {

    Enumeration denum2 = dtemp2.mstat.keys();
    MeetingStats ms2;
    Integer i;
    Double val;
    //double total = normalisingfact(dtemp2);
    while (denum2.hasMoreElements()) {
      i = (Integer) denum2.nextElement();
      ms2 = dtemp2.mstat.get(i);
      Integer[] index = hfun.hash(i);
      val = getBvalue(c, tmeetingcount);

      for (int j = 0; j < index.length; j++) {
        if ((Double) tbf.get(index[j]) < val) {
          //mbf.lput(new Integer(index[j]), 1, new Double(val));

        }
      }
    }
  }

  /*public void myvinsertval(MeetingStats ms2, Hash hfun) {

    //Enumeration <Integer>denum2 = dtemp2.mstat.keys();
    //MeetingStats ms2;
    Integer i;
    Double val;
    //ms2 = dtemp2.mstat.get(new Integer(devindex));
     //double total = normalisingfact(dtemp2);
    //System.out.println(dtemp2.mstat.size());
    //while (denum2.hasMoreElements()) {
     //i =  denum2.nextElement();
      //ms2 = dtemp2.mstat.get(new Integer(devindex));
     //  val = ms2.meetingcnt/total;
       
      val = ms2.getBvalue();
      
      //if(val==0)
        //continue;
     
      for (int j = 0; j < hindex.length; j++) {
        int rindex = mbf.vgetrindex(hindex[j]);
        if (rindex == -1)
          mbf.vput(new Integer(hindex[j]),1, new Double(val));
        else if ((Double) mbf.vgetval(rindex) < val)
          mbf.vreplace(rindex,1, val);
      }
    //}
  }*/
  /*public void myhinsertval(MeetingStats ms, int c, Double tmeetingCount){
    Double val;
    val = ms.getBvalue(c, tmeetingCount);
    BloomValue bvtemp; 
    
    BloomValue bvtemp2 = new BloomValue(devindex,1,val, new Date());
     
    for (int j = 0; j < hindex.length; j++) {
        
        bvtemp = mbf.hget(hindex[j]);
        if (bvtemp==null)
          mbf.hput(hindex[j],bvtemp2);
        else if ((Double) mbf.hgetval(hindex[j]) < val)
          mbf.hreplace(hindex[j],bvtemp);
      }
  }*/

 /* public void tmerg(MeetingStats ms2, Device dtemp, Device dtemp2) {

    /*
     * for(int i=0;i<mbf.length();i++){ if((Double)mbf.get(i)<(Double)ms2.mbf.get(i))
     * mbf.put(new Integer(i), (Double)ms2.mbf.get(i));
     * if((Double)mbf.get(i)>(Double)ms2.mbf.get(i)) ms2.mbf.put(new Integer(i),
     * (Double)mbf.get(i)); }
     */

    /*for (int i = 0; i < dtemp2.indsize; i++) {
      if ((Double) tbf.get(dtemp2.indexes[i]) < (Double) ms2.tbf
          .get(dtemp2.indexes[i]))
        tbf.put(new Integer(dtemp2.indexes[i]), (Double) ms2.tbf
            .get(dtemp2.indexes[i]));
    }
    for (int i = 0; i < dtemp.indsize; i++) {
      if ((Double) tbf.get(dtemp.indexes[i]) > (Double) ms2.tbf
          .get(dtemp.indexes[i]))
        ms2.tbf.put(new Integer(dtemp.indexes[i]), (Double) ms2.tbf
            .get(dtemp.indexes[i]));
    }
  }*/

  /*public void myvmerg(MeetingStats ms2) {

    int vlen = ms2.mbf.vlength();  
    for (int i = 0; i < vlen; i++) {
      int index = mbf.vgetindex(ms2.mbf.vbloom.get(i).index);
      if (index == -1)
        mbf.vput(ms2.mbf.vbloom.get(i).index, ms2.mbf.vget(i));
      else {
        if (mbf.vget(index) < ms2.mbf.vget(i))
          mbf.vreplace(index, ms2.mbf.vget(i));
        else
          ms2.mbf.vreplace(i, mbf.vget(index));
      }
    }
    vlen = mbf.vlength();
    for (int i = 0; i < vlen; i++) {
      int index = ms2.mbf.vgetindex(mbf.vbloom.get(i).index);
      if (index == -1)
        ms2.mbf.vput(mbf.vbloom.get(i).index, mbf.vget(i));

    }

  }*/

  /*public void mylmerg(MeetingStats ms2, Device dtemp, Device dtemp2) {

    for (int i = 0, linkindex2 = 0; i < ms2.mbf.llength(); i = ms2.mbf.bloom
        .get(++linkindex2).index) {
      int linkindex = mbf.lgetlinkindex(i);
      if ((Double) mbf.lget(linkindex) < (Double) ms2.mbf.lget(mbf
          .lgetlinkindex(i))) {
        int count = ms2.mbf.bloom.get(linkindex2).count;
        int cur_count;
        do {
          cur_count = mbf.bloom.get(linkindex + 1).index
              - ms2.mbf.bloom.get(linkindex2).index;
          mbf.lput(new Integer(i), cur_count, (Double) ms2.mbf.lget(i));
          linkindex++;
          count -= cur_count;
          i += cur_count;
        } while (count > 0);
      }
    }
    for (int i = 0, linkindex = 0; i < mbf.llength(); i = mbf.bloom
        .get(++linkindex).index) {
      int linkindex2 = ms2.mbf.lgetlinkindex(i);
      if ((Double) mbf.lget(linkindex) > (Double) ms2.mbf.lget(mbf
          .lgetlinkindex(i))) {
        int count = mbf.bloom.get(linkindex2).count;
        int cur_count;
        do {
          cur_count = ms2.mbf.bloom.get(linkindex2 + 1).index
              - mbf.bloom.get(linkindex).index;
          ms2.mbf.lput(new Integer(i), cur_count, (Double) ms2.mbf.lget(i));
          linkindex2++;
          count -= cur_count;
          i += cur_count;
        } while (count > 0);
      }
    }

  }*/

 /* public double getBvalue(int c, Double tmeetingcount) {
    double avgwtime = waitTime / meetingcnt;

    if (avgwtime > 8000)
      return 0;
    if (meetingcnt == 1)
      avgwtime = 8000;
    switch(c){
      case 0:   
        
        return (1 / (double) Math.log(avgwtime ))
            * Math.log((meetingcnt + 1.718281828459045));//*Math.log(duration);
      case 1:
        
        return (1 / (double) Math.log(avgwtime ))
        * Math.log((meetingcnt + 1.718281828459045))*duration;
    }
    
    return meetingcnt/(double)tmeetingcount;    
  }
*/
  

}
