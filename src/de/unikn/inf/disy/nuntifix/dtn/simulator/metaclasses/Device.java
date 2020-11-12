
package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;


import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

//import de.unikn.inf.disy.nuntifix.dtn.simulator.bloomfilter.BloomValue;

public class Device {

  public String mac;

  public Date starttime;

 public double tonlinetime;

  public double[] probab;

  public double approbab[];

  public int homeap;

  public Hashtable<String, Apoint> Aptable;

 // public long avgOfLinetime;
  
  public boolean active_slots[];

  public  LinkedList<TSlot> sorted_slots;

  //public  double bytestransf;

 // public int knowncontacts;

  public int meetingcount[];

 // public Integer[] indexes;

 // public int indsize;
  
  public int totalmeetcount;
  
  public Hashtable<Integer, MeetingStats> meetStat;
   
  public LinkedList<Msg> aliveMsglist;

  public NodeList cNode;
  
 public double hbytes;
  
  public double rbytes;
  
  public double dbytes;
  public int dmsgs;
  
  public double tbytes;
  
  public boolean src;
  
  public boolean dest;
  public int channelFail;
  
  public double centrality[];
  
  public int contactperTimeslot[];

  public Device(Device d) {

    mac = new String(d.mac);
    tonlinetime = 0;
    homeap = -1;
    sorted_slots = new LinkedList<TSlot>();
    starttime = new Date();
    Aptable = new Hashtable<String, Apoint>();
    Enumeration apenum = d.Aptable.keys();
    String apname;
    while (apenum.hasMoreElements()) {
      apname = new String(apenum.nextElement().toString());
      Aptable.put(apname, new Apoint(d.Aptable.get(apname)));
    }
    
    aliveMsglist = new LinkedList<Msg>();
    
    hbytes = rbytes = dbytes = tbytes = dmsgs=1;  
    meetStat = new Hashtable<Integer, MeetingStats>(); 
    centrality = new double[18];
    channelFail = 0;
    
   // sleepMsgList = new LinkedList<Msg>();

  }

  public Device(String dname) {

    mac = new String(dname);
    tonlinetime = 0;
    homeap = -1;
    sorted_slots = new LinkedList<TSlot>();
    starttime = new Date();
    Aptable = new Hashtable<String, Apoint>();
    aliveMsglist = new LinkedList<Msg>();
    hbytes = rbytes = dbytes = tbytes = dmsgs=1;
    
    meetStat = new Hashtable<Integer, MeetingStats>();
    centrality = new double[18];
    channelFail = 0;


    //sleepMsgList = new LinkedList<Msg>();
  }

  // wehshi sorting method.. sorts the the elemt whpo have hopcount <threshhold
  // according to hopcount and rest accorsng to probab of delivery.
  public boolean ifalivemorefrags(Msg mtemp){
    
    for(int i=0;i<aliveMsglist.size();i++){
      if(aliveMsglist.get(i).ID.equals(mtemp.ID) & !aliveMsglist.get(i).frag_id.equals(mtemp.frag_id))
          return true;
    }
    return false;
  }
 /* public boolean ifsleepmorefrags(Msg mtemp){
    for(int i=0;i<sleepMsgList.size();i++){
      if(sleepMsgList.get(i).ID.equals(mtemp.ID))
        return true;
    }
    return false;
    
  }*/
  
  public void sortmsglist(int b_thresh ) {

    Msg mtemp,mtemp2;

    for (int k = 0; k < aliveMsglist.size(); k++) {
      mtemp = aliveMsglist.get(k);
      if (mtemp.hoplist.size() - 1 <= b_thresh) {
        for (int i = 0; i < aliveMsglist.size(); i++) {
          if(i==k)
            continue;
          mtemp2 = aliveMsglist.get(i);
          if ( mtemp2.hoplist.size() < mtemp.hoplist.size()) {
            aliveMsglist.set(k, mtemp2);
            aliveMsglist.set(i, mtemp);

          }
        }
      } else {
        for (int i = 0; i < aliveMsglist.size(); i++) {
          mtemp2 = aliveMsglist.get(i);
          if (  mtemp2.hoplist.size() - 1 > b_thresh) {
            if (probab[mtemp.dest] < probab[ mtemp2.dest]) {
              aliveMsglist.set(k, mtemp2);
              aliveMsglist.set(i, mtemp);
            }
          }
        }
      }
    }
  }
  public Msg getFirstMsg1(Msg mtemp, int dev, Event eventQ){
    Msg mtemp2;
   
    for(int i=0;i<aliveMsglist.size();i++){
      mtemp2 = aliveMsglist.get(i);
      if(mtemp.equals(mtemp2)  )
        continue;
      if(mtemp2.alive & mtemp2.dest==dev){
        if(!mtemp2.m_interval.life_time.timemin.after(mtemp.m_interval.life_time.timemin)){            
            swap(eventQ, mtemp, mtemp2)   ;   
            return mtemp2;
          }
      }
    }
    for(int i=0;i<aliveMsglist.size();i++){
      mtemp2 = aliveMsglist.get(i);
      if(!mtemp2.replicate)
        continue;
      if(mtemp.ID.equals(mtemp2.ID) & mtemp2.alive & mtemp2.tag<mtemp.tag  ){
        if(!mtemp2.m_interval.life_time.timemin.after(mtemp.m_interval.life_time.timemin))
        {
         // mtemp2.m_interval.life_time.timemin.setTime(mtemp.m_interval.life_time.timemin.getTime());
          swap(eventQ, mtemp, mtemp2)   ;   
          mtemp2.mynhoplist=mtemp.mynhoplist;
          return mtemp2;
        }
      }
    }
    for(int i=0;i<aliveMsglist.size();i++){
      mtemp2 = aliveMsglist.get(i);
      if(!mtemp2.replicate)
        continue;
      if(mtemp.ID.equals(mtemp2.ID) & mtemp2.alive & mtemp2.tag2<mtemp.tag2  ){
        if(!mtemp2.m_interval.life_time.timemin.after(mtemp.m_interval.life_time.timemin)){
          swap(eventQ, mtemp, mtemp2)   ;   
          mtemp2.mynhoplist=mtemp.mynhoplist;
          return mtemp2;
        }
      }
    }
    
   
    long remtime = mtemp.m_interval.life_time.timemax.getTime()-mtemp.m_interval.life_time.timemin.getTime();
    for(int i=0;i<aliveMsglist.size();i++){
      mtemp2 = aliveMsglist.get(i);
      if(mtemp.equals(mtemp2)  )
        continue;
      if(!mtemp2.mynhoplist.containsKey(dev))
        continue;
      if(mtemp2.alive & !mtemp2.m_interval.life_time.timemin.after(mtemp.m_interval.life_time.timemin)){
        if(mtemp2.m_interval.life_time.timemax.getTime()-mtemp2.m_interval.life_time.timemin.getTime()<remtime){
            swap(eventQ, mtemp, mtemp2)   ;       
            return mtemp2;
         }
      }
    }
   /* for(int i=0;i<aliveMsglist.size();i++){
      mtemp2 = aliveMsglist.get(i);
      if(mtemp.ID.equals(mtemp2.ID) & mtemp2.alive & mtemp2.tag<mtemp.tag  ){
        if(!mtemp2.m_interval.life_time.timemin.after(mtemp.m_interval.life_time.timemin)){
          eventQ.remove(mtemp2);
          eventQ.add_event(mtemp);
          mtemp2.mynhoplist=mtemp.mynhoplist;
          return mtemp2;
        }
      }
    }*/
    
   
    return mtemp;
  }
  public void swap(Event eventQ, Msg mtemp, Msg mtemp2){
    if(!eventQ.remove(mtemp2))
      System.err.print("swap error");
    eventQ.add_event(mtemp);
    
  }
  /*public void addBannedhops(Msg mtemp,int dev)
  {
   for(int i=0;i<aliveMsglist.size();i++){
     if(!aliveMsglist.get(i).ID.equals(mtemp.ID))
       continue;
     aliveMsglist.get(i).bannedHops.add(dev);
     aliveMsglist.get(i).mynhoplist.remove(dev);    
   }
  }*/
  public void sortmsglist1() {

    Msg mtemp, mtemp2;

    for (int k = 0; k < aliveMsglist.size(); k++) {
      mtemp = aliveMsglist.get(k);
      for (int i = k+1; i < aliveMsglist.size(); i++) {
        mtemp2 = aliveMsglist.get(i);
       
       /* if ((mtemp.size / mtemp.nh) > (mtemp2.size / mtemp2.nh)) {
          aliveMsglist.set(k, mtemp2);
          aliveMsglist.set(i, mtemp);
          k--;
          break;
        }*/
      }
    }
  }

  public int find_slot(Date dtemp) {

    for (int i = 0; i < sorted_slots.size(); i++) {
      if (sorted_slots.get(i).timemax.compareTo(dtemp) > 0& sorted_slots.get(i).timemin.before(sorted_slots.get(i).timemax))
        return i;
    }
    return sorted_slots.size();
  }

  public int find_slot(Date dtemp, int low, int high) {

    if (high < 0)
      return -1;
    if (dtemp.after(sorted_slots.getLast().timemax))
      return -1;

    if (dtemp.before(sorted_slots.getFirst().timemin))
      return 0;

    int p = (high + low) / 2;
    TSlot ttemp = sorted_slots.get(p);

    if (ttemp.timemin.compareTo(dtemp) <= 0 & ttemp.timemax.after(dtemp)& ttemp.timemin.before(ttemp.timemax))
      return p;
    if (high <= low + 1) {
      return high;
    }

    if (dtemp.before(ttemp.timemax))
      return find_slot(dtemp, low, p);
    return find_slot(dtemp, p, high);
  }

  public int if_repmsgexist(Msg mtemp) {

    int i;
    for ( i = 0; i < aliveMsglist.size(); i++) {
      if (aliveMsglist.get(i).ID.equals(mtemp.ID) & aliveMsglist.get(i).frag_id.equals(mtemp.frag_id))
        return i;
    }
    
    return -1;
  }
  /*public int if_repmsgexistSleep(Msg mtemp) {

    int i;
    for (i=0; i < sleepMsgList.size(); i++) {
      if (sleepMsgList.get(i).ID.equals(mtemp.ID) & sleepMsgList.get(i).frag_id.equals(mtemp.frag_id))
        return i;
    }
    
    return -1;
  }*/



  public int msgcount(Msg mtemp ){

    int count = 0;
    for (int i = 0; i < aliveMsglist.size(); i++){
      if(aliveMsglist.get(i).ID.equals(mtemp.ID))
        if(aliveMsglist.get(i).frag_id.equals(mtemp.frag_id))
        count++;
    }
    return count;
  }

  public int msgcountec(Msg mtemp) {

    int count = 0;
    for (int i = 0; i < aliveMsglist.size(); i++){
      if(aliveMsglist.get(i).ID.equals(mtemp.ID))
        count++;
    }
    return count;
  }
  
  public void assignNexthop(int count,int nexthop,Msg mtemp, double probab){
    
    for(int i=0;i<aliveMsglist.size();i++) {
      if(aliveMsglist.get(i).alive & aliveMsglist.get(i).ID.equals(mtemp.ID)
          &  aliveMsglist.get(i).value<probab){
        aliveMsglist.get(i).nhoplist = new LinkedList<Integer>();        
        aliveMsglist.get(i).nhoplist.addFirst(nexthop);
        aliveMsglist.get(i).value=probab;
        count--;
        if(count==0)
          break;
      }
    }
  }

  public Msg get_ECmsg(Msg mtemp ) {

    
    for (int i = 0; i < aliveMsglist.size(); i++) {
      if(aliveMsglist.get(i).alive & aliveMsglist.get(i).ID.equals(mtemp.ID)
          & !aliveMsglist.get(i).frag_id.equals(mtemp.frag_id))
        return aliveMsglist.get(i);
    }
    return null;
  }

  public TSlot getnextslot(TSlot cslot) {

    Iterator apit = Aptable.values().iterator();
    long diff = Long.MAX_VALUE;

    TSlot fslot = cslot;
    Apoint aptemp;
    TSlot ttemp;
    while (apit.hasNext()) {
      aptemp = ((Apoint) apit.next());
      for(int i=0;i< aptemp.contacttimeList.size();i++)
      {
        ttemp = aptemp.contacttimeList.get(i);
      //  if(mac.equals("7ef7e22370fed14108d954366a4440f2ffd06bf2"))
     //     System.out.println(ttemp.timemin.toString()+"\t"+ttemp.timemax.toString()+"\t"+ttemp.apname);
        
        if (ttemp.timemin.compareTo(cslot.timemin) <= 0)
          continue;

        if(!ttemp.timemin.before(cslot.timemin)& !ttemp.timemax.after(cslot.timemax)){
          Aptable.get(ttemp.apname).contacttimeList.remove(ttemp);
          i--;
          continue;
        }
        if (ttemp.timemin.getTime() - cslot.timemin.getTime() < diff
            & ttemp.timemin.before(ttemp.timemax)) {
          diff = ttemp.timemin.getTime() - cslot.timemin.getTime();
          fslot = ttemp;
          // fslot.apname = aptemp.Apointname;
         // break;
        }
      }
    }
    return fslot;
  }
  public double getByteSize(){
    double size = 0;
    for(int i=0;i<aliveMsglist.size();i++)
      size += aliveMsglist.get(i).size;
    return size;
  }
  public int getmeetingcount() {

    int count = 0;
    for (int i = 0; i < meetingcount.length; i++)
      count += meetingcount[i];
    return count;
  }

  public void create_sortedslots() {

    TSlot ttemp, fslot = new TSlot(0, 0, "");
    Iterator apit = Aptable.values().iterator();
    sorted_slots = new LinkedList<TSlot>();
    Apoint aptemp;
    while (apit.hasNext()) {
      aptemp = ((Apoint) apit.next());
      for (int i = 0; i < aptemp.contacttimeList.size(); i++) {
        ttemp = aptemp.contacttimeList.get(i);
        if (ttemp.timemin.before(starttime)) {
          if (ttemp.timemin.before(ttemp.timemax)) {
            starttime.setTime(ttemp.timemin.getTime());
            fslot = ttemp;
          } else
            aptemp.contacttimeList.remove(ttemp);
        }
      }
    }
    ttemp = fslot;
    starttime.setTime(ttemp.timemin.getTime());
   // if(mac.equals("7ef7e22370fed14108d954366a4440f2ffd06bf2"))
  //    System.out.println("G "+ttemp.timemin.toString()+"\t"+ttemp.timemax.toString()+"\t"+ttemp.apname);
    if (ttemp.apname.length() == 0)
      return;
    //avgOfLinetime=0;
    do {
      sorted_slots.add(ttemp);

      
     // long endtime = ttemp.timemax.getTime();
      ttemp = getnextslot(sorted_slots.getLast());
  //    if(mac.equals("7ef7e22370fed14108d954366a4440f2ffd06bf2"))
   //     System.out.println("G "+ttemp.timemin.toString()+"\t"+ttemp.timemax.toString()+"\t"+ttemp.apname);
    
     // avgOfLinetime+=ttemp.timemin.getTime()-endtime;
    } while (ttemp != sorted_slots.getLast());
    /*if(sorted_slots.size()>1)
      avgOfLinetime/=sorted_slots.size()-1;*/
    checkoverlap();
    //verify();
    }
  public void verify(){
    TSlot ttemp;
  
    for (int i = 0; i < sorted_slots.size() - 1; i++) {
      ttemp = sorted_slots.get(i);
      if(!ttemp.timemin.before(sorted_slots.get(i+1).timemin))
        System.out.println("ooops");
      if(!ttemp.timemax.before(sorted_slots.get(i+1).timemin))
        System.out.println(mac+" "+i);
      if(!ttemp.timemin.before(ttemp.timemax))
        System.out.println(mac+" k "+i);
    }
  }
  public void checkoverlap() {

    TSlot ttemp, fslot;
    for (int i = 0; i < sorted_slots.size() - 1; i++) {
      ttemp = sorted_slots.get(i);
      fslot = sorted_slots.get(i + 1);
      if(!ttemp.timemin.before(ttemp.timemax)){
        Aptable.get(ttemp.apname).contacttimeList.remove(ttemp);
        sorted_slots.remove(ttemp);
        i--;
        continue;
      }

     /* if(ttemp.apname.equals("LBldg6fb63d6b1a9e03cebaed12de261f61a382b950f0")
          &
          mac.equals("7ef7e22370fed14108d954366a4440f2ffd06bf2")){
        System.out.println(ttemp.timemin.toString()+" "+ttemp.timemax.toString()+" "+ttemp.apname);
        System.out.println(fslot.timemin.toString()+" "+fslot.timemax.toString()+" "+fslot.apname);
      }
      /*if(mac.equals("48cc7f5c33c2254d8ad420fd4e7884e379069e9c"))
        if(i>120){
          System.out.print(ttemp.timemin.toString()+" "+ttemp.timemax.toString()+" ");
          System.out.println(fslot.timemin.toString()+" "+fslot.timemax.toString());
        }*/
      if (ttemp.isoverlap(fslot)){
        if(ttemp.apname.equals(fslot.apname)){
          ttemp.timemax.setTime(fslot.timemax.getTime());
          Aptable.get(fslot.apname).contacttimeList.remove(fslot);
          sorted_slots.remove(fslot);
          i--;
        }   
        //reduce the bigger slot
        else if (ttemp.timemax.after(fslot.timemax) )
            {              
              sorted_slots.remove(fslot);
              Aptable.get(fslot.apname).contacttimeList.remove(fslot);
              i--;
            }
        else if (ttemp.timemin.after(fslot.timemin))
            {
              sorted_slots.remove(ttemp);
              Aptable.get(fslot.apname).contacttimeList.remove(fslot);
              i--;
            }
       /* else if( fslot.timemax.getTime()-fslot.timemin.getTime()>ttemp.timemax.getTime()-ttemp.timemin.getTime())
          fslot.timemin.setTime(ttemp.timemax.getTime()+1000);
        else*/
          ttemp.timemax.setTime(fslot.timemin.getTime()-1000);       
          if(!ttemp.timemin.before(ttemp.timemax)){
            sorted_slots.remove(ttemp);
            Aptable.get(fslot.apname).contacttimeList.remove(fslot);
            i--;            
          }
        
      }
      else if(ttemp.timemax.getTime()+2000>=fslot.timemin.getTime())
      {
        if(ttemp.apname.equals(fslot.apname)){
          ttemp.timemax.setTime(fslot.timemax.getTime());
          Aptable.get(fslot.apname).contacttimeList.remove(fslot);
          sorted_slots.remove(fslot);
          i--;
        }   
      }
    /*  if(mac.equals("48cc7f5c33c2254d8ad420fd4e7884e379069e9c"))
        if(i>120){
          System.out.print(ttemp.timemin.toString()+" "+ttemp.timemax.toString()+" ");
          System.out.println(fslot.timemin.toString()+" "+fslot.timemax.toString());
        }
    /*  if(ttemp.apname.equals("LBldg9d727086449f16390bfa38e108e12f789d434aee")
          &
          mac.equals("9731f6bac22a836813b8fd4698a9e9e976204af6")){
        System.out.println(ttemp.timemin.toString()+" "+ttemp.timemax.toString()+" "+ttemp.apname);
        System.out.println(fslot.timemin.toString()+" "+fslot.timemax.toString()+" "+fslot.apname);
      }*/
    }
  }
  public boolean appendMsg(Msg mtemp){
    int i=0;
    for( i=0;i<aliveMsglist.size();i++){
      if(aliveMsglist.get(i).alive & aliveMsglist.get(i).ID.equals(mtemp.ID) & aliveMsglist.get(i).frag_id.equals(mtemp.frag_id )){
        aliveMsglist.get(i).size+=mtemp.validSize;
        aliveMsglist.get(i).hoplist.addAll(mtemp.hoplist);
         return true;
      }        
    }
    return false;
  }
  public boolean ifcompleteMsg(Msg mtemp){
    for(int i=0;i<aliveMsglist.size();i++){
      if(aliveMsglist.get(i).alive & aliveMsglist.get(i).ID.equals(mtemp.ID) & aliveMsglist.get(i).frag_id.equals(mtemp.frag_id )){
        if(aliveMsglist.get(i).size>=aliveMsglist.get(i).realSize)
          return true;
        else
          return false;
      }
    }
    return false;
  }
  public boolean  encode(Msg mtemp){
    Msg mtemp2,mtemp3;
    
    //looking which frag has been already there in encoded form and how many times 
    for(int i=0;i<aliveMsglist.size();i++){
      mtemp2 = aliveMsglist.get(i);
      if(mtemp2.cFrags.length<2 | !mtemp2.ID.equals(mtemp.ID))
        continue;      
      for(int j=0;j<mtemp2.cFrags.length;j++){
        for(int k=0;k<aliveMsglist.size();k++){
          mtemp3 = aliveMsglist.get(k);
          if(!mtemp3.ID.equals(mtemp.ID))
            continue;
          if(mtemp3.frag_id.equals(mtemp2.cFrags[j])){
            mtemp3.codeCount++;
            break;
          }          
        }
      }         
    }
    
    int codecount=2;
    String fragid = new String("");
    if(msgcountec(mtemp)>mtemp.frag_count/2)
      codecount++;
    mtemp.cFrags= new String[codecount];
    int count=0;
    //looking for approprate frags to be encoded.. i.e those who have not been encoded more than designated no of times
    for(int j=0;j<aliveMsglist.size()*2;j++){
      
      mtemp2 = aliveMsglist.get((int)(Math.random()*1000)%aliveMsglist.size());
      if(mtemp2.cFrags.length>1)
        continue;
      if(mtemp2.ID.equals(mtemp.ID) & mtemp2.codeCount<2 )
      {
        mtemp.cFrags[count++]=mtemp2.frag_id;        
        if(count==codecount)
          break;
      }
    }
    
    //bubble sort to get the unique frag id for the encoded message
    if(mtemp.cFrags.length<=codecount & mtemp.cFrags.length>1){
      for(int i=0;i<mtemp.cFrags.length-1;i++){
        if(Integer.parseInt(mtemp.cFrags[i])>Integer.parseInt(mtemp.cFrags[i+1]))
        {
          fragid = mtemp.cFrags[i];
          mtemp.cFrags[i]=mtemp.cFrags[i+1];
          mtemp.cFrags[i+1]=fragid;
        }
      }
      fragid = new String("");
      for(int i=0;i<mtemp.cFrags.length;i++){
        fragid=fragid.concat(mtemp.cFrags[i]+" ");
      }
      mtemp.frag_id=fragid.trim();      
      if(if_repmsgexist(mtemp)==-1){//it is not present already
        /*for(int i=0;i<mtemp.cFrags.length;i++){
          for(int j=0;j<aliveMsglist.size();j++){
            if(!aliveMsglist.get(j).ID.equals(mtemp.ID))
              continue;
            if(aliveMsglist.get(j).frag_id.equals(mtemp.cFrags[i]))
              aliveMsglist.get(j).codeCount++;
          }
        }*/
          
        return true;
      }
    }
    mtemp.cFrags = null;
    return false;
  }
 
}
