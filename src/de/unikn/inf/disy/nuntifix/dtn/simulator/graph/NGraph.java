package de.unikn.inf.disy.nuntifix.dtn.simulator.graph;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Date;

import de.unikn.inf.disy.nuntifix.dtn.simulator.Router.ConntimeComparator;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Device;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Dijkstra;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.History;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.MeetingStats;
import de.unikn.inf.disy.nuntifix.dtn.simulator.metaclasses.Msg;


public class NGraph {
  /**
   *  This structure manipulates and analyse the underlying static graph of the network
   */
  public Hashtable<String, Device> Peerstable;

  LinkedList<String> tabdevList;
  LinkedList<String> tabapList;

  History h;
  int GSIZE ;
  int MY_MAX;
  
  //Hashtable<BigDecimal, Msg> gMsgTable;
  
  public NGraph(Hashtable<String, Device> p, LinkedList<String> td,LinkedList<String> ta){
    Peerstable = new Hashtable<String, Device>();
    tabdevList = new LinkedList<String>();
    Peerstable = p;
    tabdevList = td;
    tabapList=ta;
    GSIZE = td.size();//-ta.size();
    MY_MAX= (Integer.MAX_VALUE/3);
   // gMsgTable=gm;
  
  }
  /**
   * This function writes the underlying graph as adjacency matrix to file
   * @param trace tell which network is it i.e. IBM/MIT/MITBT 
   */
  
  public void Centrality(String trace,int w){
    
    String week =new String("");
    if(w!=0)
      week="W"+Integer.toString(w)+"/";
    String line1="",line2="",line3="",line6="",line7="",line8="",line9="",line10="";
    String []frags1 = new String[0];
    String []frags2 = new String[0];
    String []frags3 = new String[0];
    String []frags6 = new String[0];
    String []frags7 = new String[0];
    String []frags8 = new String[0];
    String []frags9 = new String[0];
    String []frags10 = new String[0];
    try{ 
      BufferedReader in1 = new BufferedReader(new FileReader("result/" + trace + "/GRAPH/"
          +week+ "Graph01.txt")); // Continue to read lines while
      BufferedReader in2 = new BufferedReader(new FileReader("result/" + trace + "/GRAPH/"
          +week+ "GraphMeetcnt.txt")); // Continue to read lines while
      BufferedReader in3 = new BufferedReader(new FileReader("result/" + trace + "/GRAPH/"
          +week+ "GWaitTimeDuration.txt")); // Continue to read lines while
      BufferedReader in6 = new BufferedReader(new FileReader("result/" + trace + "/GRAPH/"
          +week+ "GWaitTimeMeetCnt.txt")); // Continue to read lines while
      BufferedReader in7 = new BufferedReader(new FileReader("result/" + trace + "/GRAPH/"
          +week+ "GDuration.txt")); // Continue to read lines while
      BufferedReader in8 = new BufferedReader(new FileReader("result/" + trace + "/GRAPH/"
          +week+ "GWaitDurationMeetingCnt.txt")); // Continue to read lines while
      BufferedReader in9 = new BufferedReader(new FileReader("result/" + trace + "/GRAPH/"
          +week+ "GWaitTime.txt")); // Continue to read lines while
     // BufferedReader in10 = new BufferedReader(new FileReader("result/" + trace + "/GRAPH/"
      //    +week+ "GSPM.txt")); // Continue to read lines while
      float []Graph01 = new float[GSIZE*GSIZE];
      float []gMeetingCnt = new float[GSIZE*GSIZE];
      float []WaitTimeDuration = new float[GSIZE*GSIZE];
      float []WaitTimeMeetingCnt = new float[GSIZE*GSIZE];
      float []Duration = new float[GSIZE*GSIZE];
      float []WaitDurationMeetingCnt = new float[GSIZE*GSIZE];
      float []WaitTime = new float[GSIZE*GSIZE];
      float []SPM = new float[GSIZE*GSIZE];


      int i=0;
      in1.readLine();
      in2.readLine();
      in3.readLine();
      in6.readLine();
      in7.readLine();
      in8.readLine();
      in9.readLine();
     // in10.readLine();
      
      while(in1.ready()){
        
        line1=in1.readLine();
        frags1=line1.split("\t");        
        line2=in2.readLine();
        frags2=line2.split("\t");        
        line3 = in3.readLine();
        frags3 =line3.split("\t");       
        line6 = in6.readLine();
        frags6 =line6.split("\t");   
        line7 = in7.readLine();
        frags7 =line7.split("\t");
        line8 = in8.readLine();
        frags8 =line8.split("\t");
        line9 = in9.readLine();
        frags9 =line9.split("\t");
    //    line10 = in10.readLine();
    //    frags10 =line10.split("\t");
        
        for(int j=0;j<frags1.length;j++){
          Graph01[i*GSIZE+j] = Float.parseFloat(frags1[j]);
          gMeetingCnt[i*GSIZE+j] = Float.parseFloat(frags2[j]);
          WaitTime[i*GSIZE+j] = Float.parseFloat(frags9[j]);          
          Duration[i*GSIZE+j] = Float.parseFloat(frags7[j]);
        //  SPM[i*GSIZE+j] = Float.parseFloat(frags10[j]);


          WaitTimeDuration[i*GSIZE+j] = WaitTime[i*GSIZE+j]/Duration[i*GSIZE+j];
          WaitTimeMeetingCnt[i*GSIZE+j] = WaitTime[i*GSIZE+j]/gMeetingCnt[i*GSIZE+j];
          WaitDurationMeetingCnt[i*GSIZE+j] = WaitTime[i*GSIZE+j]/gMeetingCnt[i*GSIZE+j]*Duration[i*GSIZE+j];
        }
        i++;
      }
      System.out.println(i);
      in1.close();
      in2.close();
      in3.close();
      in6.close();
      in7.close();
      in8.close();
      in9.close();

      //betweenness1D(Graph01,false,new String(trace+"/GRAPH/"+week+"G01betweeness.txt"));
      //betweenness1D(gMeetingCnt,true,new String(trace+"/GRAPH/"+week+"GMeetingCntbetweeness.txt"));
      //betweenness1D(WaitTimeDuration,true,new String(trace+"/GRAPH/"+week+"GWaitTimeDurabetw.txt"));
      //betweenness1D(WaitTimeMeetingCnt,true,new String(trace+"/GRAPH/"+week+"GWaitTimeMeetCntbetw.txt"));
      //betweenness1D(Duration,true,new String(trace+"/GRAPH/"+week+"GDurationbetw.txt"));
      //betweenness1D(WaitDurationMeetingCnt,true,new String(trace+"/GRAPH/"+week+"GWaitDuraMeetCntbetw.txt"));
     // betweenness1D(WaitTime,true,new String(trace+"/GRAPH/"+week+"GWaitTimebetw.txt"));
      //degreeCentrality(Graph01, new String(trace+"/GRAPH/"+week+"G01Degree.txt"), false);
      //degreeCentrality(gMeetingCnt, new String(trace+"/GRAPH/"+week+"GMeetingCntDegree.txt"), false);
      //degreeCentrality(WaitTimeDuration, new String(trace+"/GRAPH/"+week+"GWaitTDuraDegree.txt"), false);
      //degreeCentrality(WaitTimeMeetingCnt, new String(trace+"/GRAPH/"+week+"GWaitTMeetCntDegree.txt"), false);
      //degreeCentrality(Duration, new String(trace+"/GRAPH/"+week+"GDuraDegree.txt"), false);
      //degreeCentrality(WaitTime, new String(trace+"/GRAPH/"+week+"GWaitTDegree.txt"), false);
      //calculatePageRank(Graph01, new String(trace+"/GRAPH/"+week+"GPageRank.txt"));
      

      betweenness1D(SPM,false,new String(trace+"/GRAPH/"+week+"GSPMbet.txt"));
     // ClosenessCentrality(SPM, new String(trace+"/GRAPH/"+week+"GSPMClose.txt"));
      ClosenessCentrality(gMeetingCnt, new String(trace+"/GRAPH/"+week+"GMeetCntCenClose.txt"));
      ClosenessCentrality(WaitTimeDuration, new String(trace+"/GRAPH/"+week+"GWaitTimeDuraClose.txt"));
      ClosenessCentrality(WaitTime, new String(trace+"/GRAPH/"+week+"GWaitTimeClose.txt"));
      ClosenessCentrality(Graph01, new String(trace+"/GRAPH/"+week+"G01Close.txt"));

      //HarmonicCentrality(SPM, new String(trace+"/GRAPH/"+week+"GSPMHarm.txt"));
      HarmonicCentrality(gMeetingCnt, new String(trace+"/GRAPH/"+week+"GMeetCntCenHarm.txt"));
      HarmonicCentrality(WaitTimeDuration, new String(trace+"/GRAPH/"+week+"GWaitTimeDuraHarm.txt"));
      HarmonicCentrality(WaitTime, new String(trace+"/GRAPH/"+week+"GWaitTimeHarm.txt"));
      HarmonicCentrality(Graph01, new String(trace+"/GRAPH/"+week+"G01Harm.txt"));

    }
    catch(Exception e){
      System.err.println(e.getMessage()+" "+e.toString());
     e.printStackTrace();
    } 
  }
  public void degreeCentrality(float Graph[], String trace, boolean inverse){
    double []Degree= new double[GSIZE];
    for(int i=0;i<GSIZE;i++){
      for(int j=0;j<GSIZE;j++)
      Degree[i] += Graph[i*GSIZE+j];
      if(inverse)
        Degree[i]=1/Degree[i];
      Degree[i]/=GSIZE;
    }
    writecentrality(Degree, trace);
    
   // calculatePageRank(Degree, new String(trace+"GPageRank.txt"));
  }
  public void HarmonicCentrality(float Graph[], String trace){
    float distance[] = new float[GSIZE*GSIZE];
    for(int i=0;i<GSIZE;i++){
      for(int j=0;j<GSIZE;j++){
        int index =  i*GSIZE+j;
        if(i==j)
          distance[index]=0;
        else if(Graph[index]==0)
          distance[index]=MY_MAX;
        else
          distance[index]=Graph[index];
       }
     }
    for(int k=0;k<GSIZE;k++){
      for(int i=0;i<GSIZE;i++){
        for(int j=0;j<GSIZE;j++){
          int indexij = i*GSIZE+j;
          int indexik = i*GSIZE+k;
          int indexkj = k*GSIZE+j;
          if (distance[indexij] > distance[indexik] + distance[indexkj]) 
            distance[indexij] = distance[indexik] + distance[indexkj];       
        }
      }
    }
    double harmonic[]= new double[GSIZE];
    
    for(int i=0;i<GSIZE;i++){
      harmonic[i]=0;
      for(int j=0;j<GSIZE;j++){
        if(distance[i*GSIZE+j]!=MY_MAX)
          harmonic[i]+=1/(distance[i*GSIZE+j]>0? distance[i*GSIZE+j]:MY_MAX);
      }
     
      harmonic[i]=harmonic[i]/(GSIZE-1);
    }
    writecentrality(harmonic, trace);

   }
 public void ClosenessCentrality(float Graph[], String trace){
   float distance[] = new float[GSIZE*GSIZE];
   for(int i=0;i<GSIZE;i++){
     for(int j=0;j<GSIZE;j++){
       int index =  i*GSIZE+j;
       if(i==j)
         distance[index]=0;
       else if(Graph[index]==0)
         distance[index]=MY_MAX;
       else
         distance[index]=Graph[index];
      }
    }
   for(int k=0;k<GSIZE;k++){
     for(int i=0;i<GSIZE;i++){
       for(int j=0;j<GSIZE;j++){
         int indexij = i*GSIZE+j;
         int indexik = i*GSIZE+k;
         int indexkj = k*GSIZE+j;
         if (distance[indexij] > distance[indexik] + distance[indexkj]) 
           distance[indexij] = distance[indexik] + distance[indexkj];       
       }
     }
   }
   double close[]= new double[GSIZE];
   
   for(int i=0;i<GSIZE;i++){
     close[i]=0;
     for(int j=0;j<GSIZE;j++){
       if(distance[i*GSIZE+j]!=MY_MAX)
         close[i]+=distance[i*GSIZE+j];
     }
     if(close[i]==0)
       close[i]=MY_MAX;
     close[i]=GSIZE/close[i];
   }
   writecentrality(close, trace);

  }
 public void PageRankCentrality(float Graph[], boolean weighted, String trace){
   float damp=1;
   int N=GSIZE/2;
   float []v = new float[GSIZE];//Math.random();
   float []w= new float[GSIZE];
  for (int i=0;i<GSIZE;i++){
    v[i] = 1;
    w[i] = 0;
  }
  for(int i=0;i<GSIZE;i++){
    for(int j=0;j<GSIZE;j++){
      if(i==j)
        continue;
    int index=i*GSIZE+j;
    if(Graph[index]<MY_MAX)
      w[j]+=v[i];
    }
  }
  float sum=0;
  for(int i=0;i<GSIZE;i++)
    sum+=w[i];
  for(int i=0;i<GSIZE;i++)
    w[i]/=sum;
  
   last_v = ones(N, 1) * inf;
   M_hat = (d .* M) + (((1 - d) / N) .* ones(N, N));

   while(norm(v - last_v, 2) > v_quadratic_error)
     last_v = v;
     v = M_hat * v;
     v = v ./ norm(v, 2);
   end

   endfunction

   function [v] = rank2(M, d, v_quadratic_error)

   N = size(M, 2); % N is equal to half the size of M
   v = rand(N, 1);
   v = v ./ norm(v, 1);   % This is now L1, not L2
   last_v = ones(N, 1) * inf;
   M_hat = (d .* M) + (((1 - d) / N) .* ones(N, N));

   while(norm(v - last_v, 2) > v_quadratic_error)
     last_v = v;
     v = M_hat * v;
           % removed the L2 norm of the iterated PR
   end
   
 }
 public int getTargetNode(float []nodeRow, int row, int nodeNo){
   for(int i=0;i<GSIZE;i++)
   {
     if(nodeRow[row*GSIZE+i]>0){
       nodeNo--;
       if(nodeNo==0)
         return i;
     }
   }
   return -1;
 }
 public int getDegree(float []nodeRow, int row){
   int degree=0;
   for(int i=0;i<GSIZE;i++)
   {
     degree+=nodeRow[row*GSIZE+i];
   }
   return degree;
 }
 public void calculatePageRank(float graph[],String trace)
 {
   double alpha = 0.85;
   // Create new/old PageRank vectors for iteration
   double []PR = new double[GSIZE];
   double [] PR_new = new double[GSIZE];

   // Initialize Uniform PageRank Vector
   for(int i = 0; i < PR.length; i++)
   {
     PR[i] = (float) (1.0 / (float) PR.length);
   }//end: for(i)

   // PageRank!!!
   double change;
   do {
     
     // Set the additional randomSurfer parameter
     double randomSurfer = 0;
     
     for(int i = 0; i < GSIZE; i++)
     {
       // Valid out-bound graph
      // if(graph[i] != null && graph[i].length != 0)
       {
           // Update new values for neighbor vertices
         int degree=getDegree(graph,i);
         for(int j = 0; j < degree; j++)
         {
           int target = getTargetNode(graph, i, j+1);
           PR_new[target] += (PR[i]/ (degree));
         }//end: for(j)
       }
      // else
       { // No outbound links, add to overall graph values
        // randomSurfer += PR[i] / graph.length;
       }

      
     }//end: for(i)
 

     for(int x = 0; x < PR_new.length; x++)
     {
       //System.out.println(x+" "+PR_new[x]);

       PR_new[x] = (alpha * (PR_new[x] + randomSurfer)) + ((1-alpha) / PR_new.length);
     //  System.out.println(x+" "+PR_new[x]);
     }//end: for(x)

     // Calculate change between PR generations
     change = pageRankDiff(PR, PR_new);

     // Reset new PR array.
     for(int x = 0; x < PR.length; x++)
     {
       PR[x] = PR_new[x];
       PR_new[x] = 0;
     }

     // Calculate the magnitude for normalization
     double mag = 0;
     for(int i = 0; i < PR.length; i++)
     {
       mag += PR[i];
     }//end: for(i)
     
     //System.out.println(mag);
    // mag=mag/2;
     // Normalize PR vector
     //for(int i = 0; i < PR.length; i++)
     {
     //  PR[i] = (float) (PR[i] / mag);
     }//end: for(i)

 

   }while(change > 0.001);     
   for(int i = 0; i < PR.length; i++)
   {
     PR[i] = (float) (PR[i] *1000);
   }//end: for(i)
   writecentrality(PR, trace);

 }//end: calculatePageRank(double)

 protected static double pageRankDiff(double[] oldPR, double[] newPR)
 {
   float diff = 0;
   
   for(int x = 0; x < oldPR.length; x++)
   {
     diff += Math.abs( oldPR[x] - newPR[x] );
   }//end: for(x)
   
   return diff;
 }//end:
  void betweenness1D(float Graph[], boolean weighted, String trace){

    double []betw= new double[GSIZE];
    int s = 0;
    for (int i = 0; i < GSIZE; i++){
      betw[i] = 0;
    }
    //printf("\n\tStarting 1 D Betweeness\n");
    while (s < GSIZE)
    {
      int stack[] = new int[GSIZE];
      Integer top = new Integer(-1);
      int P[]= new int[GSIZE*GSIZE];
      int pFront[] = new int[GSIZE];

      float sigma[] = new float[GSIZE];

    
      if (!weighted)
        top=BFSB(Graph, s, stack,top,P,pFront,sigma);
      else
        top=dijkstraB(Graph, s, stack, top, P, pFront, sigma);

 
      float delta[] = new float[GSIZE];
      for (int i = 0; i<GSIZE; i++)
        delta[i] = 0;
      //betw[s] += top;
      //printf("\n in a basic\n");
      while (top > -1){
        int w = stack[top--];
        float coef = (1 + delta[w]) / sigma[w];
       // printf("w=%d coef=%.2f\n", w+1, coef);
        for (int i = 0; i <= pFront[w]; i++)
        {
          int v = P[w*GSIZE + i];
          delta[v] += sigma[v] * coef;
          //printf("v=%d deltav=%.2f\n", v+1, delta[v]);
        }
       // printf("w=%d s=%d\n", w+1, s+1);
        if (w != s)
          betw[w] += delta[w];
      }
      s++;
    }
    writecentrality(betw,trace);

  }
  public void writecentrality(double []betw, String trace){
    try{
      DataOutputStream out1 = new DataOutputStream(new FileOutputStream("result/" + trace)); // Continue to read lines while
      for(int i=0;i<GSIZE;i++)
        out1.writeBytes(Double.toString(Round(betw[i],2))+"\n");
      out1.close();
    }
  catch(Exception e){
    System.err.print(e.getMessage()+" "+e.toString());
    e.printStackTrace();
  }
  System.out.println(trace+" Centrality written");

  }
  
  public int dijkstraB(float graph[], int s, int stack[], Integer top, int P[], int pFront[], float sigma[])
  {
    float distance[]= new float[GSIZE];     // The output array.  dist[i] will hold the shortest
    // distance from src to i
    int pred[] = new int[GSIZE];

    for (int i = 0; i < GSIZE; i++){
      distance[i] = -1;
      pFront[i] = -1;
      //pRear[i] = 0;
      sigma[i] = 0;
      pred[i] = -1;
    }
    //int q[GSIZE]; int front = 0, rear = -1;

    distance[s] = 0;
    sigma[s] = 1;
    pred[s] = s;

    //q[++rear] = s;

    //printf("\n\tStarting Dijkstra's \n");
    
    boolean sptSet[]= new boolean[GSIZE]; // sptSet[i] will true if vertex i is included in shortest
    // path tree or shortest distance from src to i is finalized

    // Initialize all distances as INFINITE and stpSet[] as false
    for (int i = 0; i < GSIZE; i++){
      distance[i] = MY_MAX; sptSet[i] = false;
    }
    // Distance of source vertex from itself is always 0
    distance[s] = 0;

    // Find shortest path for all vertices
    for (int count = 0; count < GSIZE ; count++)
    {
      // Pick the minimum distance vertex from the set of vertices not
      // yet processed. u is always equal to src in first iteration.
      int u = minDistance(distance, sptSet);

      // Mark the picked vertex as processed
      sptSet[u] = true;
      if(pred[u]<0)
        continue;

      sigma[u] += sigma[pred[u]];
      stack[++top] = u;
      // Update dist value of the adjacent vertices of the picked vertex.
      for (int v = 0; v < GSIZE; v++){

        if (graph[u*GSIZE+v] != MY_MAX && graph[u*GSIZE+v] != 0)
        {
          float vdist = distance[u] + graph[u*GSIZE+v];
          

          if (distance[v] == -1 || vdist<distance[v]){
            distance[v] = vdist;
            pred[v] = u;
            P[v*GSIZE + ++pFront[v]] = u; 
          }
          else if (vdist == distance[v]){
            sigma[v] += sigma[u];
            P[v*GSIZE + ++pFront[v]] = u;
          }

        }
      }
      //if ((!sptSet[v]) && distance[u] != MY_MAX && graph[u][v] != MY_MAX && distance[u] + graph[u][v] < distance[v])
        //distance[v] = distance[u] + graph[u][v];
    }
    return top;

    // print the constructed distance array
    //printSolution(dist, src);
  }


  public Integer BFSB(float Graph[], int s, int stack[], Integer top,int P[], int pFront[], float sigma[]){
    int distance[]= new int[GSIZE];
    for (int i = 0; i < GSIZE; i++){
      distance[i] = -1;
      pFront[i] = -1;
      //pRear[i] = 0;
      sigma[i] = 0;
    }
    int q[]= new int[GSIZE]; int front = 0, rear = -1;

    distance[s] = 0;
    sigma[s] = 1;

    q[++rear] = s;

    while (front <= rear)
    {
      int v = q[front++];
      top = top +1;
      stack[top] = v;
      for (int w = 0; w < GSIZE; w++)
      {
        if (Graph[v*GSIZE + w] != 0 && Graph[v*GSIZE + w] != MY_MAX)// && visited[j] != 1)
        {
          if (distance[w] < 0)
          {
            q[++rear] = w;
            distance[w] = distance[v] + 1;
          }
          if (distance[w] == (distance[v] + 1))
          {
            sigma[w] += sigma[v];
            P[w*GSIZE + ++pFront[w]] = v;
          }
        }
      }
    }
    return top;
  }
  public int minDistance(float dist[], boolean sptSet[])
  {
    // Initialize min value
    float min = Float.MAX_VALUE;
    int min_index=-1;

    for (int v = 0; v < GSIZE; v++)
    if (sptSet[v] == false && dist[v] <= min){
      min = dist[v]; min_index = v;
    }

    return min_index;
  }
  public void Betweenusefulness(String trace, String bw,int w){
      String week =new String("");
      if(w!=0)
        week="W"+Integer.toString(w)+"/";
      readDevStat(trace, bw);
      readCentrality(new String(trace+"/GRAPH/"+week+"G01betweeness.txt"),0);
      readCentrality(new String(trace+"/GRAPH/"+week+"GMeetingCntbetweeness.txt"),1);
      readCentrality(new String(trace+"/GRAPH/"+week+"GWaitTimeDurabetw.txt"),2);
      readCentrality(new String(trace+"/GRAPH/"+week+"GWaitTimeMeetCntbetw.txt"),3);
      readCentrality(new String(trace+"/GRAPH/"+week+"GDurationbetw.txt"),4);
      readCentrality(new String(trace+"/GRAPH/"+week+"GWaitDuraMeetCntbetw.txt"),5);
      readCentrality(new String(trace+"/GRAPH/"+week+"GWaitTimebetw.txt"),6);
      readCentrality(new String(trace+"/GRAPH/"+week+"G01Degree.txt"),7);
      readCentrality(new String(trace+"/GRAPH/"+week+"GMeetCntCenClose.txt"),8);
      readCentrality(new String(trace+"/GRAPH/"+week+"GWaitTimeDuraClose.txt"),9);
      readCentrality(new String(trace+"/GRAPH/"+week+"GWaitTimeClose.txt"),10);
      readCentrality(new String(trace+"/GRAPH/"+week+"G01Close.txt"),11);

      writeCentralitySummary(trace,week);

      /*LinkedList<Device>g01L = new LinkedList<Device>();
      LinkedList<Device>gMeetingCnt = new LinkedList<Device>();
      LinkedList<Device>gWaitTimeDuraL = new LinkedList<Device>();
      LinkedList<Device>gWaitTimeMeetCntL = new LinkedList<Device>();
      LinkedList<Device>gDurationL = new LinkedList<Device>();
      LinkedList<Device>gWaitDuraMeetinCntL = new LinkedList<Device>();
      LinkedList<Device>gWaitTimeL = new LinkedList<Device>();
      for(int i=0;i<tabdevList.size();i++)
      {
        g01L.add(Peerstable.get(tabdevList.get(i)));
        gMeetingCnt.add(Peerstable.get(tabdevList.get(i)));
        gWaitTimeDuraL.add(Peerstable.get(tabdevList.get(i)));
        gWaitTimeMeetCntL.add(Peerstable.get(tabdevList.get(i)));
        gDurationL.add(Peerstable.get(tabdevList.get(i)));
        gWaitDuraMeetinCntL.add(Peerstable.get(tabdevList.get(i)));
        gWaitTimeL.add(Peerstable.get(tabdevList.get(i)));
      }
      /*Collections.sort(g01L, new Centrality0Comparator());
      Collections.sort(gMeetingCnt, new Centrality1Comparator());
      Collections.sort(gWaitTimeDuraL, new Centrality2Comparator());
      Collections.sort(gWaitTimeMeetCntL, new Centrality3Comparator());
      Collections.sort(gDurationL, new Centrality4Comparator());
      Collections.sort(gWaitDuraMeetinCntL, new Centrality5Comparator());
      Collections.sort(gWaitTimeL, new Centrality6Comparator());
*/
  }
  public void readDevStat(String trace,String bw){
    try{
      int seq;
      for(seq=2;seq<=33;seq++){
        BufferedReader in1 = new BufferedReader(new FileReader("result/"+trace +"/DSTAT/DStat"+Integer.toString(seq)+bw+"Paths_0Flooding.txt")); // Continue to read lines while      
        for(int i=0;i<GSIZE;i++)
        {
          String[]frags= in1.readLine().split("\t");
          Peerstable.get(tabdevList.get(i)).rbytes+=Double.parseDouble(frags[1]);
          Peerstable.get(tabdevList.get(i)).dbytes+=Double.parseDouble(frags[2]);
          Peerstable.get(tabdevList.get(i)).hbytes+=Double.parseDouble(frags[3]);
          Peerstable.get(tabdevList.get(i)).tbytes+=Double.parseDouble(frags[4]);
          Peerstable.get(tabdevList.get(i)).dmsgs+=Double.parseDouble(frags[5]);

        }
        in1.close();
      }
    }
    catch(Exception e){
      System.err.print(e.getMessage()+" "+e.toString());
      e.printStackTrace();
    }
  }public void readMsgStat(String trace, String bw){
    try{
      for(int i=2;i<33;i++){
        
        BufferedReader in1 = new BufferedReader(new FileReader("result/" + trace + "/MULTI/"
          +Integer.toString(i)+bw+"Flooding.txt")); // Continue to read lines while
        String line="";
        String []frags = new String[0];
        in1.readLine();
        for(int j=0;j<30;j++)
        {          
          line=in1.readLine();
          frags=line.split("\t");
          if(frags[2].equals(frags[frags.length-1]))
          {
            for(int k=6;k<frags.length-1;k++){
              Peerstable.get(tabdevList.get(Integer.parseInt(frags[k]))).dmsgs++;
              Peerstable.get(tabdevList.get(Integer.parseInt(frags[k]))).dbytes+=Double.parseDouble(frags[3]);
            }
          }
        }
        in1.close();
      }

        }  catch(Exception e){
          System.err.print(e.getMessage()+" "+e.toString());
          e.printStackTrace();
        }
  }
  public void readCentrality(String file, int param){
    try{
      BufferedReader in1 = new BufferedReader(new FileReader("result/" + file)); // Continue to read lines while      
      for(int i=0;i<GSIZE;i++)
        Peerstable.get(tabdevList.get(i)).centrality[param]=Double.parseDouble(in1.readLine());
      in1.close();
    }
    catch(Exception e){
      System.err.print(e.getMessage()+" "+e.toString());
      e.printStackTrace();
    }
  }
   public void writeCentralitySummary(String trace, String week){
     try{
       DataOutputStream out1 = new DataOutputStream(new FileOutputStream("result/" + trace + "/GRAPH/"+
            week + "BetweenesssummaryallPath.txt")); // Continue to read lines while
       for(int i=0;i<GSIZE;i++){
         out1.writeBytes(Integer.toString(i)+"\t");
         Device dtemp = Peerstable.get(tabdevList.get(i));
         for(int k=0;k<12;k++)
         {
           out1.writeBytes(dtemp.centrality[k]+"\t");
         }
         out1.writeBytes(+dtemp.rbytes+"\t"+dtemp.dbytes+"\t"+dtemp.hbytes+"\t"+dtemp.tbytes+"\t"+dtemp.dmsgs+"\n");
       }
       out1.close();
     }catch(Exception e){
       System.err.print(e.getMessage()+" "+e.toString());
       e.printStackTrace();
     }
     System.out.println(trace+" week"+week+ "betweenness summary written");
   }
   
   public int getSPMFt(double maxWt){
     int i = (int)maxWt+1;
     int sum=i;
     for(;i>=0;i--)
       sum+=i;
     return sum;
   }

  public void writegraph(String trace, int ap_count, Date T,int w){
    int devlist = tabdevList.size()-ap_count;
   
    for(int i=0;i<devlist;i++){
      Peerstable.get(tabdevList.get(i)).meetStat = new Hashtable<Integer, MeetingStats>();
    }
    h = new History(Peerstable, tabdevList, null, null);
    h.construct_graphdjpathsT(devlist,T);
  
   String week =new String("");
    if(w!=0)
      week="W"+Integer.toString(w)+"/";
    try{
    /*  DataOutputStream out1 = new DataOutputStream(new FileOutputStream("result/" + trace + "/GRAPH/"
            +week+ "Graph01.txt")); // Continue to read lines while
      DataOutputStream out2 = new DataOutputStream(new FileOutputStream("result/" + trace + "/GRAPH/"
          +week+ "GraphMeetcnt.txt")); // Continue to read lines while
      DataOutputStream out3 = new DataOutputStream(new FileOutputStream("result/" + trace + "/GRAPH/"
          +week+ "GWaitTimeDuration.txt")); // Continue to read lines while
      DataOutputStream out4 = new DataOutputStream(new FileOutputStream("result/" + trace + "/GRAPH/"
          +week+ "MeetingCount.txt")); // Continue to read lines while
      DataOutputStream out5 = new DataOutputStream(new FileOutputStream("result/" + trace + "/GRAPH/"
          +week+ "Onlinetime.txt")); // Continue to read lines while
      DataOutputStream out6 = new DataOutputStream(new FileOutputStream("result/" + trace + "/GRAPH/"
          +week+ "GWaitTimeMeetCnt.txt")); // Continue to read lines while
      DataOutputStream out7 = new DataOutputStream(new FileOutputStream("result/" + trace + "/GRAPH/"
          +week+ "GDuration.txt")); // Continue to read lines while
      DataOutputStream out8 = new DataOutputStream(new FileOutputStream("result/" + trace + "/GRAPH/"
          +week+ "GWaitDurationMeetingCnt.txt")); // Continue to read lines while
      DataOutputStream out9 = new DataOutputStream(new FileOutputStream("result/" + trace + "/GRAPH/"
          +week+ "GWaitTime.txt")); // Continue to read lines while*/
      
      DataOutputStream out10 = new DataOutputStream(new FileOutputStream("result/" + trace + "/GRAPH/"
          +week+ "GSPM.txt")); // Continue to read lines while
    
      /*out1.writeBytes(Integer.toString(devlist)+"\n");
     out2.writeBytes(Integer.toString(devlist)+"\n");
     out3.writeBytes(Integer.toString(devlist)+"\n");
     out6.writeBytes(Integer.toString(devlist)+"\n");
     out7.writeBytes(Integer.toString(devlist)+"\n");
     out8.writeBytes(Integer.toString(devlist)+"\n");
     out9.writeBytes(Integer.toString(devlist)+"\n");*/
      out10.writeBytes(Integer.toString(devlist)+"\n");

      Device dtemp;
      MeetingStats ms;
    
      for(int i=0;i<devlist;i++){
        dtemp=Peerstable.get(tabdevList.get(i));
        for(int j=0;j<devlist;j++){
          if(dtemp.meetStat.containsKey(j)){
            ms = dtemp.meetStat.get(j);
            
           /*   out1.writeBytes("1\t");
              out2.writeBytes(Double.toString(1/(double)ms.meetingcnt)+"\t");
              //if(ms.meetingcnt==1)
                //out3.writeBytes(Double.toString(Round(MY_MAX/(double)60,2))+"\t");
              //else
                out3.writeBytes(Double.toString(Round(ms.waitTime/(double)ms.duration,2))+"\t");
              out6.writeBytes(Double.toString(Round(ms.waitTime/(double)ms.meetingcnt,2))+"\t");
              out7.writeBytes(Double.toString(Round(1/(double)ms.duration,2))+"\t");
              out8.writeBytes(Double.toString(Round(ms.waitTime/(double)(ms.duration*ms.meetingcnt),2))+"\t");
              out9.writeBytes(Double.toString(Round(ms.waitTime,2))+"\t");    */
            if(ms.meetingcnt>1)
              out10.writeBytes(Double.toString(Round(dtemp.sorted_slots.size()/(double)getSPMFt(ms.maxWaitTime),2))+"\t");
            else
              out10.writeBytes(Double.toString(1)+"\t");

          }
          else{
            if(i==j)
            {
              /*out1.writeBytes("0\t");
              out2.writeBytes("0\t");
              out3.writeBytes("0\t");
              out6.writeBytes("0\t");
              out7.writeBytes("0\t");
              out8.writeBytes("0\t");
              out9.writeBytes("0\t");*/
              out10.writeBytes(Integer.toString(dtemp.sorted_slots.size())+"\t");

            }
            else{
             /* out1.writeBytes("0\t");
              out2.writeBytes(Double.toString(Round(MY_MAX,2))+"\t");
              out3.writeBytes(Double.toString(Round(MY_MAX,2))+"\t");
              out6.writeBytes(Double.toString(Round(MY_MAX,2))+"\t");
              out7.writeBytes(Double.toString(Round(MY_MAX,2))+"\t");
              out8.writeBytes(Double.toString(Round(MY_MAX,2))+"\t");
              out9.writeBytes(Double.toString(Round(MY_MAX,2))+"\t");*/
              out10.writeBytes("0\t");

            }
          }
        }
        /*Enumeration <Integer>denum = dtemp.meetStat.keys();
        double[] total = normalisingfact(dtemp);
        while(denum.hasMoreElements()){
          dkey=denum.nextElement();
          ms = dtemp.meetStat.get(dkey);
          if(ms.meetingcnt>1 & ms.waitTime>0){
            out1.writeBytes(dkey+","+ms.meetingcnt+","+ms.waitTime+","+ms.minwaitTime+"\t");
            out3.writeBytes(Double.toString((ms.waitTime/total[0]))+"\t"+ms.meetingcnt/total[1]+"\t");
          }
            
        }*/
        /*out2.writeBytes("\n");
        out1.writeBytes("\n");
        out3.writeBytes("\n");    
        out4.writeBytes(Integer.toString(dtemp.totalmeetcount)+"\n");
        out5.writeBytes(Double.toString(dtemp.tonlinetime)+"\n");
        out6.writeBytes("\n");  
        out7.writeBytes("\n");  
        out8.writeBytes("\n");  
        out9.writeBytes("\n"); */
        out10.writeBytes("\n");
      }
      //out2.close();
    /*  out1.close();
      out2.close();
      out3.close();
      out4.close();
      out5.close();
      out6.close();
      out7.close();
      out8.close();
      out9.close();*/
      out10.close();
      
    }catch(Exception e){
      System.err.print(e.getMessage()+" "+e.toString());
      e.printStackTrace();
    }
    System.out.println(trace+" graph written");
  }
  public static double[] normalisingfact(Device dtemp2) {

    Enumeration <Integer>denum2 = dtemp2.meetStat.keys();
    double []total = new double[2] ;
    int key;
    while (denum2.hasMoreElements()){
      key =denum2.nextElement();
      total[1] += dtemp2.meetStat.get(key).meetingcnt;
      total[0] += dtemp2.meetStat.get(key).waitTime;
    }
    return total;
  }
  public static double Round(double Rval, int Rpl) {
    double p = (double)Math.pow(10,Rpl);
    Rval = Rval * p;
    double tmp = Math.round(Rval);
    return (double)tmp/p;
      }
  public void readData(String trace,int ap_count){
    int devlistsize= tabdevList.size()-ap_count;
    for(int i=0;i<devlistsize;i++){
      Peerstable.get(tabdevList.get(i)).meetStat = new Hashtable<Integer, MeetingStats>();
    }
    Device dtemp,dtemp2;
    String []line= new String[0];
    int j=0;
    try{
      BufferedReader in1 = new BufferedReader(new FileReader("result/" + trace + "/"
          + "Graph.txt")); // Continue to read lines while
     
      MeetingStats ms;
      for(int i=0;i<devlistsize;i++ ){
        line=in1.readLine().split("\t");
        dtemp = Peerstable.get(tabdevList.get(i));
        for( j=0;j<line.length;j++){
          String []comp=line[j].split(",");
          if(comp[0].length()<1)
            continue;
          int k = Integer.parseInt(comp[0]);
           if(!dtemp.meetStat.containsKey(k)){
            ms = new MeetingStats(0, null, -1,-1,dtemp.sorted_slots.getLast().timemax,0);
            dtemp.meetStat.put(k,ms );
            ms.meetingcnt=Integer.parseInt(comp[1]);
            ms.waitTime=Double.parseDouble(comp[2]);
            ms.minwaitTime=Double.parseDouble(comp[3]);          
          }
          dtemp2=Peerstable.get(tabdevList.get(k));
          if(!dtemp2.meetStat.containsKey(i)){
            ms = new MeetingStats(0, null, -1,-1,dtemp2.sorted_slots.getLast().timemax);
            dtemp.meetStat.put(i,ms );
            ms.meetingcnt=Integer.parseInt(comp[1]);
            ms.waitTime=Double.parseDouble(comp[2]);
            ms.minwaitTime=Double.parseDouble(comp[3]);          
          }
        }      
      }
    }
    catch(Exception e){
      System.err.println(e.getMessage()+" "+e.toString()+" "+line.length);
     e.printStackTrace();
    }
  }
  public void calcDpath(String trace,int ap_count){
    readData(trace, ap_count);

    HashMap<Integer, Integer>  TraversedNodes;// = new HashMap<Integer, Integer>();
    ArrayList <LinkedList<Integer>>AvailablePath;
    Dijkstra dij = new Dijkstra(Peerstable,tabdevList);
    ArrayList <Double>pDelays; //= new ArrayList<Double>();
    Msg mtemp;
    Enumeration<BigDecimal> mkeys =gMsgTable.keys();

    try{
      DataOutputStream out1 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + "DisjointPaths.txt")); // Continue to read lines while
      int mcount=0;
      while(mkeys.hasMoreElements()){
        if(++mcount%5==0)
          System.out.println(mcount);
        mtemp=gMsgTable.get(mkeys.nextElement());
        TraversedNodes = new HashMap<Integer, Integer>();
        AvailablePath =new ArrayList<LinkedList<Integer>>();
        pDelays = new ArrayList<Double>();
        do{
         // pDelays.add(dij.DisJointDikstra(AvailablePath, ap_count, mtemp));
          pDelays.add(dij.BFS(AvailablePath,mtemp,ap_count));
          
        }while(pDelays.get(pDelays.size()-1)!=Double.MAX_VALUE);//till no new path is found
       if(pDelays.size()<=1)
         continue;
         out1.writeBytes(pDelays.size()-1+"\t");
        for(int i=0;i<pDelays.size()-1;i++){
          int count=0;
          for(int j=0;j<AvailablePath.get(i).size();j++)
          {
            boolean disjoint=true;
            for(int k=i+1;k<pDelays.size()-1 & disjoint;k++){
              if(AvailablePath.get(k).contains(AvailablePath.get(i).get(j)))
                 disjoint=false;
            }
            if(disjoint)
              count++;
          }
          out1.writeBytes(pDelays.get(i)+","+count+"\t");
        }
        out1.writeBytes("\n");
      }
      out1.close();
    
    }catch(Exception e){
      System.err.print(e.getMessage()+" "+e.toString());
    }
    System.out.println(trace + " Disjoint paths written");
  }
  public void calcDpathMFlow(String trace){
    
    Dijkstra dij = new Dijkstra(Peerstable,tabdevList);
    Msg mtemp;
    Enumeration<BigDecimal> mkeys =gMsgTable.keys();
   
    ArrayList <ArrayList<boolean[]>>AdjMat = new ArrayList<ArrayList<boolean[]>>();
    
    boolean nodedisjoint=true;
   try{
      DataOutputStream out1 = new DataOutputStream(new FileOutputStream("result/" + trace + "/"
            + "DisjointPaths.txt")); // Continue to read lines while
      int mcount=0;
      readAdjMatrix(AdjMat, trace);
      while(mkeys.hasMoreElements()){
        if(++mcount%5==0)
          System.out.println(mcount);
          mtemp=gMsgTable.get(mkeys.nextElement());
          ArrayList <ArrayList<boolean[]>>tempAdjMat= new ArrayList<ArrayList<boolean[]>>();
        
          for(int i=0;i<AdjMat.size();i++){
            ArrayList<boolean[]>row = new ArrayList<boolean[]>();  
            for(int j=0;j<AdjMat.get(i).size();j++){
              boolean []flow = new boolean[2];
              flow[0]=AdjMat.get(i).get(j)[0];
              flow[1]=AdjMat.get(i).get(j)[1];
              row.add(flow);
            }           
            tempAdjMat.add(row);            
          }
         
         
          if(nodedisjoint)
            NodeDisjoint(tempAdjMat);
          /*for(int i=0;i<tempAdjMat.size();i++){
            for(int j=0;j<tempAdjMat.size();j++)
              System.out.print(tempAdjMat.get(i).get(j)[0]+"\t");
          System.out.println();
          }*/
          int dpathcount = 0;
         //mtemp.src=0;
        // mtemp.dest=3;
          while(dij.BFS(tempAdjMat, new Integer(mtemp.src), new Integer(mtemp.dest),AdjMat.size(),nodedisjoint));
          //while(dij.BFS(tempAdjMat, 0,5,AdjMat.size(),nodedisjoint));
           
          for(int i=0;i<tempAdjMat.get(mtemp.src).size();i++){
            if(tempAdjMat.get(mtemp.src).get(i)[1])
              dpathcount++;
          }
          out1.writeBytes(mtemp.ID.toString()+"\t"+Integer.toString(dpathcount)+"\t"+Peerstable.get(tabdevList.get(mtemp.src)).meetStat.size()+"\n");
          //System.out.println(dpathcount);
          //break;
        }
        
      out1.close();
    
    }catch(Exception e){
      System.err.print(e.getMessage()+" "+e.toString());
      e.printStackTrace();
    }
    System.out.println(trace + " Disjoint paths written");
  }
  public void NodeDisjoint(ArrayList <ArrayList<boolean[]>>tempAdjMat){
    int initsize = tempAdjMat.size();
    for(int i=0;i<initsize;i++){
      for(int j=0;j<initsize;j++)
        tempAdjMat.get(i).add(new boolean[2]);
    }
    for(int i=0;i<initsize;i++){
      ArrayList<boolean[]>row = new ArrayList<boolean[]>(); 
      for(int j=0;j<initsize*2;j++){
        row.add(new boolean[2]);            
      }
      tempAdjMat.add(row);
      //tempAdjMat.get(i).get(tempAdjMat.size()-1)[0] = true;
      tempAdjMat.get(tempAdjMat.size()-1).get(i)[0] = true;
      for(int j=0;j<initsize;j++){
        if(j==i)
          continue;
        tempAdjMat.get(j).get(tempAdjMat.size()-1)[0] = tempAdjMat.get(j).get(i)[0];
        tempAdjMat.get(j).get(i)[0] = false;
        
      }
    }
  }
  public void readAdjMatrix(ArrayList <ArrayList<boolean[]>>l, String trace){

    String []frags= new String[0];
    try{
      BufferedReader in1 = new BufferedReader(new FileReader("result/" + trace + "/"
          + "AdjMatrix.txt")); // Continue to read lines while
     ArrayList <boolean[]>dl;
     while(in1.ready()){
        dl=new ArrayList<boolean[]>();
        String line=in1.readLine();
        frags=line.split(" ");
        if(frags.length<2)
          frags=line.split("\t");
          
        for(int j=0;j<frags.length;j++){
          boolean flow[] = new boolean[2];
          if(Double.parseDouble(frags[j])>0)
              flow[0]=true;
          
          dl.add(flow);
        }
        l.add(dl);        
      }
     if(frags.length!=l.size())
       System.err.println("Input malformed");
    }catch(Exception e){
      System.err.println("Input error\n");
      e.printStackTrace();
    }
    
  }
}
