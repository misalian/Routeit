
package de.unikn.inf.disy.nuntifix.dtn.simulator.bloomfilter;

import java.util.Enumeration;
import java.util.LinkedList;

import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Device;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.MeetingStats;

/*
 * ************************************************************************* *
 * General Purpose Hash Function Algorithms Library * * Author: Arash Partow -
 * 2002 * URL: http://www.partow.net * URL:
 * http://www.partow.net/programming/hashfunctions/index.html * * Copyright
 * notice: * Free use of the General Purpose Hash Function Algorithms Library is *
 * permitted under the guidelines and in accordance with the most current *
 * version of the Common Public License. *
 * http://www.opensource.org/licenses/cpl.php * *
 * *************************************************************************
 */

class BloomFilterLibrary {

  float tune = (float) 0.6;

  public long RSHash(String str) {

    int b = 378551;
    int a = 63689;
    long hash = 0;

    for (int i = 0; i < str.length(); i++) {
      hash = hash * a + str.charAt(i);
      a = a * b;
    }

    return Math.abs(hash);
  }

  /* End Of RS Hash Function */

  public long JSHash(String str) {

    long hash = 1315423911;

    for (int i = 0; i < str.length(); i++) {
      hash ^= ((hash << 5) + str.charAt(i) + (hash >> 2));
    }
    return Math.abs(hash);
  }

  /* End Of JS Hash Function */

  public long PJWHash(String str) {

    long BitsInUnsignedInt = (long) (4 * 8);
    long ThreeQuarters = (long) ((BitsInUnsignedInt * 3) / 4);
    long OneEighth = (long) (BitsInUnsignedInt / 8);
    long HighBits = (long) (0xFFFFFFFF) << (BitsInUnsignedInt - OneEighth);
    long hash = 0;
    long test = 0;

    for (int i = 0; i < str.length(); i++) {
      hash = (hash << OneEighth) + str.charAt(i);

      if ((test = hash & HighBits) != 0) {
        hash = ((hash ^ (test >> ThreeQuarters)) & (~HighBits));
      }
    }

    return Math.abs(hash);
  }

  /* End Of P. J. Weinberger Hash Function */

  public long ELFHash(String str) {

    long hash = 0;
    long x = 0;

    for (int i = 0; i < str.length(); i++) {
      hash = (hash << 4) + str.charAt(i);

      if ((x = hash & 0xF0000000L) != 0) {
        hash ^= (x >> 24);
      }
      hash &= ~x;
    }

    return Math.abs(hash);
  }

  /* End Of ELF Hash Function */

  public long BKDRHash(String str) {

    long seed = 131; // 31 131 1313 13131 131313 etc..
    long hash = 0;

    for (int i = 0; i < str.length(); i++) {
      hash = (hash * seed) + str.charAt(i);
    }

    return Math.abs(hash);
  }

  /* End Of BKDR Hash Function */

  public long SDBMHash(String str) {

    long hash = 0;

    for (int i = 0; i < str.length(); i++) {
      hash = str.charAt(i) + (hash << 6) + (hash << 16) - hash;
    }

    return Math.abs(hash);
  }

  /* End Of SDBM Hash Function */

  public long DJBHash(String str) {

    long hash = 5381;

    for (int i = 0; i < str.length(); i++) {
      hash = ((hash << 5) + hash) + str.charAt(i);
    }

    return Math.abs(hash);
  }

  /* End Of DJB Hash Function */

  public long DEKHash(String str) {

    long hash = str.length();

    for (int i = 0; i < str.length(); i++) {
      hash = ((hash << 5) ^ (hash >> 27)) ^ str.charAt(i);
    }

    return Math.abs(hash);
  }

  /* End Of DEK Hash Function */

  public long BPHash(String str) {

    long hash = 0;

    for (int i = 0; i < str.length(); i++) {
      hash = hash << 7 ^ str.charAt(i);
    }

    return Math.abs(hash);
  }

  /* End Of BP Hash Function */

  public long FNVHash(String str) {

    long fnv_prime = 0x811C9DC5;
    long hash = 0;

    for (int i = 0; i < str.length(); i++) {
      hash *= fnv_prime;
      hash ^= str.charAt(i);
    }

    return Math.abs(hash);
  }

  /* End Of FNV Hash Function */

  public long APHash(String str) {

    long hash = 0xAAAAAAAA;

    for (int i = 0; i < str.length(); i++) {
      if ((i & 1) == 0) {
        hash ^= ((hash << 7) ^ str.charAt(i) ^ (hash >> 3));
      } else {
        hash ^= (~((hash << 11) ^ str.charAt(i) ^ (hash >> 5)));
      }
    }

    return Math.abs(hash);
  }

  /* End Of AP Hash Function */
  /*
   * public void UpdateVal(Device dtemp, Device dtemp2,float bval,LinkedList
   * tabdevList){ int bfilterlength = dtemp.bloom[0].size(); switch
   * ((int)Math.round((Math.log10(bfilterlength)/Math.log10(2))*tune)){ case 11:
   * if(dtemp.bloom[0].get((int)(RSHash(dtemp2.mac)%bfilterlength)).proximity<bval)
   * dtemp.bloom[0].set( (int)(RSHash(dtemp2.mac)%bfilterlength), new
   * BloomValue(tabdevList.indexOf(dtemp2.mac),bval)); case 10:
   * if(dtemp.bloom[0].get((int)((JSHash(dtemp2.mac)%bfilterlength))).proximity<bval)
   * dtemp.bloom[0].set( (int)((JSHash(dtemp2.mac)%bfilterlength)), new
   * BloomValue(tabdevList.indexOf(dtemp2.mac),bval)); case 9:
   * if(dtemp.bloom[0].get((int)((PJWHash(dtemp2.mac)%bfilterlength))).proximity<bval)
   * dtemp.bloom[0].set( (int)((PJWHash(dtemp2.mac)%bfilterlength)), new
   * BloomValue(tabdevList.indexOf(dtemp2.mac),bval)); case 8:
   * if(dtemp.bloom[0].get((int)((ELFHash(dtemp2.mac)%bfilterlength))).proximity<bval)
   * dtemp.bloom[0].set( (int)((ELFHash(dtemp2.mac)%bfilterlength)), new
   * BloomValue(tabdevList.indexOf(dtemp2.mac),bval)); case 7:
   * if(dtemp.bloom[0].get((int)((BKDRHash(dtemp2.mac)%bfilterlength))).proximity<bval)
   * dtemp.bloom[0].set( (int)((BKDRHash(dtemp2.mac)%bfilterlength)), new
   * BloomValue(tabdevList.indexOf(dtemp2.mac),bval)); case 6:
   * if(dtemp.bloom[0].get((int)((DJBHash(dtemp2.mac)%bfilterlength))).proximity<bval)
   * dtemp.bloom[0].set( (int)((DJBHash(dtemp.mac)%bfilterlength)), new
   * BloomValue(tabdevList.indexOf(dtemp2.mac),bval)); case 5:
   * if(dtemp.bloom[0].get((int)((DEKHash(dtemp2.mac)%bfilterlength))).proximity<bval)
   * dtemp.bloom[0].set( (int)((DEKHash(dtemp2.mac)%bfilterlength)), new
   * BloomValue(tabdevList.indexOf(dtemp2.mac),bval)); case 4:
   * if(dtemp.bloom[0].get((int)((BPHash(dtemp2.mac)%bfilterlength))).proximity<bval)
   * dtemp.bloom[0].set( (int)((BPHash(dtemp2.mac)%bfilterlength)), new
   * BloomValue(tabdevList.indexOf(dtemp2.mac),bval)); case 3:
   * if(dtemp.bloom[0].get((int)(FNVHash(dtemp2.mac)%bfilterlength)).proximity<bval)
   * dtemp.bloom[0].set( (int)((FNVHash(dtemp2.mac)%bfilterlength)), new
   * BloomValue(tabdevList.indexOf(dtemp2.mac),bval)); case 2:
   * if(dtemp.bloom[0].get((int)((SDBMHash(dtemp2.mac)%bfilterlength))).proximity<bval)
   * dtemp.bloom[0].set( (int)((SDBMHash(dtemp2.mac)%bfilterlength)), new
   * BloomValue(tabdevList.indexOf(dtemp2.mac),bval)); case 1:
   * if(dtemp.bloom[0].get((int)((APHash(dtemp2.mac)%bfilterlength))).proximity<bval)
   * dtemp.bloom[0].set( (int)((APHash(dtemp2.mac)%bfilterlength)), new
   * BloomValue(tabdevList.indexOf(dtemp2.mac),bval)); break; } }
   */

  /*
   * public int []GetHash(Device dtemp, String mac){ int bfilterlength =
   * dtemp.bloom[0].size(); int []hashval = new int
   * [(int)Math.round((Math.log10(bfilterlength)/Math.log10(2))*tune)]; switch
   * ((int)Math.round((Math.log10(bfilterlength)/Math.log10(2))*tune)){ case 11:
   * hashval[10]= (int)(RSHash(mac)%bfilterlength); case 10: hashval[9]=
   * (int)(JSHash(mac)%bfilterlength); case 9: hashval[8]=
   * (int)(PJWHash(mac)%bfilterlength); case 8: hashval[7]=
   * (int)(ELFHash(mac)%bfilterlength); case 7: hashval[6]=
   * (int)(BKDRHash(mac)%bfilterlength); case 6: hashval[5]=
   * (int)(DJBHash(mac)%bfilterlength); case 5: hashval[4]=
   * (int)(DEKHash(mac)%bfilterlength); case 4: hashval[3]=
   * (int)(BPHash(mac)%bfilterlength); case 3: hashval[2]=
   * (int)(FNVHash(mac)%bfilterlength); case 2: hashval[1]=
   * (int)(SDBMHash(mac)%bfilterlength); case 1: hashval[0]=
   * (int)(APHash(mac)%bfilterlength); break; } return hashval; } void
   * ExchangeBF(Device dtemp, Device dtemp2,int path_length,LinkedList
   * tabdevList){ int size1 = dtemp.bloom[0].size(); int size2=
   * dtemp2.bloom[0].size(); for (int i=0;i<path_length-1;i++){ for(int j=0;j<size1;j++){
   * if(dtemp.bloom[i+1].get(j).proximity<dtemp2.bloom[i].get(j).proximity)
   * dtemp.bloom[i+1].set(j, new
   * BloomValue(tabdevList.indexOf(dtemp2.mac),dtemp2.bloom[i].get(j).proximity)); }
   * for(int j=0;j<size2;j++){ if(dtemp2.bloom[i+1].get(j).proximity<dtemp.bloom[i].get(j).proximity)
   * dtemp2.bloom[i+1].set(j, new
   * BloomValue(tabdevList.indexOf(dtemp.mac),dtemp.bloom[i].get(j).proximity)); } } }
   * public void AttenuateBF(Device dtemp, int path_len){ int
   * size=dtemp.bloom[0].size(); for(int i=0;i<path_len-1;i++){ for(int j=i+1;j<path_len;j++){
   * for(int k=0;k<size;k++){
   * if(dtemp.bloom[i].get(k).proximity>dtemp.bloom[j].get(k).proximity)
   * dtemp.bloom[j].set(k, dtemp.bloom[i].get(k)); } } } } void
   * AdjustBloomFilter(float val,Device dtemp,Device dtemp2,int path_length,int
   * optBFlen, LinkedList tabdevList){ /*int deslen= (int)Math.pow(2, optBFlen);
   * for(int j=0;j<path_length;j++){ if(dtemp.bloom[j].size()<deslen) {
   * for(int i=dtemp.bloom[j].size();i<deslen;i++) dtemp.bloom[j].add(new
   * BloomValue(-1,0)); } int size1=dtemp.bloom[j].size(); int
   * size2=dtemp2.bloom[j].size(); if(size2>size1) { for(double i=size1;i<size2;i+=1)
   * dtemp.bloom[j].add(new BloomValue(-1,0)); } if(size2<size1){ for(double
   * i=size2;i<size1;i+=1) dtemp2.bloom[j].add(new BloomValue(-1,0)); } }
   * UpdateVal(dtemp,dtemp2,10/val,tabdevList);
   * UpdateVal(dtemp2,dtemp,10/val,tabdevList);
   * ExchangeBF(dtemp,dtemp2,path_length); AttenuateBF(dtemp,path_length);
   * AttenuateBF(dtemp2,path_length);/ }
   */
  public static double getBvalue(MeetingStats ms) {

    double avgwtime = ms.waitTime / ms.meetingcnt;

    if (avgwtime > 8000)
      return 0;
    return (1 / (double) Math.log(avgwtime + 1.1));// *Math.log((ms.meetingcnt+0.718281828459045))*Math.log(ms.duration);
  }

  public static double normalisingfact(Device dtemp2) {

    Enumeration denum2 = dtemp2.meetStat.keys();
    double total = 0;
    while (denum2.hasMoreElements())
      total += dtemp2.meetStat.get(denum2.nextElement()).meetingcnt;
    return total;
  }

  public void UpdateVal(MeetingStats ms, Device dtemp2, int path_length,
      int bfilterlength, LinkedList tabdevList) {

    MeetingStats ms2;
    double total = normalisingfact(dtemp2);
    Enumeration denum2 = dtemp2.meetStat.keys();

    while (denum2.hasMoreElements()) {
      String mac = (String) denum2.nextElement();
      ms2 = dtemp2.meetStat.get(mac);
      /*
       * if(ms2.meetingcnt==1) continue;
       */
      // double value = getBvalue(ms2);
      double value = ms2.meetingcnt / total;
    }

    /*
     * if(ms.getval((int)(RSHash(mac)%bfilterlength))<value)
     * ms.insertval((int)(RSHash(mac)%bfilterlength),0,value);
     */
    /*
     * if(value==0) continue; if(ms.getval((int)(JSHash(mac)%bfilterlength))<value)
     * ms.insertval((int)(JSHash(mac)%bfilterlength),1,value);
     * if(ms.getval((int)(PJWHash(mac)%bfilterlength))<value)
     * ms.insertval((int)(PJWHash(mac)%bfilterlength),1,value);
     * if(ms.getval((int)(ELFHash(mac)%bfilterlength))<value)
     * ms.insertval((int)(ELFHash(mac)%bfilterlength),1,value);
     * if(ms.getval((int)(BKDRHash(mac)%bfilterlength))<value)
     * ms.insertval((int)(BKDRHash(mac)%bfilterlength),1,value);
     * if(ms.getval((int)(SDBMHash(mac)%bfilterlength))<value)
     * ms.insertval((int)(SDBMHash(mac)%bfilterlength),1,value);
     * if(ms.getval((int)(DJBHash(mac)%bfilterlength))<value)
     * ms.insertval((int)(DJBHash(mac)%bfilterlength),1,value);
     * if(ms.getval((int)(DEKHash(mac)%bfilterlength))<value)
     * ms.insertval((int)(DEKHash(mac)%bfilterlength),1,value);
     * if(ms.getval((int)(BPHash(mac)%bfilterlength))<value)
     * ms.insertval((int)(BPHash(mac)%bfilterlength),1,value);
     * if(ms.getval((int)(FNVHash(mac)%bfilterlength))<value)
     * ms.insertval((int)(FNVHash(mac)%bfilterlength),1,value);
     * if(ms.getval((int)(APHash(mac)%bfilterlength))<value)
     * ms.insertval((int)(APHash(mac)%bfilterlength),1,value); }
     * /////////////////////////////////////////////////////////////// /*int
     * bfilterlength = dtemp.mstat.get(dtemp2.mac).bloom[0].length; MeetingStats
     * ms,ms2; ms = dtemp.mstat.get(dtemp2.mac); Enumeration denum2 =
     * dtemp2.mstat.keys(); while(denum2.hasMoreElements()){ String mac =
     * (String)denum2.nextElement(); ms2 = dtemp2.mstat.get(mac);
     * if(ms2.meetingcnt<2) continue; if(ms2.waitTime==0) ms2.waitTime++; float
     * value=0; /*int j = ms2.waitingtime.length>ms2.meetingcnt ?
     * ms2.meetingcnt:ms2.waitingtime.length; for(int i=0;i<j;i++) value +=
     * ms2.waitingtime[i]; value /=j;
     */
    /*
     * value = (float)(1/(ms2.waitTime/ms2.meetingcnt));//' +
     * (float)(0.0001*ms2.duration/ms2.meetingcnt); if(value>5) value=value+1-1;
     */

    /*
     * if(ms.bloom[0][(int)(RSHash(mac)%bfilterlength)]<value)
     * ms.bloom[0][(int)(RSHash(mac)%bfilterlength)] =value;
     * if(ms.bloom[0][(int)(JSHash(mac)%bfilterlength)]<value)
     * ms.bloom[0][(int)(JSHash(mac)%bfilterlength)] =value;
     * if(ms.bloom[0][(int)(PJWHash(mac)%bfilterlength)]<value)
     * ms.bloom[0][(int)(PJWHash(mac)%bfilterlength)] =value;
     * if(ms.bloom[0][(int)(ELFHash(mac)%bfilterlength)]<value)
     * ms.bloom[0][(int)(ELFHash(mac)%bfilterlength)] =value;
     * if(ms.bloom[0][(int)(BKDRHash(mac)%bfilterlength)]<value)
     * ms.bloom[0][(int)(BKDRHash(mac)%bfilterlength)] =value;
     * if(ms.bloom[0][(int)(DJBHash(mac)%bfilterlength)]<value)
     * ms.bloom[0][(int)(DJBHash(mac)%bfilterlength)] =value;
     * if(ms.bloom[0][(int)(DEKHash(mac)%bfilterlength)]<value)
     * ms.bloom[0][(int)(DEKHash(mac)%bfilterlength)] =value;
     * if(ms.bloom[0][(int)(BPHash(mac)%bfilterlength)]<value)
     * ms.bloom[0][(int)(BPHash(mac)%bfilterlength)] =value;
     * if(ms.bloom[0][(int)(FNVHash(mac)%bfilterlength)]<value)
     * ms.bloom[0][(int)(FNVHash(mac)%bfilterlength)] =value;
     * if(ms.bloom[0][(int)(SDBMHash(mac)%bfilterlength)]<value)
     * ms.bloom[0][(int)(SDBMHash(mac)%bfilterlength)] =value;
     * if(ms.bloom[0][(int)(APHash(mac)%bfilterlength)]<value)
     * ms.bloom[0][(int)(APHash(mac)%bfilterlength)] =value; } denum2 =
     * dtemp2.mstat.keys(); while(denum2.hasMoreElements()){ ms2 =
     * dtemp2.mstat.get(denum2.nextElement()); for(int i=1;i<path_length;i++){
     * for(int k=0;k<bfilterlength;k++){ if( ms.bloom[i][k]<ms2.bloom[i-1][k])
     * ms.bloom[i][k]=ms2.bloom[i-1][k] ; } for(int k=0;k<bfilterlength;k++){
     * if( ms.bloom[i][k]<ms.bloom[i-1][k]) ms.bloom[i][k]=ms.bloom[i-1][k] ; } } }
     */
  }

  public int[] GetHash(int bfilterlength, String mac) {

    int[] hashval = new int[11];
    hashval[10] = (int) (RSHash(mac) % bfilterlength);

    hashval[9] = (int) (JSHash(mac) % bfilterlength);
    hashval[8] = (int) (PJWHash(mac) % bfilterlength);
    hashval[7] = (int) (ELFHash(mac) % bfilterlength);
    hashval[6] = (int) (BKDRHash(mac) % bfilterlength);

    hashval[5] = (int) (SDBMHash(mac) % bfilterlength);
    hashval[4] = (int) (DJBHash(mac) % bfilterlength);
    hashval[3] = (int) (DEKHash(mac) % bfilterlength);
    hashval[2] = (int) (BPHash(mac) % bfilterlength);
    hashval[1] = (int) (FNVHash(mac) % bfilterlength);
    hashval[0] = (int) (APHash(mac) % bfilterlength);

    return hashval;
  }

}
