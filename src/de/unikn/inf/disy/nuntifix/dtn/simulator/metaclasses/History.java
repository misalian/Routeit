
package de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses;


import java.io.DataOutputStream;
import java.io.FileOutputStream;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import de.unikn.inf.disy.nuntifix.dtn.simulator.Global;
//import de.unikn.inf.disy.nuntifix.dtn.simulator.bloomfilter.Hash;
//import de.unikn.inf.disy.nuntifix.dtn.simulator.bloomfilter.*;

public class History {

  public Hashtable<String, Device> Peerstable;

  LinkedList<String> tabapList;
  LinkedList<String> tabdevList;
  Date starttime;

  public History(Hashtable<String, Device> p, LinkedList<String> td,
      LinkedList<String> ta, Date st) {
    Peerstable = new Hashtable<String, Device>();
    tabdevList = new LinkedList<String>();
    
    tabapList = new LinkedList<String>();
    starttime = new Date();
    Peerstable = p;
    tabdevList = td;
    tabapList = ta;
    starttime = st;
  }

  public void calc_history(Date T, Date T2, int ap_count,double bw) {
    Device dtemp, dtemp2;
    TSlot ttemp, ttemp2;
    double t = ((double) (T.getTime() - starttime.getTime()))
        / (1000 * 60 * 60);
    double t2 = ((double) (T2.getTime() - starttime.getTime()))
        / (1000 * 60 * 60);
    int tij = 0;

    TSlot itime ;
    for (int l = 0; l < tabdevList.size() - ap_count; l++) {

      dtemp = Peerstable.get(tabdevList.get(l));
      int list_size1 = dtemp.sorted_slots.size();
      for (int i = dtemp.find_slot(T2, 0, dtemp.sorted_slots.size() - 1); i > -1
          & i < list_size1; i++) {
        ttemp = (TSlot) dtemp.sorted_slots.get(i);
        if (ttemp.timemin.after(T))
          break;
        for (int k = 0; k < tabdevList.size() - ap_count ; k++) {

          if(k==l)
            continue;
          dtemp2 = Peerstable.get(tabdevList.get(k));


          if (!dtemp2.Aptable.containsKey(ttemp.apname))
            continue;
          tij = 0;
          int list_size2 = dtemp2.sorted_slots.size();
          for (int j = dtemp2.find_slot(ttemp.timemin, 0, dtemp2.sorted_slots
              .size() - 1); j > -1 & j < list_size2; j++) {
            ttemp2 = (TSlot) dtemp2.sorted_slots.get(j);
            if (ttemp.timemax.before(ttemp2.timemin))
              break;
            if (!ttemp.isoverlap(ttemp2)
                | !(ttemp.apname.equals(ttemp2.apname)))
              continue;
            itime = new TSlot();
            itime.timemin.setTime((ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin : ttemp.timemin).getTime());
            itime.timemax.setTime((ttemp.timemax.before(ttemp2.timemax) ? ttemp.timemax : ttemp2.timemax).getTime());
            double duration = (itime.timemax.getTime()-itime.timemin.getTime())/1000;
            if (itime.timemin.after(T)) {

              break;
            }
            tij++;
            meetingSetup(dtemp, dtemp2, l, k, itime, bw,duration);
          }
         
          dtemp.probab[tabdevList.indexOf(dtemp2.mac)] = dtemp.probab[tabdevList
              .indexOf(dtemp2.mac)]
              * t2 + tij;
          dtemp.probab[tabdevList.indexOf(dtemp2.mac)] /= t;

        }
      }
    }
  }

  public void calc_zebraHistory(Date T, Date T2, int ap_count) {

    Device dtemp, dtemp2;
    TSlot ttemp, ttemp2;

    for (int l = 0; l < tabdevList.size() - ap_count; l++) {

      dtemp = Peerstable.get(tabdevList.get(l));
      
      int list_size1 = dtemp.sorted_slots.size();
      for (int i = dtemp.find_slot(T2, 0, dtemp.sorted_slots.size() - 1); i > -1
          & i < list_size1; i++) {
        ttemp = (TSlot) dtemp.sorted_slots.get(i);
        if (ttemp.timemin.after(T))
          break;
        // System.out.println(dtemp.mac+" "+ttemp.timemin.toString()+"
        // "+ttemp.timemax.toString());
        for (int k = 0; k < tabdevList.size() - ap_count ; k++) {
          if(k==l)
            continue;
          dtemp2 = Peerstable.get(tabdevList.get(k));

          if (!dtemp2.Aptable.containsKey(ttemp.apname))
            continue;
          int list_size2 = dtemp2.sorted_slots.size();
          for (int j = dtemp2.find_slot(ttemp.timemin, 0, dtemp2.sorted_slots
              .size() - 1); j > -1 & j < list_size2; j++) {
            ttemp2 = (TSlot) dtemp2.sorted_slots.get(j);
            if (ttemp.timemax.before(ttemp2.timemin))
              break;
            if(ttemp.Correspond.containsKey(ttemp2.hashCode()))
              continue;
            if (!ttemp2.apname.equals(ttemp.apname))
              continue;
            if (!ttemp.isoverlap(ttemp2))
              continue;
            ttemp.Correspond.put(ttemp.hashCode(), ttemp2);
            // System.out.println(dtemp2.mac+" "+ttemp2.timemin.toString()+"
            // "+ttemp2.timemax.toString());

            Date itime = ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin
                : ttemp.timemin;
            if (itime.after(T))
              break;
            dtemp.probab[tabdevList.indexOf(dtemp2.mac)] += 2;
          }
        }
        if (i % 5 == 0) {
          for (int j = 0; j < tabdevList.size() - ap_count; j++)
            dtemp.probab[j]--;
        }
      }
    }
  }

  public boolean MaxpropHistory(Date T, Date T2, int ap_count,double bw) {

    int devlistsize = Peerstable.size() - ap_count ;
    Device dtemp, dtemp2;
    TSlot ttemp = new TSlot(0, 0, "");
    TSlot ttemp2 = new TSlot(0, 0, "");
    
    boolean changed = false;
   int y=0;
    
    for (int i = 0; i < devlistsize; i++) {
      dtemp = Peerstable.get(tabdevList.get(i));
      boolean []done = new boolean[devlistsize];
      int list_size1 = dtemp.sorted_slots.size();
      for (int k = dtemp.find_slot(T2, 0, dtemp.sorted_slots.size() - 1); k > -1 & k < list_size1; k++) {
        ttemp = dtemp.sorted_slots.get(k);        
        
        if (ttemp.timemin.after(T)) 
          break;
        /*if(ttemp.timemin.toString().substring(0,16 ).equals("Mon Aug 05 16:35")){
          System.out.println(ttemp.timemin.toString()+" "+ttemp.timemax.toString());
          y=1;
        }*/
                
        for (int j = i+1; j < devlistsize; j++) {          
            if(done[j])
              continue;
          dtemp2 = Peerstable.get(tabdevList.get(j));          
          
          
          int list_size2 = dtemp2.sorted_slots.size();
          for (int l = dtemp2.find_slot(ttemp.timemin, 0, dtemp2.sorted_slots
              .size() - 1); l > -1 & l < list_size2; l++) {
            ttemp2 = dtemp2.sorted_slots.get(l);
           // if(y==1)
             // System.out.println(ttemp2.timemin.toString()+" "+ttemp2.timemax.toString());
            if (ttemp.timemax.before(ttemp2.timemin))
              break;
            /*if(ttemp.Correspond.containsKey(ttemp2.hashCode()))
              continue;*/
            if (!ttemp.apname.equals(ttemp2.apname))
              continue;
            if (!ttemp.isoverlap(ttemp2))
              continue;
            
            TSlot itime = new TSlot();
            itime.timemin.setTime((ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin : ttemp.timemin).getTime());
            itime.timemax.setTime((ttemp.timemax.before(ttemp2.timemax) ? ttemp.timemax : ttemp2.timemax).getTime());
            
            double duration = (itime.timemax.getTime()-itime.timemin.getTime())/1000;
            
            ttemp.Correspond.put(ttemp2.hashCode(), ttemp2);  
            meetingSetup(dtemp, dtemp2, i, j, itime, bw,duration);
            dtemp.meetingcount[j]++;
            dtemp.probab[j] += 1;
            dtemp2.meetingcount[i]++;
            dtemp2.probab[i] += 1;
          }
        }
      }
     double total = 0;
      boolean normalise = false;
      for (int l = 0; l < tabdevList.size() - ap_count; l++) {
        total += dtemp.probab[l];
        if (dtemp.probab[l] >= 1) {
          normalise = true;
          changed = true;
        }
      }
      if (normalise)
        for (int l = 0; l < tabdevList.size() - ap_count; l++)
          dtemp.probab[l] /= total;
    }
    //removeCluster(devlistsize);
    /*Device dtemp, dtemp2;
    TSlot ttemp, ttemp2;
    

    for (int l = 0; l < tabdevList.size() - ap_count; l++) {

      dtemp = Peerstable.get(tabdevList.get(l));
      int list_size1 = dtemp.sorted_slots.size();
      for (int i = dtemp.find_slot(T2, 0, dtemp.sorted_slots.size() - 1); i > -1
          & i < list_size1; i++) {
      
        ttemp = (TSlot) dtemp.sorted_slots.get(i);
        if (ttemp.timemin.after(T)) 
          break;
        
        for (int k = l+1; k < tabdevList.size() - ap_count; k++) {

          dtemp2 = Peerstable.get(tabdevList.get(k));
          if (!dtemp2.Aptable.containsKey(ttemp.apname)
              | dtemp2.mac.equals(dtemp.mac))
            continue;
          int list_size2 = dtemp2.sorted_slots.size();
          for (int j = dtemp2.find_slot(ttemp.timemin, 0, dtemp2.sorted_slots
              .size() - 1); j > -1 & j < list_size2; j++) {
        
            ttemp2 = (TSlot) dtemp2.sorted_slots.get(j);
            if (ttemp.timemax.before(ttemp2.timemin))
              break;
            if (!ttemp.isoverlap(ttemp2)
                | !(ttemp.apname.equals(ttemp2.apname)))
              continue;
            Date itime = ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin
                : ttemp.timemin;
            if (itime.after(T))
              break;
            
            dtemp.meetingcount[tabdevList.indexOf(dtemp2.mac)]++;
            dtemp.probab[tabdevList.indexOf(dtemp2.mac)] += 1;
            dtemp2.meetingcount[tabdevList.indexOf(dtemp.mac)]++;
            dtemp2.probab[tabdevList.indexOf(dtemp.mac)] += 1;
          }
        }
      }
      double total = 0;
      boolean normalise = false;
      for (int i = 0; i < tabdevList.size() - ap_count; i++) {
        total += dtemp.probab[i];
        if (dtemp.probab[i] >= 1) {
          normalise = true;
          changed = true;
        }
      }
      if (normalise)
        for (int i = 0; i < tabdevList.size() - ap_count; i++)
          dtemp.probab[i] /= total;
    }*/
    return changed;
  }

  // calculates the tprobability of delivery using only one intermediatry node
  // to the region of destination node
  public void MV_weights(Date T, Date T2, int ap_count) {

    Device dtemp;
    TSlot ttemp;
    double max = 0;

    double t = ((double) (T.getTime() - starttime.getTime()))
        / (1000 * 60 * 60);
    double t2 = ((double) (T2.getTime() - starttime.getTime()))
        / (1000 * 60 * 60);
    double[] approbab = new double[tabapList.size()];
    // float t2=((float)(T.getTime()-T2.getTime()))/(1000*60*60);
    for(int p=0;p<tabdevList.size()-ap_count;p++){
      dtemp = Peerstable.get(tabdevList.get(p));
      dtemp.approbab = new double[tabapList.size()];
      int list_size1 = dtemp.sorted_slots.size();
      for (int i = dtemp.find_slot(T2, 0, dtemp.sorted_slots.size() - 1); i > -1
          & i < list_size1; i++) {
        ttemp = (TSlot) dtemp.sorted_slots.get(i);
        if (ttemp.timemin.after(T))
          break;
        int apindex = tabapList.indexOf(ttemp.apname);
        if (apindex > -1)
          approbab[apindex]++;

      }

      for (int i = 0; i < tabapList.size(); i++) {
        dtemp.approbab[i] = dtemp.approbab[i] * t2 + approbab[i];
        dtemp.approbab[i] /= t;

        if (max < dtemp.approbab[i]) {
          max = dtemp.approbab[i];
          dtemp.homeap = i;
        }
      }
    }
    Device dtemp2;
    // Apoint aptemp2;
    TSlot ttemp2;
    
    // float approbab;
    float tij = 0;

    // float [][]approbabs = new
    // float[tabdevList.size()-ap_count][tabapList.size()];

    for (int l = 0; l < tabdevList.size() - ap_count; l++) {

      dtemp = Peerstable.get(tabdevList.get(l));
      int list_size1 = dtemp.sorted_slots.size();
      for (int i = dtemp.find_slot(T2, 0, dtemp.sorted_slots.size() - 1); i > -1
          & i < list_size1; i++) {
        ttemp = (TSlot) dtemp.sorted_slots.get(i);
        if (ttemp.timemin.after(T))
          break;

        for (int k = 0; k < tabdevList.size() - ap_count ; k++) {
          if(k==l)
            continue;
          dtemp2 = Peerstable.get(tabdevList.get(k));

          if (!dtemp2.Aptable.containsKey(ttemp.apname))
            continue;
          tij = 0;
          int list_size2 = dtemp2.sorted_slots.size();
          for (int j = dtemp2.find_slot(ttemp.timemin, 0, dtemp2.sorted_slots
              .size() - 1); j > -1 & j < list_size2; j++) {
            ttemp2 = (TSlot) dtemp2.sorted_slots.get(j);
            if (ttemp.timemax.before(ttemp2.timemin))
              break;
            if (!ttemp.isoverlap(ttemp2)
                | !(ttemp.apname.equals(ttemp2.apname)))
              continue;
            Date itime = ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin
                : ttemp.timemin;
            if (itime.after(T)) {
              break;
            }
            tij++;
          }
          // dtemp.probab[tabdevList.indexOf(dtemp2.mac)]=
          // dtemp.probab[tabdevList.indexOf(dtemp2.mac)]*t2+(tij/t);
          dtemp.probab[tabdevList.indexOf(dtemp2.mac)] = dtemp.probab[tabdevList
              .indexOf(dtemp2.mac)]
              * t2 + tij;
          dtemp.probab[tabdevList.indexOf(dtemp2.mac)] /= t;
        }
      }      
    }   
  }

  // Prophet history
  public void Prophethistory(Date T, Date T2, int ap_count, float pinit,
      float gama) {

    Device dtemp, dtemp2;
    TSlot ttemp, ttemp2;
    double tu = ((double) (T.getTime() - T2.getTime())) / (1000 * 60 * 60);

    for (int l = 0; l < tabdevList.size() - ap_count; l++) {

      dtemp = Peerstable.get(tabdevList.get(l));

      int list_size1 = dtemp.sorted_slots.size();
      for (int i = dtemp.find_slot(T2, 0, dtemp.sorted_slots.size() - 1); i > -1
          & i < list_size1; i++) {
        ttemp = (TSlot) dtemp.sorted_slots.get(i);
        if (ttemp.timemin.after(T))
          break;
        for (int k = 0; k < tabdevList.size() - ap_count; k++) {

          dtemp2 = Peerstable.get(tabdevList.get(k));
          dtemp.probab[tabdevList.indexOf(dtemp2.mac)] *= Math.pow(gama, tu);
          if (!dtemp2.Aptable.containsKey(ttemp.apname))
            continue;
          int list_size2 = dtemp2.sorted_slots.size();
          for (int j = dtemp2.find_slot(ttemp.timemin, 0, dtemp2.sorted_slots
              .size() - 1); j > -1 & j < list_size2; j++) {
            ttemp2 = (TSlot) dtemp2.sorted_slots.get(j);
            if (ttemp.timemax.before(ttemp2.timemin))
              break;
            if (!ttemp.isoverlap(ttemp2)
                | !(ttemp.apname.equals(ttemp2.apname)))
              continue;
            Date itime = ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin
                : ttemp.timemin;
            if (itime.after(T))
              break;

            dtemp.probab[tabdevList.indexOf(dtemp2.mac)] = dtemp.probab[tabdevList
                .indexOf(dtemp2.mac)]
                + (1 - dtemp.probab[tabdevList.indexOf(dtemp2.mac)]) * pinit;
          }
        }
      }
    }
  }

  
 /* public void removeOldstats(Date T,int devlistsize){
    Device dtemp;
    for (int i = 0; i < devlistsize; i++) {
      dtemp = Peerstable.get(tabdevList.get(i));
      Enumeration<Integer>mskenum = dtemp.meetStat.keys();
      
      while (mskenum.hasMoreElements()){
        int j = mskenum.nextElement();
        dtemp.meetStat.get(j).removeoldData(T);
      if(!dtemp.meetStat.get(j).PVector.containsKey(j))
        dtemp.meetStat.remove(j);
      }      
    }
  }*/
  public void GenHistory(int ap_count,double bw,String trace) {

    int devlistsize = Peerstable.size() - ap_count ;
    Device dtemp, dtemp2;
    TSlot ttemp = new TSlot(0, 0, "");
    TSlot ttemp2 = new TSlot(0, 0, "");
    double duration;
    //Hashtable<String, SlotStats> devOverlap= new Hashtable<String, SlotStats>();
    Hashtable<Integer, LinkedList<TSlot>>slotBuffer = new Hashtable<Integer, LinkedList<TSlot>>();
    Hashtable<Integer, LinkedList<Integer>>debug = new Hashtable<Integer, LinkedList<Integer>>();
    for (int i = 0; i < devlistsize; i++) {
      dtemp = Peerstable.get(tabdevList.get(i));
      if(i%50==0)  
      System.out.println(i);
        
        for (int j = i + 1; j < devlistsize; j++) {     
          dtemp2 = Peerstable.get(tabdevList.get(j));
          
          int list_size1 = dtemp.sorted_slots.size();
          int list_size2 = dtemp2.sorted_slots.size();
          int k = 0,l = 0;         
          TSlot itime = new TSlot();
          TSlot itime2 = new TSlot();
          
          duration=0;
          
          while(k<list_size1 & l<list_size2){
            ttemp = dtemp.sorted_slots.get(k);
            ttemp2 = dtemp2.sorted_slots.get(l);
           
            if (!ttemp.isoverlap(ttemp2) | !ttemp.apname.equals(ttemp2.apname)){	
           	
              if( duration>2){
              		
                saveSlots(ttemp, ttemp2, itime2, slotBuffer, Round((double)(itime2.timemin.getTime()-starttime.getTime())/(60000*60*24),2),debug);
                itime2 = new TSlot();
              }	
              duration=0;
              if (ttemp.timemax.before(ttemp2.timemin))                
                k++;              
              else if (ttemp.timemin.after(ttemp2.timemax))                
                l++;
              else if (ttemp.timemax.before(ttemp2.timemax))
                k++;
              else
                l++;
              }
            else{
            		itime.timemin.setTime((ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin : ttemp.timemin).getTime());
            		itime.timemax.setTime((ttemp.timemax.before(ttemp2.timemax) ? ttemp.timemax : ttemp2.timemax).getTime());
            		if(itime2.timemax.before(itime.timemin))
            		  if(itime2.timemax.getTime()+(5000)>itime.timemin.getTime()){
            		    saveSlots(ttemp, ttemp2, itime, slotBuffer, Round((double)(itime.timemin.getTime()-starttime.getTime())/(60000*60*24),2),debug);
            		    itime.timemin.setTime(itime2.timemin.getTime());
            		  }
            		  else
            		  {
            		    if( duration>2){
                    
            		      saveSlots(ttemp, ttemp2, itime2, slotBuffer, Round((double)(itime2.timemin.getTime()-starttime.getTime())/(60000*60*24),2),debug);
            		    }
            		  }            		
            		itime2 = new TSlot(itime.timemin,itime.timemax);
            		duration = (itime2.timemax.getTime()-itime2.timemin.getTime())/1000;
            		
            		itime = new TSlot();
            		
            		k++;l++;	
              } 
            
          } 
          if( duration>2){            
            saveSlots(ttemp, ttemp2, itime2, slotBuffer, Round((double)(itime2.timemin.getTime()-starttime.getTime())/(60000*60*24),2),debug);
          }   
      }
    }  
    writedata(slotBuffer, trace, debug);    
  }
  public void writentnewaitingtime(int ap_count, double bw, String trace){
    int devlistsize = Peerstable.size() - ap_count ;
    Device dtemp, dtemp2;
    TSlot ttemp = new TSlot(0, 0, "");
    TSlot ttemp2 = new TSlot(0, 0, "");
    MeetingStats ms1, ms2;
       
   for (int i = 0; i < devlistsize; i++) {
     dtemp = Peerstable.get(tabdevList.get(i));
     boolean []done = new boolean[devlistsize];
     int list_size1 = dtemp.sorted_slots.size();
     for (int k = 0; k < list_size1; k++) {
       ttemp = dtemp.sorted_slots.get(k);        
         
                    
      for (int j = i + 1; j < devlistsize; j++) {          
        if(done[j])
          continue;
        dtemp2 = Peerstable.get(tabdevList.get(j));
        Date itime = new Date();
        int list_size2 = dtemp2.sorted_slots.size();
        for (int l = dtemp2.find_slot(ttemp.timemin, 0, dtemp2.sorted_slots
                  .size() - 1); l > -1 & l < list_size2; l++) {
          ttemp2 = dtemp2.sorted_slots.get(l);
          if (ttemp.timemax.before(ttemp2.timemin))
            break;
          if(ttemp.Correspond.containsKey(ttemp2.hashCode()))
             continue;
          if (!ttemp.apname.equals(ttemp2.apname))
             continue;
          if (!ttemp.isoverlap(ttemp2))
              continue;
               
          itime = ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin : ttemp.timemin;
       
          double duration = ((ttemp.timemax.after(ttemp2.timemax) ? ttemp2.timemax : ttemp.timemax).getTime() - itime.getTime()) / 1000;
          if(duration<2)
              continue;
          ttemp.Correspond.put(ttemp2.hashCode(), ttemp2);
          if (!dtemp.meetStat.containsKey(new Integer(j))) {
            ms1 = new MeetingStats( new Integer(j),new Integer(i));
            ms2 = new MeetingStats(new Integer(i),new Integer(j));
            ms1.PVector.put(ms1.devindex, new DVectorUnit(ms1.devindex,itime));
            ms2.PVector.put(ms2.devindex, new DVectorUnit(ms2.devindex,itime));
            dtemp.meetStat.put(new Integer(j), ms1);
            dtemp2.meetStat.put(new Integer(i), ms2);    
                  
              
            }
            else{
              ms1 = dtemp.meetStat.get(j);
              ms2 = dtemp2.meetStat.get(i);             
            }         
            ms1.meetingcnt++;
            ms2.meetingcnt++;
            ms1.duration = duration;
            ms2.duration = duration;
            ms1.bAvailable+=duration*bw;
            ms2.bAvailable=ms1.bAvailable;
                
            DVectorUnit dvtemp1 = ms1.PVector.get(ms1.devindex);
            DVectorUnit dvtemp2 = ms2.PVector.get(ms2.devindex);
            double virtualdelay ;
                
            if(ms1.duration<=0){
              System.out.println("bad duration");
            }
                
            if(ms1.meetingcnt>1)              
              virtualdelay = (itime.getTime()- ms1.lastmeeting.getTime())/((double) 60000);             
                else
            virtualdelay = Global.weekMin;
            if(virtualdelay>1){
              if(ms1.meetingcnt==2 & dvtemp1.virtmsgDelaysList.size()==1){
                if(dvtemp1.virtmsgDelaysList.get(0).time==Global.weekMin)
                  dvtemp1.virtmsgDelaysList.remove(0);                  
                    
                if(ms2.meetingcnt==2 & dvtemp2.virtmsgDelaysList.size()==1){
                  if(dvtemp2.virtmsgDelaysList.get(0).time==Global.weekMin)
                    dvtemp2.virtmsgDelaysList.remove(0);
                }              
              }
              
              dvtemp1.AddVirtDelay(virtualdelay,ms1.duration,itime.getTime()+(long)(ms1.duration*1000));
              dvtemp2.AddVirtDelay(virtualdelay,ms1.duration,itime.getTime()+(long)(ms1.duration*1000));
              }
               /* else 
                  virtualdelay =0;*/
                ms1.lastmeeting = new Date((ttemp.timemax.after(ttemp2.timemax) ? ttemp2.timemax : ttemp.timemax).getTime());
                ms2.lastmeeting = new Date(ms1.lastmeeting.getTime());
                           
              }              
            }
          }
        }
        try{
          DataOutputStream out = new DataOutputStream(new FileOutputStream("result/"+trace+"/node-nodewt.txt"));
          //DataOutputStream out2 = new DataOutputStream(new FileOutputStream("result/"+trace+"/node-nodemeetincn.txt"));
          DataOutputStream out3 = new DataOutputStream(new FileOutputStream("result/"+trace+"/node-nodemix.txt"));
          
          for(int i=0;i<devlistsize;i++){
            dtemp = Peerstable.get(tabdevList.get(i));
            for(int j=0;j<devlistsize;j++){
              if(!dtemp.meetStat.containsKey(j)){
                if(i==j){
                  out.writeBytes(Double.toString(0)+" ");
                  out3.writeBytes(Double.toString(0)+" ");
                }
                else{
                out.writeBytes(Double.toString(Double.MAX_VALUE)+" ");
                out3.writeBytes(Double.toString(Double.MAX_VALUE)+" ");
                //out2.writeBytes(Double.toString(0)+"\t");
                }
                continue;
              }
              dtemp.meetStat.get(j).PVector.get(j).calcVirAvDTime(null,0);
             
                //out.writeBytes(control.tabdevList.indexOf(dtemp.mac)+" "+j+" "+control.roundOff(Float.toString(dtemp.mstat[j].
              //waitTime/dtemp.mstat[j].meetingcnt))+" "+dtemp.mstat[j].meetingcnt+"\n");
                out.writeBytes(Double.toString(Round(Math.log(dtemp.meetStat.get(j).PVector.get(j).VirtAvgDelay),3))+" ");
                //out2.writeBytes(Double.toString(1/((double)(dtemp.mstat[j].meetingcnt)))+" ");
                out3.writeBytes(Double.toString(Round(Math.log(dtemp.meetStat.get(j).PVector.get(j).VirtAvgSize*
                    dtemp.meetStat.get(j).PVector.get(j).VirtAvgDelay),3))+" ");
              }
            out.writeBytes("\n");
            out3.writeBytes("\n");
           // out2.writeBytes("\n");  
            }
            
            
             
          out.close();
          out3.close();
          //out2.close();
          System.out.println("Node to node waiting time written");
        }
        catch(Exception e){
          System.err.println("Unable to write the end file "+e.toString() +" "+ e.getMessage());
        } 
      }
  public void writedata(Hashtable<Integer, LinkedList<TSlot>> slotBuffer, String trace,Hashtable<Integer, LinkedList<Integer>>debug){
    TSlot ttemp, ttemp2;
    double duration;
    try {
      DataOutputStream out = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
          + "MeetingDist.txt")); // Continue to re
      //Calendar c1=Calendar.getInstance(),c2=Calendar.getInstance(),c3=Calendar.getInstance(),c4=Calendar.getInstance();
      Enumeration<Integer>kenum = slotBuffer.keys();
      
      while(kenum.hasMoreElements()){
        Integer key = kenum.nextElement();
        LinkedList<TSlot>slotlist = slotBuffer.get(key);
        
        for(int i=0;i<slotlist.size();i++){
          ttemp = slotlist.get(i);
          if(debug.containsKey(ttemp.hashCode()))
            System.out.println(ttemp.hashCode());  
          if(ttemp.correspo.size()==0){
            continue;
          }
          int count=0;
          Enumeration<Integer>cenum = ttemp.Correspond.keys();
          while(cenum.hasMoreElements()){
            int key2= cenum.nextElement();
             
            count += calcMeetings(slotBuffer, key2, ttemp,debug);
          }
          duration = (ttemp.timemax.getTime()-ttemp.timemin.getTime())/(double)(60000);
          out.writeBytes(Double.toString(ttemp.etime)+"\t"+Double.toString(Round(duration,2))+"\t"+
              Integer.toString(count+1)+"\n");   
          
          if(count==0){// & debug.containsKey(ttemp.hashCode())){
            System.out.println(ttemp.timemin.toString()+" "+ttemp.timemax.toString()+" "+ttemp.correspo.size());
           
           cenum = ttemp.Correspond.keys();
            while(cenum.hasMoreElements()){     
              LinkedList<TSlot>slotlist2 = slotBuffer.get(cenum.nextElement().hashCode());
              for(int k=0;k<slotlist2.size();k++){
                ttemp2 = slotlist2.get(k);
                System.out.println(ttemp2.timemin.toString()+" "+ttemp2.timemax.toString()+" "+ttemp2.correspo.size());
              }
            }
            System.out.println("Hayeeee");
            System.exit(0);
          }          
        }    
    }
    System.out.println(trace+" Meeting Distribution writing done");
    }catch (Exception e) {
      System.err.println(e.getMessage()+" "+e.toString());
      e.printStackTrace();
    }
  }
  public int calcMeetings(Hashtable<Integer, LinkedList<TSlot>>slotBuffer, Integer key2,TSlot ttemp, Hashtable<Integer, LinkedList<Integer>>debug ){
   
    int count=0;
    
    LinkedList<TSlot>slotlist2 = slotBuffer.get(key2);
    for(int k=0;k<slotlist2.size();k++){
      if(ttemp.correspo.size()==0)
        break;
      TSlot ttemp2 = slotlist2.get(k);
      if(!ttemp2.correspo.containsKey(ttemp.hashCode()))
        continue;
      /*if(ttemp2.correspo.size()==0)
        continue;
        */
      if(debug.containsKey(ttemp2.hashCode()))
        System.out.println(ttemp2.hashCode());
      if((ttemp.timemin.getTime()==ttemp2.timemin.getTime()) & (ttemp.timemax.getTime()==ttemp2.timemax.getTime()))
      {
        count++;              
        /*ttemp2.count--;
        ttemp.count--;*/
        if(ttemp2.correspo.remove(ttemp.hashCode())==null)
          System.err.println("key not found ttemp2 "+ttemp.hashCode());
        if(ttemp.correspo.remove(ttemp2.hashCode())==null)
          System.err.println("key not found ttemp "+ttemp2.hashCode());
        if(ttemp2.correspo.size()>0){
          Enumeration<Integer>cenum = ttemp2.Correspond.keys();
          while(cenum.hasMoreElements())
            count+=calcMeetings(slotBuffer, cenum.nextElement(), ttemp2,debug); 
        }     
      }
    }
    return count;
  }
  
  public boolean checkAdjustment(TSlot ttemp, TSlot itime,Hashtable<Integer, LinkedList<TSlot>>slotBuffer, TSlot itime2,TSlot ttemp2){
    itime.exact = false;
    itime.adjust = false;
    itime2.exact = false;
    itime2.adjust = false;
    int i;
    
    //if(itime.timemin.toString().equals("Tue Nov 09 09:38:52 CET 2004") & itime.timemax.toString().equals("Tue Nov 09 09:41:54 CET 2004"))
      //i=0;
    for(  i=0;i<slotBuffer.get(ttemp2.hashCode()).size() & (!itime.adjust & !itime.exact);i++){
      if(slotBuffer.get(ttemp2.hashCode()).get(i).timemin.getTime()==itime.timemin.getTime() & 
          slotBuffer.get(ttemp2.hashCode()).get(i).timemax.getTime()==itime.timemax.getTime())
        itime.exact=true;
      else if(Math.abs(slotBuffer.get(ttemp2.hashCode()).get(i).timemin.getTime()-itime.timemin.getTime())<5000 & 
          Math.abs(slotBuffer.get(ttemp2.hashCode()).get(i).timemax.getTime()-itime.timemax.getTime())<5000)
            itime.adjust=true;
    }
    if(itime.adjust){
      itime.timemin.setTime(slotBuffer.get(ttemp2.hashCode()).get(i-1).timemin.getTime());
      itime.timemax.setTime(slotBuffer.get(ttemp2.hashCode()).get(i-1).timemax.getTime());      
      itime2.timemin.setTime(itime.timemin.getTime());
      itime2.timemax.setTime(itime.timemax.getTime());
    }
    
    int k;
    for(  k=0;k<slotBuffer.get(ttemp.hashCode()).size() & (!itime2.adjust & !itime2.exact);k++){
      if(slotBuffer.get(ttemp.hashCode()).get(k).timemin.getTime()==itime2.timemin.getTime() & 
          slotBuffer.get(ttemp.hashCode()).get(k).timemax.getTime()==itime2.timemax.getTime())
        itime2.exact=true;
      else if(Math.abs(slotBuffer.get(ttemp.hashCode()).get(k).timemin.getTime()-itime2.timemin.getTime())<5000 & 
          Math.abs(slotBuffer.get(ttemp.hashCode()).get(k).timemax.getTime()-itime2.timemax.getTime())<5000)
            itime2.adjust=true;
    }
    if(itime2.adjust){
      itime2.timemin.setTime(slotBuffer.get(ttemp.hashCode()).get(k-1).timemin.getTime());
      itime2.timemax.setTime(slotBuffer.get(ttemp.hashCode()).get(k-1).timemax.getTime());      
      itime.timemin.setTime(itime2.timemin.getTime());
      itime.timemax.setTime(itime2.timemax.getTime());
    }
    if(itime.adjust&itime2.adjust){  
      //if(itime.timemin.toString().equals("Mon Nov 22 14:17:51 CET 2004") & itime.timemax.toString().equals("Mon Nov 22 19:37:48 CET 2004"))
        //System.out.println();
      return false;
    }
    if(itime.exact&itime2.exact){
      //if(itime.timemin.toString().equals("Mon Nov 22 14:17:51 CET 2004") & itime.timemax.toString().equals("Mon Nov 22 19:37:48 CET 2004"))
       // System.out.println();
      return false;
    }
    if((itime.exact & itime2.adjust) |(itime.adjust&itime2.exact))
        return false;
    if(itime.adjust|itime.exact){     
      if(slotBuffer.get(ttemp2.hashCode()).get(i-1).correspo.containsKey(itime2.hashCode()))
        System.err.println("Adjust aadd error 1");
      slotBuffer.get(ttemp2.hashCode()).get(i-1).correspo.put(itime2.hashCode(), itime2);
      slotBuffer.get(ttemp2.hashCode()).get(i-1).Correspond.put(ttemp.hashCode(), ttemp);  
      if(itime2.correspo.remove(itime.hashCode())==null)
        System.err.println("Adjust remove error 1");
      itime2.correspo.put(slotBuffer.get(ttemp2.hashCode()).get(i-1).hashCode(), slotBuffer.get(ttemp2.hashCode()).get(i-1));
      if(!itime2.Correspond.containsKey(ttemp2.hashCode()))
        System.err.println("Adjust remove error 2");
      
     // if(itime.timemin.toString().equals("Mon Nov 15 12:21:36 CET 2004") & itime.timemax.toString().equals("Mon Nov 15 12:23:30 CET 2004"))
       // System.out.println("adust "+ ttemp.hashCode()+" "+ttemp2.hashCode()+" "+itime.hashCode()+" "+itime2.hashCode()+" "+slotBuffer.get(ttemp2.hashCode()).get(i-1).hashCode());
      
      
    }    
    if(itime2.adjust|itime2.exact){     
      if(slotBuffer.get(ttemp.hashCode()).get(k-1).correspo.containsKey(itime.hashCode()))
        System.err.println("Adjust aadd error 1");
      slotBuffer.get(ttemp.hashCode()).get(k-1).correspo.put(itime.hashCode(), itime);
      slotBuffer.get(ttemp.hashCode()).get(k-1).Correspond.put(ttemp2.hashCode(), ttemp2);  
      if(itime.correspo.remove(itime2.hashCode())==null)
        System.err.println("Adjust remove error 1");
      itime.correspo.put(slotBuffer.get(ttemp.hashCode()).get(k-1).hashCode(), slotBuffer.get(ttemp.hashCode()).get(k-1));
      if(!itime.Correspond.containsKey(ttemp.hashCode()))
        System.err.println("Adjust remove error 2");
      //if(itime2.timemin.toString().equals("Mon Nov 15 12:21:36 CET 2004") & itime2.timemax.toString().equals("Mon Nov 15 12:23:30 CET 2004"))
      //  System.out.println("adust "+ ttemp.hashCode()+" "+ttemp2.hashCode()+" "+itime.hashCode()+" "+itime2.hashCode()+" "+slotBuffer.get(ttemp.hashCode()).get(k-1).hashCode());
      //itime2.Correspond.put(key, value)*/
      
    }   
    return true;
  } 
  
  public void saveSlots(TSlot ttemp, TSlot ttemp2, TSlot itime3,Hashtable<Integer, LinkedList<TSlot>>slotBuffer,double etime, Hashtable<Integer, LinkedList<Integer>>debug){
    TSlot itime = new TSlot(itime3.timemin,itime3.timemax);
    TSlot itime2 = new TSlot(itime.timemin,itime.timemax);
     
    itime.etime = etime;
    itime2.etime = etime;
    
    itime.correspo = new Hashtable<Integer, TSlot>();
    itime2.correspo = new Hashtable<Integer, TSlot>();
    itime.Correspond = new Hashtable<Integer, TSlot>();
    itime2.Correspond= new Hashtable<Integer, TSlot>();
    
    itime.Correspond.put(ttemp.hashCode(), ttemp);
    itime2.Correspond.put(ttemp2.hashCode(), ttemp2);    
    itime.correspo.put(itime2.hashCode(), itime2);
    itime2.correspo.put(itime.hashCode(), itime);   

    if(debug.containsKey(ttemp.hashCode())|debug.containsKey(ttemp2.hashCode()))
      itime.etime=etime;
    if(!slotBuffer.containsKey(ttemp.hashCode()))
      slotBuffer.put(ttemp.hashCode(), new LinkedList<TSlot>());    
    if(!slotBuffer.containsKey(ttemp2.hashCode()))
      slotBuffer.put(ttemp2.hashCode(), new LinkedList<TSlot>());
    
    if(!checkAdjustment(ttemp, itime, slotBuffer,itime2,ttemp2))
      return;
   // checkAdjustment(ttemp2, itime2, slotBuffer,itime,ttemp);
  
    
    
    if(itime.adjust& !itime.exact){
      slotBuffer.get(ttemp.hashCode()).add(itime2);
      //return;
    }
    else if(!itime.exact & !itime2.adjust)
        slotBuffer.get(ttemp2.hashCode()).add(itime);  
    
    if(itime2.adjust & !itime2.exact){
      slotBuffer.get(ttemp2.hashCode()).add(itime);
      //return;
    }
    else    
    if(!itime2.exact & !itime.adjust)
      slotBuffer.get(ttemp.hashCode()).add(itime2);  
   
    
   
   /* if(itime.timemin.toString().equals("Mon Nov 15 12:21:36 CET 2004") & itime.timemax.toString().equals("Mon Nov 15 12:23:30 CET 2004"))
      {
      
      System.out.println(ttemp.hashCode()+" "+ttemp2.hashCode()+" "+itime.hashCode()+" "+itime2.hashCode());
      if(!debug.containsKey(itime.hashCode()))
          debug.put(itime.hashCode(), new LinkedList<Integer>());
      if(!debug.containsKey(itime2.hashCode()))
          debug.put(itime2.hashCode(), new LinkedList<Integer>());
      debug.get(itime.hashCode()).add(itime.hashCode());
      debug.get(itime2.hashCode()).add(itime2.hashCode());
      
    }else
      if(itime2.timemin.toString().equals("Mon Nov 15 12:21:36 CET 2004") & itime2.timemax.toString().equals("Mon Nov 15 12:23:30 CET 2004"))
        {
        System.out.println(ttemp.hashCode()+" "+ttemp2.hashCode()+" "+itime.hashCode()+" "+itime2.hashCode());
        if(!debug.containsKey(ttemp.hashCode()))
            debug.put(ttemp.hashCode(), new LinkedList<Integer>());
        if(!debug.containsKey(ttemp2.hashCode()))
            debug.put(ttemp2.hashCode(), new LinkedList<Integer>());
        debug.get(itime.hashCode()).add(itime.hashCode());
        debug.get(itime2.hashCode()).add(itime2.hashCode());
        
      }*/
    
  }
  public void meetingSetup(Device dtemp, Device dtemp2, int i,int j,  TSlot itime,double bw,double duration){
    
    MeetingStats ms1, ms2;
    
    if (!dtemp.meetStat.containsKey(new Integer(j))) {
      ms1 = new MeetingStats( new Integer(j),new Integer(i));
      ms2 = new MeetingStats(new Integer(i),new Integer(j));
      /*ms1.myNeighbor.get(ms1.devindex).PVector.put(ms1.devindex, new DVectorUnit(ms1.devindex,itime.timemin));
      ms2.myNeighbor.get(ms2.devindex).PVector.put(ms2.devindex, new DVectorUnit(ms2.devindex,itime.timemin));*/
      ms1.PVector.put(ms1.devindex, new DVectorUnit(ms1.devindex,itime.timemin));
      ms2.PVector.put(ms2.devindex, new DVectorUnit(ms2.devindex,itime.timemin));
      
      dtemp.meetStat.put(new Integer(j), ms1);
      dtemp2.meetStat.put(new Integer(i), ms2);               
    }
    else{
      ms1 = dtemp.meetStat.get(j);
      ms2 = dtemp2.meetStat.get(i);             
    }         
    if(ms1.meetingcnt>1)     
        ms1.stabiltiy = Math.min(((duration*bw)/(ms1.bAvailable/ms1.meetingcnt))*Math.log10(ms1.meetingcnt),1); 
         
    ms1.meetingcnt++;
    ms2.meetingcnt++;
    ms1.duration = duration;
    ms2.duration = duration;
    ms2.stabiltiy = ms1.stabiltiy;
    ms1.bAvailable+=duration*bw;
    ms2.bAvailable=ms1.bAvailable;       
  }
  public void NileHistory(Date T, Date T2, int ap_count, int msgtrack, double bw, boolean dFlood) {

    int devlistsize = Peerstable.size() - ap_count ;
    Device dtemp, dtemp2;
    TSlot ttemp = new TSlot(0, 0, "");
    TSlot ttemp2 = new TSlot(0, 0, "");
    MeetingStats ms1, ms2;
    boolean nitem;
   // dFlood = false;

    
    for (int i = 0; i < devlistsize; i++) {
      dtemp = Peerstable.get(tabdevList.get(i));
      boolean []done = new boolean[devlistsize];
     /* if(i==22)
        i=i+1-1;*/
      int list_size1 = dtemp.sorted_slots.size();
      for (int k = dtemp.find_slot(T2, 0, dtemp.sorted_slots.size() - 1); k > -1
          & k < list_size1; k++) {
        ttemp = dtemp.sorted_slots.get(k);        
        
        if (ttemp.timemin.after(T)) 
          break;
                
        for (int j = i + 1; j < devlistsize; j++) {          
            if(done[j])
              continue;
          dtemp2 = Peerstable.get(tabdevList.get(j));
          boolean check = false;
          nitem= false;
          Date itime = new Date();
          int list_size2 = dtemp2.sorted_slots.size();
          for (int l = dtemp2.find_slot(T2, 0, dtemp2.sorted_slots
              .size() - 1); l > -1 & l < list_size2; l++) {
            ttemp2 = dtemp2.sorted_slots.get(l);
            if (ttemp.timemax.before(ttemp2.timemin))
              break;
            if(ttemp.Correspond.containsKey(ttemp2.hashCode()))
              continue;
            if (!ttemp.apname.equals(ttemp2.apname))
              continue;
            if (!ttemp.isoverlap(ttemp2))
              continue;
            /*if(i==22 & j==275)
              j=j+1-1;*/
            itime = ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin : ttemp.timemin;
            if(itime.after(T))
              break;
            double duration = ((ttemp.timemax.after(ttemp2.timemax) ? ttemp2.timemax : ttemp.timemax).getTime() - itime.getTime()) / 1000;
            if(duration<2)
              continue;
            ttemp.Correspond.put(ttemp2.hashCode(), ttemp2);
            if (!dtemp.meetStat.containsKey(new Integer(j))) {
              ms1 = new MeetingStats( new Integer(j),new Integer(i));
              ms2 = new MeetingStats(new Integer(i),new Integer(j));
             /* ms1.myNeighbor.get(ms1.devindex).PVector.put(ms1.devindex, new DVectorUnit(ms1.devindex,itime));
              ms2.myNeighbor.get(ms2.devindex).PVector.put(ms2.devindex, new DVectorUnit(ms2.devindex,itime));*/
              ms1.PVector.put(ms1.devindex, new DVectorUnit(ms1.devindex,itime));
              ms2.PVector.put(ms2.devindex, new DVectorUnit(ms2.devindex,itime));
              dtemp.meetStat.put(new Integer(j), ms1);
              dtemp2.meetStat.put(new Integer(i), ms2);    
              
              nitem=true;
              setMsgUpdate(dtemp);              
              setMsgUpdate(dtemp2);
              
            }
            else{
              ms1 = dtemp.meetStat.get(j);
              ms2 = dtemp2.meetStat.get(i);             
            }           
            ms1.myMsgStatsExchange(dtemp,dtemp2,msgtrack);
            if(ms2==null)
             i = i+1-1; 
            ms2.myMsgStatsExchange(dtemp2,dtemp,msgtrack);           
                      
           // ttemp2.Correspond.put(ttemp, ttemp);
            //break;    
            dtemp.totalmeetcount++;
            dtemp2.totalmeetcount++;
            ms1.meetingcnt++;
            ms2.meetingcnt++;
            ms1.duration = duration;
            ms2.duration = duration;
            ms1.bAvailable+=duration*bw;
            ms2.bAvailable=ms1.bAvailable;
            /*DVectorUnit dvtemp1 = ms1.myNeighbor.get(ms1.devindex).PVector.get(ms1.devindex);
            DVectorUnit dvtemp2 = ms2.myNeighbor.get(ms2.devindex).PVector.get(ms2.devindex);*/
            DVectorUnit dvtemp1 = ms1.PVector.get(ms1.devindex);
            DVectorUnit dvtemp2 = ms2.PVector.get(ms2.devindex);
            double virtualdelay ;
            
            if(ms1.duration<=0){
              System.out.println("bad duration");
            }
            
            if(ms1.meetingcnt>1)              
              virtualdelay = (itime.getTime()- ms1.lastmeeting.getTime())/((double) 60000);             
            else
              virtualdelay = Global.weekMin;
            if(virtualdelay>1){
              if(ms1.meetingcnt==2 & dvtemp1.virtmsgDelaysList.size()==1){
                if(dvtemp1.virtmsgDelaysList.get(0).time==Global.weekMin)
                 dvtemp1.virtmsgDelaysList.remove(0);                  
                
                if(ms2.meetingcnt==2 & dvtemp2.virtmsgDelaysList.size()==1){
                  if(dvtemp2.virtmsgDelaysList.get(0).time==Global.weekMin)
                    dvtemp2.virtmsgDelaysList.remove(0);
                }              
              }
              check=true; 
              dvtemp1.AddVirtDelay(virtualdelay,ms1.duration*bw,itime.getTime()+(long)(ms1.duration*1000));
              dvtemp2.AddVirtDelay(virtualdelay,ms1.duration*bw,itime.getTime()+(long)(ms1.duration*1000));
            }
           /* else 
              virtualdelay =0;*/
            ms1.lastmeeting = new Date((ttemp.timemax.after(ttemp2.timemax) ? ttemp2.timemax : ttemp.timemax).getTime());
            ms2.lastmeeting = new Date(ms1.lastmeeting.getTime());
                       
          }
          if(check ){
            done[j]=true;
            //dtemp.normalisingfact();
            //dtemp2.normalisingfact();
            ms1 = dtemp.meetStat.get(j);
            ms2 = dtemp2.meetStat.get(i);
           /* ms1.neighborExchange(dtemp2, T2, dFlood);
            ms2.neighborExchange(dtemp, T2, dFlood);*/
            ms1.mypvexchange(dtemp2,dFlood,T,false);
            ms2.mypvexchange(dtemp,dFlood,T,false); 
            if(!dFlood){
              checkDuplicateMsg(dtemp, dtemp2, ms1, ms2, nitem);
              checkDuplicateMsg(dtemp2, dtemp, ms2, ms1, nitem);
            }
          }
        }
      }
    }
    //removeCluster(devlistsize);
  }
  public void MaxFlowHistory(Date T, Date T2, int ap_count, double bw) {

    int devlistsize = Peerstable.size() - ap_count ;
    Device dtemp, dtemp2;
    TSlot ttemp = new TSlot(0, 0, "");
    TSlot ttemp2 = new TSlot(0, 0, "");
    MeetingStats ms1, ms2;
    boolean nitem;
   // dFlood = false;

    
    for (int i = 0; i < devlistsize; i++) {
      dtemp = Peerstable.get(tabdevList.get(i));
      boolean []done = new boolean[devlistsize];
     /* if(i==22)
        i=i+1-1;*/
      int list_size1 = dtemp.sorted_slots.size();
      for (int k = dtemp.find_slot(T2, 0, dtemp.sorted_slots.size() - 1); k > -1
          & k < list_size1; k++) {
        ttemp = dtemp.sorted_slots.get(k);        
        
        if (ttemp.timemin.after(T)) 
          break;
                
        for (int j = i + 1; j < devlistsize; j++) {          
            if(done[j])
              continue;
          dtemp2 = Peerstable.get(tabdevList.get(j));
          boolean check = false;
          nitem= false;
          Date itime = new Date();
          int list_size2 = dtemp2.sorted_slots.size();
          for (int l = dtemp2.find_slot(T2, 0, dtemp2.sorted_slots
              .size() - 1); l > -1 & l < list_size2; l++) {
            ttemp2 = dtemp2.sorted_slots.get(l);
            if (ttemp.timemax.before(ttemp2.timemin))
              break;
            if(ttemp.Correspond.containsKey(ttemp2.hashCode()))
              continue;
            if (!ttemp.apname.equals(ttemp2.apname))
              continue;
            if (!ttemp.isoverlap(ttemp2))
              continue;
           
            itime = ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin : ttemp.timemin;
            if(itime.after(T))
              break;
            double duration = ((ttemp.timemax.after(ttemp2.timemax) ? ttemp2.timemax : ttemp.timemax).getTime() - itime.getTime()) / 1000;
            if(duration<2)
              continue;
            ttemp.Correspond.put(ttemp2.hashCode(), ttemp2);
            if (!dtemp.meetStat.containsKey(new Integer(j))) {
              ms1 = new MeetingStats( new Integer(j),new Integer(i));
              ms2 = new MeetingStats(new Integer(i),new Integer(j));
             /* ms1.myNeighbor.get(ms1.devindex).PVector.put(ms1.devindex, new DVectorUnit(ms1.devindex,itime));
              ms2.myNeighbor.get(ms2.devindex).PVector.put(ms2.devindex, new DVectorUnit(ms2.devindex,itime));*/
              ms1.PVector.put(ms1.devindex, new DVectorUnit(ms1.devindex,itime));
              ms2.PVector.put(ms2.devindex, new DVectorUnit(ms2.devindex,itime));
              dtemp.meetStat.put(new Integer(j), ms1);
              dtemp2.meetStat.put(new Integer(i), ms2);    
              
              nitem=true;        
              
            }
            else{
              ms1 = dtemp.meetStat.get(j);
              ms2 = dtemp2.meetStat.get(i);             
            }         
            ms1.meetingcnt++;
            ms2.meetingcnt++;
            ms1.duration = duration;
            ms2.duration = duration;
            ms1.bAvailable+=duration*bw;
            ms2.bAvailable=ms1.bAvailable;
            /*DVectorUnit dvtemp1 = ms1.myNeighbor.get(ms1.devindex).PVector.get(ms1.devindex);
            DVectorUnit dvtemp2 = ms2.myNeighbor.get(ms2.devindex).PVector.get(ms2.devindex);*/
            DVectorUnit dvtemp1 = ms1.PVector.get(ms1.devindex);
            DVectorUnit dvtemp2 = ms2.PVector.get(ms2.devindex);
            double virtualdelay ;
            
            if(ms1.duration<=0){
              System.out.println("bad duration");
            }
            
            if(ms1.meetingcnt>1)              
              virtualdelay = (itime.getTime()- ms1.lastmeeting.getTime())/((double) 60000);             
            else
              virtualdelay = Global.weekMin;
            if(virtualdelay>1){
              if(ms1.meetingcnt==2 & dvtemp1.virtmsgDelaysList.size()==1){
                if(dvtemp1.virtmsgDelaysList.get(0).time==Global.weekMin)
                 dvtemp1.virtmsgDelaysList.remove(0);                  
                
                if(ms2.meetingcnt==2 & dvtemp2.virtmsgDelaysList.size()==1){
                  if(dvtemp2.virtmsgDelaysList.get(0).time==Global.weekMin)
                    dvtemp2.virtmsgDelaysList.remove(0);
                }              
              }
              check=true; 
              dvtemp1.AddVirtDelay(virtualdelay,ms1.duration*bw,itime.getTime()+(long)(ms1.duration*1000));
              dvtemp2.AddVirtDelay(virtualdelay,ms1.duration*bw,itime.getTime()+(long)(ms1.duration*1000));
            }
           /* else 
              virtualdelay =0;*/
            ms1.lastmeeting = new Date((ttemp.timemax.after(ttemp2.timemax) ? ttemp2.timemax : ttemp.timemax).getTime());
            ms2.lastmeeting = new Date(ms1.lastmeeting.getTime());
                       
          }
          if(check ){
            done[j]=true;           
            ms1 = dtemp.meetStat.get(j);
            ms2 = dtemp2.meetStat.get(i);
            ms1.mypvexchange(dtemp2,false, null,true);
            ms2.mypvexchange(dtemp,false,null,true);             
          }
        }
      }
    }
   
  }
  public void removeCluster(int devlistsize){
    int threshHold=4;
    Device dtemp, dtemp2;
    for (int i = 0; i < devlistsize; i++) {
      dtemp = Peerstable.get(tabdevList.get(i));
      if(dtemp.meetStat.size()<=threshHold)
        continue;
      for (int j = i + 1; j < devlistsize; j++) { 
        dtemp2 = Peerstable.get(tabdevList.get(j));  
        if(dtemp2.meetStat.size()<=threshHold)
          continue;
        if(Math.abs(dtemp.meetStat.size()-dtemp2.meetStat.size())>threshHold)
            continue;
        int common = 0;
        int loop = 0;
        Enumeration<Integer>mskenum = dtemp2.meetStat.keys();
        while(mskenum.hasMoreElements()){
          if(dtemp.meetStat.containsKey(mskenum.nextElement()))
            common++;
          loop++;
          if(loop-common>4)
            break;
        }
        if(loop-common<5){
          dtemp.meetStat.remove(j);
          dtemp2.meetStat.remove(i);
        }
      }
     // removeCongestion(dtemp);
    }
  }
 
  public void setMsgUpdate(Device dtemp){
    for(int m=0;m<dtemp.aliveMsglist.size();m++)
      dtemp.aliveMsglist.get(m).update=true;
  }
  public void checkDuplicateMsg(Device dtemp, Device dtemp2,MeetingStats ms1, MeetingStats ms2, boolean nitem){
    Msg mtemp1,mtemp2;
    LinkedList<Integer>checkedMsg = new LinkedList<Integer>();
    for(int m=0;m<dtemp.aliveMsglist.size();m++){
      mtemp1 = dtemp.aliveMsglist.get(m);
      if(!mtemp1.replicate)
        continue;
      if(mtemp1.delivered)
        continue;      
      if(mtemp1.mynhoplist.containsKey(tabdevList.indexOf(dtemp2.mac)))
          continue;
      if(mtemp1.src == tabdevList.indexOf(dtemp2.mac) | mtemp1.src == tabdevList.indexOf(dtemp.mac))
        continue;
     /*if(!checkedMsg.contains(mtemp1.ID) & mtemp1.replicate){
        if(dtemp2.msgcountec(mtemp1)>=mtemp1.frag_count & dtemp.msgcountec(mtemp1)>=mtemp1.frag_count){
          checkErasureMsg(dtemp, dtemp2, mtemp1);
          checkedMsg.add(mtemp1.ID);
        }
      }*/   
        
      if( mtemp1.BList.containsKey(tabdevList.indexOf(dtemp2.mac)))
        continue;
      for(int n=0;n<dtemp2.aliveMsglist.size();n++){
        mtemp2 = dtemp2.aliveMsglist.get(n);
        if(mtemp2.ID.equals(mtemp1.ID)){// & mtemp2.frag_id.equals(mtemp1.frag_id) ){
          mtemp1.divRatio *= 2.75;
          mtemp1.BList.put(tabdevList.indexOf(dtemp2.mac), tabdevList.indexOf(dtemp2.mac)); 
          break;
        }
      }
   }
   /* for(int m=0;m<dtemp2.aliveMsglist.size();m++){
      mtemp1 = dtemp2.aliveMsglist.get(m);
      if(mtemp1.delivered)
        continue;
      if(ms2.PVector.contains(mtemp1.dest) & nitem )
        mtemp1.update=true;
      if(mtemp1.mynhoplist.containsKey(tabdevList.indexOf(dtemp2.mac)))
          continue;
      if(mtemp1.src == tabdevList.indexOf(dtemp2.mac) | mtemp1.src == tabdevList.indexOf(dtemp.mac))
        continue;
      if(dtemp.msgcountec(mtemp1)>=mtemp1.frag_count & dtemp.msgcountec(mtemp1)>=mtemp1.frag_count)
      {
        for(int n=0;n<dtemp.aliveMsglist.size();n++){
          mtemp2 = dtemp.aliveMsglist.get(n);
          if(mtemp2.ID.equals(mtemp1.ID))
          mtemp2.replicate = false;
        }                  
      }
    
      if( mtemp1.BList.containsKey(tabdevList.indexOf(dtemp.mac)))
        continue;
      for(int n=0;n<dtemp.aliveMsglist.size();n++){
        mtemp2 = dtemp.aliveMsglist.get(n);
        if(mtemp2.ID.equals(mtemp1.ID) ){//& mtemp2.frag_id.equals(mtemp1.frag_id)){
          mtemp1.divRatio *= 3.5;
          mtemp1.BList.put(tabdevList.indexOf(dtemp.mac), tabdevList.indexOf(dtemp.mac));
          //mtemp2.divRatio *= 2;
        }
      }
    }*/
  }
  public void checkErasureMsg(Device dtemp, Device dtemp2,Msg mtemp1){
    Msg mtemp2= new Msg(0,0,0,0,0,0,0,0);
   
    for(int n=0;n<dtemp2.aliveMsglist.size();n++){
      mtemp2 = dtemp2.aliveMsglist.get(n);
      if(mtemp2.ID.equals(mtemp1.ID))
        break;
    }
    
  // if(dtemp2.msgcountec(mtemp1)<=dtemp.msgcountec(mtemp1))
    if(mtemp2.value>=mtemp1.value)
    {
      for(int n=0;n<dtemp2.aliveMsglist.size();n++){
        mtemp2 = dtemp2.aliveMsglist.get(n);
        if(!mtemp2.replicate )          
          continue;
        
        if(mtemp2.ID.equals(mtemp1.ID)){
          mtemp2.replicate = false;
          mtemp2.mynhoplist =new Hashtable<Integer, PossibleHop>();
        }
      }
    }
   else{ 
      if(!mtemp2.replicate)
        return;
      for(int n=0;n<dtemp.aliveMsglist.size();n++){
        mtemp2 = dtemp.aliveMsglist.get(n);
        if(!mtemp2.replicate)
          continue;
        if(mtemp2.ID.equals(mtemp1.ID)){
          mtemp2.replicate = false;  
          mtemp2.mynhoplist =new Hashtable<Integer, PossibleHop>();
        }
      }          
    }
  }
  public static double Round(double Rval, int Rpl) {
    double p = (double)Math.pow(10,Rpl);
    Rval = Rval * p;
    double tmp = Math.round(Rval);
    return (double)tmp/p;
      }
//  public void NileHistory2(Date T, Date T2, int ap_count, int msgtrack, double bw, boolean dFlood) {
//
//    int devlistsize = Peerstable.size() - ap_count ;
//    Device dtemp, dtemp2;
//    TSlot ttemp = new TSlot(0, 0, "");
//    TSlot ttemp2 = new TSlot(0, 0, "");
//    MeetingStats ms1, ms2;
//    boolean nitem;
//    dFlood = false;
//
//    
//    for (int i = 0; i < devlistsize; i++) {
//      dtemp = Peerstable.get(tabdevList.get(i));
//      boolean []done = new boolean[devlistsize];
//      int list_size1 = dtemp.sorted_slots.size();
//      for (int k = dtemp.find_slot(T2, 0, dtemp.sorted_slots.size() - 1); k > -1
//          & k < list_size1; k++) {
//        ttemp = dtemp.sorted_slots.get(k);
//        if(!ttemp.real)
//          continue;
//        if (ttemp.timemin.after(T)) {
//          break;
//        }
//        
//        for (int j = i + 1; j < devlistsize; j++) {          
//            if(done[j])
//              continue;
//          dtemp2 = Peerstable.get(tabdevList.get(j));
//          boolean check = false;
//          nitem= false;
//          Date itime = new Date();
//          int list_size2 = dtemp2.sorted_slots.size();
//          for (int l = dtemp2.find_slot(ttemp.timemin, 0, dtemp2.sorted_slots
//              .size() - 1); l > -1 & l < list_size2; l++) {
//            ttemp2 = dtemp2.sorted_slots.get(l);
//            if (ttemp.timemax.before(ttemp2.timemin))
//              break;
//            if (!ttemp.apname.equals(ttemp2.apname))
//              continue;
//            if (!ttemp.isoverlap(ttemp2))
//              continue;
//            itime = ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin
//                : ttemp.timemin;
//            if (!dtemp.meetStat.containsKey(new Integer(j))) {
//              ms1 = new MeetingStats( new Integer(j),new Integer(i),bw);
//              ms2=new MeetingStats(new Integer(i),new Integer(j),bw);
//              dtemp.meetStat.put(new Integer(j), ms1);
//              dtemp2.meetStat.put(new Integer(i), ms2);    
//              nitem=true;
//            }
//            else{
//              ms1 = dtemp.meetStat.get(j);
//              ms2 = dtemp2.meetStat.get(i);
//            }
//            
//             ms1.myMsgStatsExchange(dtemp,dtemp2,msgtrack);
//             ms2.myMsgStatsExchange(dtemp2,dtemp,msgtrack);
//            
//            check=true; 
//            //break;
//            
//            ms1.meetingcnt++;
//            ms2.meetingcnt++;
//            
//            /*ms1.duration += ((ttemp.timemax.after(ttemp2.timemax) ? ttemp2.timemax
//                : ttemp.timemax).getTime() - itime.getTime()) / 1000;
//            ms2.duration += ms1.duration;*/
//            if(ms1.meetingcnt>1){
//              double virtualdelay = itime.getTime()-ms1.lastmeeting.getTime() /(double) 1000;
//              ms1.duration += ((ttemp.timemax.after(ttemp2.timemax) ? ttemp2.timemax
//                  : ttemp.timemax).getTime() - itime.getTime()) / 1000;
//              ms1.PVector.get(j).virtmsgDelaysList.add(new TDelayFactor(virtualdelay,ms1.duration*bw,itime.getTime()+(long)(ms1.duration*1000)));
//              ms2.PVector.get(i).virtmsgDelaysList.add(new TDelayFactor(virtualdelay,ms1.duration*bw,itime.getTime()+(long)(ms1.duration*1000)));
//            
//            }
//           
//            ms1.lastmeeting = new Date(
//                (ttemp.timemax.after(ttemp2.timemax) ? ttemp2.timemax
//                    : ttemp.timemax).getTime());
//            
//          }
//          if(check ){
//            done[j]=true;
//            //dtemp.normalisingfact();
//            //dtemp2.normalisingfact();
//            ms1 = dtemp.meetStat.get(j);
//            ms2 = dtemp2.meetStat.get(i);
//           
//            ms1.mypvexchange(dtemp2,dFlood, ttemp.timemin);
//            ms2.mypvexchange(dtemp,dFlood,ttemp.timemin); 
//            if(nitem & !dFlood){
//              for(int m=0;m<dtemp.aliveMsglist.size();m++){
//                if(ms1.PVector.contains(dtemp.aliveMsglist.get(m).dest))
//                  dtemp.aliveMsglist.get(m).update=true;
//              }
//              for(int m=0;m<dtemp2.aliveMsglist.size();m++){
//                if(ms1.PVector.contains(dtemp2.aliveMsglist.get(m).dest))
//                  dtemp2.aliveMsglist.get(m).update=true;
//              }             
//            }
//          }
//        }
//      }
//    }
//  }

  public void construct_graphdjpaths(int devlistsize){
    Device dtemp, dtemp2;
    TSlot ttemp = new TSlot(0, 0, "");
    TSlot ttemp2 = new TSlot(0, 0, "");
    MeetingStats ms1, ms2;
    boolean check=false;
    for (int i = 0; i < devlistsize; i++) {
      dtemp = Peerstable.get(tabdevList.get(i));
      //System.out.println(dtemp.mac);
      int list_size1 = dtemp.sorted_slots.size();
      for (int k = 0;    k < list_size1 ; k++) {
        ttemp = dtemp.sorted_slots.get(k);
        for (int j = i + 1; j < devlistsize; j++) {
            dtemp2 = Peerstable.get(tabdevList.get(j));  
           // System.out.println(dtemp2.mac);
            check=false;
            Date itime = new Date();
            int list_size2 = dtemp2.sorted_slots.size();
            for (int l = dtemp2.find_slot(ttemp.timemin, 0, dtemp2.sorted_slots
              .size() - 1); l > -1 & l < list_size2 ; l++) {
              ttemp2 = dtemp2.sorted_slots.get(l);
              if (ttemp.timemax.before(ttemp2.timemin))
                break;
              if (!ttemp.apname.equals(ttemp2.apname))
                continue;
              if (!ttemp.isoverlap(ttemp2))
                continue;
              itime = ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin
                  : ttemp.timemin;
              if (!dtemp.meetStat.containsKey(new Integer(j))) {
                ms1 = new MeetingStats( new Integer(j),new Integer(i));
                ms2=new MeetingStats( new Integer(i),new Integer(j));
                dtemp.meetStat.put(new Integer(j), ms1);
                dtemp2.meetStat.put(new Integer(i), ms2);
                
              }
              else{
                ms1 = dtemp.meetStat.get(j);
                ms2 = dtemp2.meetStat.get(i);
              }
  
              ms1.meetingcnt++;
              ms2.meetingcnt++;
              
              double wttime = (double)(itime.getTime() - ms1.lastmeeting.getTime()) / 60000;
              if (ms1.meetingcnt > 1) {
                ms1.waitTime += wttime;
                ms2.waitTime += wttime;
                if(wttime>ms1.maxWaitTime)
                  ms1.maxWaitTime=wttime;
                if(wttime>ms2.maxWaitTime)
                  ms2.maxWaitTime=wttime;
                
               
                check=true;    
                ms1.duration += ((ttemp.timemax.after(ttemp2.timemax) ? ttemp2.timemax
                    : ttemp.timemax).getTime() - itime.getTime()) / 60000;
                ms2.duration += ms1.duration;
               
              if(wttime<ms1.minwaitTime)
              {
                ms1.minwaitTime=wttime;
                ms2.minwaitTime=wttime;
              }
            }
            ms1.lastmeeting = new Date(
                (ttemp.timemax.after(ttemp2.timemax) ? ttemp2.timemax
                    : ttemp.timemax).getTime());
            ms2.lastmeeting = new Date(ms1.lastmeeting.getTime());
          }
          if(check){
            //dtemp.normalisingfact();
            //dtemp2.normalisingfact();
            ms1 = dtemp.meetStat.get(j);
            ms2 = dtemp2.meetStat.get(i);
            
           // ms1.mypvInsertVal(ms2, metricSelect, dtemp.totalmeetcount,itime);
           // ms1.mypvexchange(dtemp2,false,null);
                 
           // ms2.mypvInsertVal(ms1, metricSelect, dtemp2.totalmeetcount,itime);
            //ms2.mypvexchange(dtemp,false,null);
          }
        }
      }          
    }    
    for(int i=0;i<devlistsize;i++){
      dtemp = Peerstable.get(tabdevList.get(i));
      for(int j=0;j<devlistsize;j++){
        if(i==j)
          continue;
          
        if(dtemp.meetStat.containsKey(j)){
          dtemp.totalmeetcount+=dtemp.meetStat.get(j).meetingcnt;
          dtemp.tonlinetime+=dtemp.meetStat.get(j).duration;
        }
      }
    }
  }
  public void construct_graphdjpathsT(int devlistsize,Date T){
    Device dtemp, dtemp2;
    long netwaitingtime=0;
    TSlot ttemp = new TSlot(0, 0, "");
    TSlot ttemp2 = new TSlot(0, 0, "");
    MeetingStats ms1, ms2;
    boolean check=false;
    for (int i = 0; i < devlistsize; i++) {
      dtemp = Peerstable.get(tabdevList.get(i));
      //System.out.println(dtemp.mac);
      int list_size1 = dtemp.sorted_slots.size();
      for (int k = 0;    k < list_size1 ; k++) {
        ttemp = dtemp.sorted_slots.get(k);
        if(ttemp.timemin.after(T))
          break;
        for (int j = i + 1; j < devlistsize; j++) {
            dtemp2 = Peerstable.get(tabdevList.get(j));  
           // System.out.println(dtemp2.mac);
            check=false;
            Date itime = new Date();
            int list_size2 = dtemp2.sorted_slots.size();
            for (int l = dtemp2.find_slot(ttemp.timemin, 0, dtemp2.sorted_slots
              .size() - 1); l > -1 & l < list_size2 ; l++) {
              ttemp2 = dtemp2.sorted_slots.get(l);
              if (ttemp.timemax.before(ttemp2.timemin))
                break;
              if (!ttemp.apname.equals(ttemp2.apname))
                continue;
              if (!ttemp.isoverlap(ttemp2))
                continue;
              itime = ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin
                  : ttemp.timemin;
              if (!dtemp.meetStat.containsKey(new Integer(j))) {
                ms1 = new MeetingStats( new Integer(j),new Integer(i));
                ms2=new MeetingStats( new Integer(i),new Integer(j));
                dtemp.meetStat.put(new Integer(j), ms1);
                dtemp2.meetStat.put(new Integer(i), ms2);
                
              }
              else{
                ms1 = dtemp.meetStat.get(j);
                ms2 = dtemp2.meetStat.get(i);
              }
  
              ms1.meetingcnt++;
              ms2.meetingcnt++;
              
              double wttime = (double)(itime.getTime() - ms1.lastmeeting.getTime()) / 60000;
              if (ms1.meetingcnt > 1) {
                ms1.waitTime += wttime;
                ms2.waitTime += wttime;
                netwaitingtime+=wttime;
                
                if(wttime>ms1.maxWaitTime)
                  ms1.maxWaitTime=wttime;
                if(wttime>ms2.maxWaitTime)
                  ms2.maxWaitTime=wttime;
               
                check=true;    
                ms1.duration += ((ttemp.timemax.after(ttemp2.timemax) ? ttemp2.timemax
                    : ttemp.timemax).getTime() - itime.getTime()) / 60000;
                ms2.duration += ms1.duration;
               
              if(wttime<ms1.minwaitTime)
              {
                ms1.minwaitTime=wttime;
                ms2.minwaitTime=wttime;
              }
            }
            ms1.lastmeeting = new Date(
                (ttemp.timemax.after(ttemp2.timemax) ? ttemp2.timemax
                    : ttemp.timemax).getTime());
            ms2.lastmeeting = new Date(ms1.lastmeeting.getTime());
          }
          if(check){
            //dtemp.normalisingfact();
            //dtemp2.normalisingfact();
            ms1 = dtemp.meetStat.get(j);
            ms2 = dtemp2.meetStat.get(i);
            
           // ms1.mypvInsertVal(ms2, metricSelect, dtemp.totalmeetcount,itime);
           // ms1.mypvexchange(dtemp2,false,null);
                 
           // ms2.mypvInsertVal(ms1, metricSelect, dtemp2.totalmeetcount,itime);
            //ms2.mypvexchange(dtemp,false,null);
          }
        }
      }          
    }    
    for(int i=0;i<devlistsize;i++){
      dtemp = Peerstable.get(tabdevList.get(i));
      for(int j=0;j<devlistsize;j++){
        if(i==j)
          continue;
          
        if(dtemp.meetStat.containsKey(j)){
          dtemp.totalmeetcount+=dtemp.meetStat.get(j).meetingcnt;
          dtemp.tonlinetime+=dtemp.meetStat.get(j).duration;
        }
      }
    }
    System.out.println("Avg Waiting time="+netwaitingtime);
  }
 /* public void fillinmyvectorpath(int dest, int devlistsize, int c) {
    Device dtemp = Peerstable.get(tabdevList.get(dest));
    Enumeration<Integer>mkey = dtemp.mstat.keys();
    while(mkey.hasMoreElements()){
      propvector(Peerstable.get(tabapList.get(mkey.nextElement())), c);
    }    
  }
  
  public void propvector(Device dtemp, int c){
  
    Enumeration<Integer>mkey = dtemp.mstat.keys();
    MeetingStats ms;
    while(mkey.hasMoreElements()){
      Integer key = mkey.nextElement();
      ms=dtemp.mstat.get(key);
      Enumeration <Integer>mkey2 = dtemp.mstat.keys();
      while(mkey2.hasMoreElements()){
        Integer key2 = mkey2.nextElement();
        if(key.equals(key2) | ms.PVector.contains(key2))
          continue;
        ms.mypvInsertVal(key2, c, dtemp.totalmeetcount,ms.PVector.get(key2));
        MeetingStats ms2 = dtemp.mstat.get(key2);
      }
    }    
  }*/

 /* public void mergebloomfilter(MeetingStats ms1, MeetingStats ms2){

    Enumeration<Integer> kenum2= ms2.mbf.hbloom.keys();
    BloomValue bvtemp1,bvtemp2;
    Integer key;

    while(kenum2.hasMoreElements()){
      key = kenum2.nextElement();
      bvtemp2 = ms2.mbf.hget(key);
      if(bvtemp2.depth==0)
        continue;
      bvtemp1 = ms1.mbf.hget(key);
      if(bvtemp1==null)
        ms1.mbf.hput(key, new BloomValue(bvtemp2.depth+1, bvtemp2.proximity/(bvtemp2.depth*5)));
      else if(bvtemp1.proximity<bvtemp2.proximity){
        ms1.mbf.hreplace(key, new BloomValue(bvtemp2.depth+1, bvtemp2.proximity/(bvtemp2.depth*5)));
 
      }        
    }     
  }*/
  
  public void construct_graphbloom( int ap_count){
      int devlistsize = Peerstable.size() - ap_count -1;
      Device dtemp, dtemp2;
      TSlot ttemp = new TSlot(0, 0, "");
      TSlot ttemp2 = new TSlot(0, 0, "");
      MeetingStats ms1, ms2;
      
      for (int i = 0; i < devlistsize; i++) {
        dtemp = Peerstable.get(tabdevList.get(i));
        
        
        for (int j = i + 1; j < devlistsize; j++) {
          dtemp2 = Peerstable.get(tabdevList.get(j));
          int list_size1 = dtemp.sorted_slots.size();
        
          for (int k = 0;    k < list_size1; k++) {
            ttemp = dtemp.sorted_slots.get(k);
            int list_size2 = dtemp2.sorted_slots.size();
          
            for (int l = dtemp2.find_slot(ttemp.timemin, 0, dtemp2.sorted_slots
                .size() - 1); l > -1 & l < list_size2; l++) {
              ttemp2 = dtemp2.sorted_slots.get(l);
            
              if (ttemp.timemax.before(ttemp2.timemin))
                break;
              if (!ttemp.apname.equals(ttemp2.apname))
                continue;
              if (!ttemp.isoverlap(ttemp2))
                continue;
              Date itime = ttemp.timemin.before(ttemp2.timemin) ? ttemp2.timemin
                  : ttemp.timemin;
            
             
              if (!dtemp.meetStat.containsKey(j)) {
                dtemp.meetStat.put(j, new MeetingStats( -1,-1));
                dtemp2.meetStat.put(i, new MeetingStats(-1,-1));
              }
              ms1 = dtemp.meetStat.get(j);
              ms2 = dtemp2.meetStat.get(i);

       
              double wttime = (itime.getTime() - ms1.lastmeeting.getTime()) / 60000;
              ms1.meetingcnt++;
              ms2.meetingcnt++;
              ms1.duration += ((ttemp.timemax.after(ttemp2.timemax) ? ttemp.timemax
                  : ttemp2.timemax).getTime() - itime.getTime()) / 60000;
              ms2.duration += ms1.duration;
              if (ms1.meetingcnt > 1 & wttime>60) {
                ms1.waitTime += wttime;
                ms2.waitTime += wttime;
              }
              ms1.lastmeeting = new Date(
                  (ttemp.timemax.after(ttemp2.timemax) ? ttemp2.timemax
                      : ttemp.timemax).getTime());
              ms2.lastmeeting = new Date(ms1.lastmeeting.getTime());     
           
            }          
        }
      }
    }
 }  

}
