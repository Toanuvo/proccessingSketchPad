import processing.core.PApplet;

import java.io.*;
import java.util.*;
import java.math.*;
public class MAIN extends PApplet implements Serializable {

  private static final long serialVersionUID = 2070320022527217445L;

  public  static void main(String[] args){
        PApplet.main("MAIN");
    }
  public void settings(){
      fullScreen();
    }
  public void setup() {  // setup() runs once
    frameRate(30);

    // add all saved graphs
    for(int i =0; i<=100; i++){
      String filename = "graph"+i+".ser";
      File file = new File(filename);
      if(file.exists()){
        graphs.add(i);
      }
    }
  }


  // create graph properties
  Vertex vertex1 = null;
  Vertex vertex2 = null;

  int vertex_amount = 0;
  int edge_amount = 0;
  ArrayList<Vertex> vertexes = new ArrayList<>();
  ArrayList<Edge> edges = new ArrayList<>();
  int[][] transitiontable;
  int graphnum = 0;
  char lastkey;

  float circleD = 50;
  float loadBX = 34;
  float loadBY = 40;
  float loadBD = 20;
  float saveBX = loadBX +25;

  boolean digraph = false;
  boolean bipartitegraph = false;
  boolean showvnums = true;
  boolean showenums = false;
  boolean showtable = false;
  //              black, red, yellow, green, blue, pink
  int[] colors = {0, 0xfffe0000,0xfffdfe02, 0xff0bff01, 0xff011efe, 0xfffe00f6};
  ArrayList<ArrayList<Vertex>> components = new ArrayList<>();
  matrixtype curtype = matrixtype.indice;
  ArrayList<Integer> graphs = new ArrayList<>();
  enum matrixtype{
    adjacecny,diagnal,indice,graph,laplace
  }
  int slowdraw = 0;

  class Vertex implements Serializable {
    private static final long serialVersionUID = 4973616882134323328L;
    float ypos;
    float xpos;
    int vcolor;
    int num;
    int distance;
    Edge path;
    ArrayList<MAIN.Edge> connected_edges;

    Vertex(float vx, float vy){
      num = vertex_amount;
      xpos = vx;
      ypos = vy;
      connected_edges = new ArrayList();
      vcolor = 0;
      distance = -1;
    }

    // equals function so i can call .equals on vertexes
    boolean equals(Vertex v1){
      if(v1 == null){
        return false;
      }
      return v1.xpos == xpos && v1.ypos == ypos;
    }

    // does not take directed edges intro account;
    MAIN.Edge getConnectedEdge(Vertex v){
      for(MAIN.Edge e : connected_edges){
        if(e.connected1.equals(this) && e.connected2.equals(v) ||
            e.connected2.equals(this) && e.connected1.equals(v)){
          return e;
        }
      }
      return null;
    }

    void show(){
      // highlight the last clicked vertices
      if(this.equals(vertex1)){
        stroke(255);
        strokeWeight(3);
      }
      if(this.equals(vertex2)){
        stroke(255);
        strokeWeight(3);
      }

      // create the vertex
      fill(colors[vcolor]);
      ellipse(xpos,ypos,circleD,circleD);
      stroke(0);
      strokeWeight(1);
      fill(10);
      if(showvnums) { // show its num if setting is on
        text(num, (xpos + 20), ypos - 20);
        text(connected_edges.size(), (xpos -30), ypos-20);
      }
    }
  }

  class Edge implements Serializable{
    private static final long serialVersionUID = -3647714615786853892L;
    int size;
    Vertex connected1;
    Vertex connected2;
    int edgesto2;
    int edgesto1;
    boolean isloop;
    boolean isBridge;
    boolean dijkstrapath;

    Edge(Vertex v1, Vertex v2){
      connected1 = v1;
      connected2 = v2;
      size = 1;
      isloop = v1.equals(v2);
    }

    void delete(){
      connected2.connected_edges.remove(this);
      connected1.connected_edges.remove(this);
    }

    Vertex getOtherVertex(Vertex v){
      if(isloop){
        return null;
      }
      if(v.equals(connected1)){
        return connected2;
      } else return connected1;
    }

    void show(){
      boolean displaynum = showenums;
      edge_amount += size;

      // it works
      float tempS = size;
      if(tempS == 3){
        tempS -= 1;
      } else if (tempS > 3 && size%2 != 0){
        tempS = (tempS+1)/2;
      } else if(tempS != 2 && size%2 == 0 ) {
        tempS = (tempS+1)/2;
      }

      float increment = (circleD/2)/(tempS);

      // loop display
      if(connected1.equals(connected2) && size > 0 ){
        noFill();
        increment = (circleD/2)/(size);
        float i = 0;
        while (i<circleD/2) {
          circle(connected1.xpos - circleD, connected1.ypos, (circleD * 2)-(i*3));
          i+= increment;
        }
        return;
      }

      BigDecimal X = new BigDecimal(connected1.ypos-connected2.ypos);
      BigDecimal Y = new BigDecimal(connected1.xpos-connected2.xpos);

      float angle = atan2(X.floatValue(),Y.floatValue());

      // create temp versions of directed edge so we can count them
      int tempto2 = edgesto2;
      int tempto1 = edgesto1;
      float i = 0;
      if(size%2 == 0){
        i += increment;
      }

      // loop for creating edge lines
      // could make into a for loop somehow probably
      while(i<(circleD/2)){

        //setup coloring
        if(isBridge){
          stroke(colors[4]);
        } else if(dijkstrapath){
          stroke(colors[5]);
        } else if(digraph) { // directed edge green to red
          if (tempto2 > 0) {
            stroke(colors[3]);
          } else if (tempto1 > 0) {
            stroke(colors[1]);
          }
        }

        //bottom lines, line is split into 2 so can make a digraph if needed
        float[] rotatedpoints1 = rotatePoint(connected1.xpos, connected1.ypos, connected1.xpos, connected1.ypos-i, angle);
        float[] rotatedpoints2= rotatePoint(connected2.xpos, connected2.ypos, connected2.xpos, connected2.ypos-i, angle);
        float midx = (rotatedpoints1[0]+rotatedpoints2[0])/2;
        float midy = (rotatedpoints1[1]+rotatedpoints2[1])/2;
        line(rotatedpoints1[0],rotatedpoints1[1],midx,midy);
        if(digraph) {
          if (tempto2 > 0) {
            stroke(colors[1]);
            tempto2--;
          } else if (tempto1 > 0) {
            stroke(colors[3]);
            tempto1--;
          }
        }
        midx = (rotatedpoints1[0]+rotatedpoints2[0])/2;
        midy = (rotatedpoints1[1]+rotatedpoints2[1])/2;
        line(midx,midy,rotatedpoints2[0],rotatedpoints2[1]);

        // display the edge number
        if(displaynum) {
          fill(255);
          text(edges.indexOf(this), midx + 5, midy - 5);
          fill(0);
          displaynum = false;
        }
        stroke(0);

        // only create 1 middle line
        if(i == 0){
          i+= increment;
          continue;
        }

        // top lines
        // setup coloring
        if(isBridge){
          stroke(colors[4]);
        } else if(dijkstrapath){
          stroke(colors[5]);
        } else if(digraph) {
          if (tempto2 > 0) {
            stroke(colors[3]);
          } else if (tempto1 > 0) {
            stroke(colors[1]);
          }
        }

        rotatedpoints1 = rotatePoint(connected1.xpos, connected1.ypos, connected1.xpos, connected1.ypos+i, angle);
        rotatedpoints2 = rotatePoint(connected2.xpos, connected2.ypos, connected2.xpos, connected2.ypos+i, angle);

        midx = (rotatedpoints1[0]+rotatedpoints2[0])/2;
        midy = (rotatedpoints1[1]+rotatedpoints2[1])/2;
        line(rotatedpoints1[0],rotatedpoints1[1],midx,midy);
        if(digraph) {
          if (tempto2 > 0 ) {
            stroke(colors[1]);
            tempto2--;
          } else if (tempto1 > 0) {
            stroke(colors[3]);
            tempto1--;
          }
        }
        midx = (rotatedpoints1[0]+rotatedpoints2[0])/2;
        midy = (rotatedpoints1[1]+rotatedpoints2[1])/2;
        line(midx,midy,rotatedpoints2[0],rotatedpoints2[1]);
        stroke(0);

        /* colors were easier than arrows
        // +- x changes arrow direction
        rotatedpoints1 = rotatePoint(midx, midy, midx-10, midy-10, angle);

        line(midx,midy,rotatedpoints1[0],rotatedpoints1[1]);
        rotatedpoints2 = rotatePoint(midx, midy, midx-10, midy+10, angle);
        line(rotatedpoints2[0],rotatedpoints2[1],midx,midy);

        midx = (connected1.xpos+connected2.xpos)/2;
        midy = (connected1.ypos+connected2.ypos-2*i)/2;

        // +- x changes arrow direction
        rotatedpoints1 = rotatePoint(midx, midy, midx-10, midy-10, angle);

        line(midx,midy,rotatedpoints1[0],rotatedpoints1[1]);
        rotatedpoints2 = rotatePoint(midx, midy, midx-10, midy+10, angle);
        line(rotatedpoints2[0],rotatedpoints2[1],midx,midy);
        */
        i += increment;

      }
    }


  }

  public void draw() {
    // set up
    background(200);
    fill(50);
    showstats();

    for(Vertex v : vertexes){
      v.show();
    }

    edge_amount = 0;
    for(Edge e : edges){
      e.show();
    }

    // only compute components and table every so many frames for optimization
    if(slowdraw == 10){
      switch (curtype) {
        case graph:
          transitiontable = createGraphMatrix();
          break;
        case indice:
          transitiontable = createTransitionTable();
          break;
        case diagnal:
          transitiontable = createDiagnalMatrix(createGraphMatrix());
          break;
        case adjacecny:
          transitiontable = createAdjacencyMatrix(createGraphMatrix());
          break;
        case laplace:
          transitiontable = createLaplaceMatrix(createGraphMatrix());
          break;

      }

      components = createComponents();
      slowdraw = 0;
    }
    slowdraw++;
  }

  // show stats and toggled settings along the top
  void showstats(){
    int tablestart = width-400;
    fill(0);
    textSize(20);
    text("n = " + vertexes.size() ,10,20);
    text("m = " + edge_amount , 80, 20);
    text("C = " + components.size(), 165, 20 );

    // create a colored box for each setting red off green on
    text("d", 230,20);
    if(digraph){
      fill(colors[3]);
    } else {
      fill(colors[1]);
    }
    square(245, 6, 12);
    fill(0);

    text("bi", 270,20);
    if(bipartitegraph){
      fill(colors[3]);
    } else {
      fill(colors[1]);
    }
    square(290, 6, 12);
    fill(0);

    text("vnums", 315,20);
    if(showvnums){
      fill(colors[3]);
    } else {
      fill(colors[1]);
    }
    square(380, 6, 12);
    fill(0);

    text("enums", 397,20);
    if(showenums){
      fill(colors[3]);
    } else {
      fill(colors[1]);
    }
    square(462, 6, 12);
    fill(0);
    text("last key = " + lastkey, 10, height-10);

    if(showtable) {

      if (transitiontable != null) {

        int i = 0;
        for (int[] vert : transitiontable) {
          int j = 0;
          for (int edg : vert) {
            if(edg == -1){
              text(edg, tablestart + j * 35, 25 + i * 17);
            } else {
              text(" "+ edg, tablestart + j * 35, 25 + i * 17);
            }
            j++;
          }
          i++;
        }
      }
      switch (curtype){
        case graph:
          text("L" ,tablestart-17, 17);
          break;
        case indice:
          text("i" ,tablestart-17, 17);
          break;
        case diagnal:
          text("D" ,tablestart-17, 17);
          break;
        case adjacecny:
          text("A" ,tablestart-17, 17);
          break;
        case laplace:
          break;
      }
    }

    // draw load/save buttons
    for(int i = 0; i<graphs.size(); i++){
      text(graphs.get(i),10, loadBY+7+22*i );
      fill(colors[3]);
      ellipse(loadBX,loadBY+22*i, loadBD, loadBD);
      fill(colors[1]);
      ellipse(saveBX,loadBY+22*i, loadBD, loadBD);
      fill(0);
    }
    fill(50);
  }

  // using processing mousedragged function
  public void mouseDragged(){

    if(vertex1 != null) {
      vertex1.ypos = pmouseY;
      vertex1.xpos = pmouseX;
    } else {
      for(Vertex v : vertexes){
        float dsq = sq(mouseX - v.xpos) + sq(mouseY - v.ypos);
        if(dsq <= sq(circleD/2)){
          v.xpos = pmouseX;
          v.ypos = pmouseY;
          break;
        }
      }
    }
  }

  // using processing mousepressed fucntion
  public void mousePressed() {
    // stop looping so we dont calculate tables while modifying vertexes/edges
    noLoop();
    boolean incircle = false;


    breakout:
    {
      // if we click a load or delete save button
      for (int i = 0; i < graphs.size(); i++) {
        float dsq = sq(mouseX - loadBX) + sq(mouseY - (loadBY + 22 * i));

        // check if clicked in a load button
        if (dsq <= sq(loadBD / (float) 2)) {
          load(graphs.get(i));
          break breakout;
        }

        // check if clicked in a delete button if yes then delete the file.
        dsq = sq(mouseX - saveBX) + sq(mouseY - (loadBY + 22 * i));
        if (dsq <= sq(loadBD / (float) 2)) {
          int num = graphs.get(i);
          File file = new File("graph" + num + ".ser");
          try {
            file.delete();
            graphs.remove(graphs.indexOf(num));
          } catch (Exception ex) {
            ex.printStackTrace();
          }
          break breakout;
        }
      }

      // if there are no vertexes when we click create 1
      if (vertexes.size() == 0) {
        Vertex v = new Vertex(mouseX, mouseY);
        vertexes.add(v);
        vertex2 = vertex1;
        vertex1 = v;
        vertex_amount++;
        break breakout;
      }

      // check if clicked inside a circle
      for (Vertex v : vertexes) {
        if (incircle) {
          break;
        }
        float dsq = sq(mouseX - v.xpos) + sq(mouseY - v.ypos);
        if (dsq <= sq(circleD / 2)) {
          incircle = true;

          // if we want to remove a vertex
          if (mouseButton == RIGHT) {
            ArrayList<Edge> templist = new ArrayList<>(v.connected_edges);

            // templist bc delete deletes the edge from v.connectededges would have concurrent modification exception
            for (Edge e : templist) {
              e.delete();
              edges.remove(e);
            }
            vertexes.remove(v);
            vertex2 = null;
            vertex1 = null;
            break breakout;
          }
          // set last clicked vertexes
          vertex2 = vertex1;
          vertex1 = v;
        }
      }


      // if we clicked inside 2 circles create an edge
      if (vertex1 != null && vertex2 != null && keyPressed) {
        // removing an edge
        if (key == 'z') {
          ArrayList<Edge> toremove = new ArrayList<>();
          for (Edge e : edges) {
            if (e.connected1.equals(vertex1) && e.connected2.equals(vertex2)) {
              e.size--;
              e.edgesto1--;
              vertex1 = null;
              vertex2 = null;
            } else if (e.connected1.equals(vertex2) && e.connected2.equals(vertex1)) {
              e.size--;
              e.edgesto2--;
              vertex1 = null;
              vertex2 = null;
            } else if (e.connected1.equals(vertex1) && e.connected2.equals(vertex2)) {
              e.size--;
              vertex1 = null;
              vertex2 = null;
            }
            if (e.size == 0) {
              toremove.add(e);
            }
          }
          // remove marked edges so theres no concurrent modification
          for (Edge e : toremove) {
            e.connected1.connected_edges.remove(e);
            e.connected2.connected_edges.remove(e);
            edges.remove(e);
          }
          break breakout;
        }
        // adding edge stuff
        if (key == 'e') {
          for (Edge e : edges) {
            if (e.connected1.equals(vertex1) && e.connected2.equals(vertex2)) {
              e.size++;
              e.edgesto1++;
              vertex1 = null;
              vertex2 = null;
              break breakout;
            } else if (e.connected1.equals(vertex2) && e.connected2.equals(vertex1)) {
              e.size++;
              e.edgesto2++;
              vertex1 = null;
              vertex2 = null;
              break breakout;
            } else if (e.connected1.equals(vertex1) && e.connected2.equals(vertex2)) {
              e.size++;
              vertex1 = null;
              vertex2 = null;
              break breakout;
            }
          }
          edge_amount++;
          Edge newedge = new Edge(vertex1, vertex2);
          edges.add(newedge);
          vertex1.connected_edges.add(newedge);
          if (!vertex1.equals(vertex2)) {
            vertex2.connected_edges.add(newedge);
            newedge.edgesto1++;
          }
          vertex1 = null;
          vertex2 = null;
          break breakout;
        }
      }
      if (!incircle) {
        Vertex newv = new Vertex(mouseX, mouseY);
        vertexes.add(newv);
        vertex1 = newv;
        vertex_amount++;
      }
    }
    loop();
    }

  // using processing keyReleased function
  public void keyReleased(){
    lastkey = key;
    System.out.println(key);

    switch (key) {
      case 'b':
        findBridges();
        break;
      case 'p':
        bipartitegraph = bipartite();
        break;
      case 't':
        showtable = !showtable;
        createTransitionTable();
        break;
      case 'm':
        int[][] t = transitiontable;
        for(int[] i : t){
          System.out.println();
          for(int j : i){
            System.out.print(j);
          }
        }
        System.out.println("--");
      case 'c':
        for(Vertex v : vertexes){
          v.vcolor = 0;
          v.num = vertexes.indexOf(v);
        }
        for(Edge e : edges ){
          e.isBridge = false;
          e.dijkstrapath = false;
        }
        break;
      case 'C':
        noLoop();
        vertex1 = null;
        vertex2 = null;
        vertex_amount = 0;
        vertexes.clear();
        edges.clear();
        transitiontable = null;
        loop();
        break;
      case 'd':
        digraph = !digraph;
        break;
      case 'n':
        showvnums = !showvnums;
        break;
      case 'N':
        showenums = !showenums;
        break;
      case '[':
        if (vertex1 != null) {
          vertex1.vcolor -= 1;
        }
        break;
      case ']':
        if (vertex1 != null) {
          vertex1.vcolor += 1;
        }
        break;
      case '1':
        curtype = matrixtype.indice;
        break;
      case '2':
        curtype = matrixtype.graph;
        break;
      case '3':
        curtype = matrixtype.adjacecny;
        break;
      case '4':
        curtype = matrixtype.diagnal;
        break;
      case '5':
        curtype = matrixtype.laplace;
        break;
      case 's':
        try {
          save();
        } catch (IOException e) {
          e.printStackTrace();
        }
        break;
      case 'l':
        load(graphnum);
        break;
      case 'j':
        dijkstra();
        break;
      case 'a':
        spanningTree();
        break;
    }

    // loop colors so they are not out of bounds
    if( vertex1 != null ) {
      if (vertex1.vcolor < 0) {
        vertex1.vcolor = colors.length - 1;
      } else if (vertex1.vcolor >= colors.length) {
        vertex1.vcolor = 0;
      }
    }

  }

  // rows are vertices and columnes are edges
  // indice matrix
  int[][] createTransitionTable(){

    if(edge_amount < edges.size()){
      return null;
    }
    int[][] vertexlist = new int[vertexes.size()][edge_amount];
    int extraedges = 0;
    for(Edge e : edges){
      // need to create matrix for directed graph
      if(digraph){
        int nomod = extraedges; // need to increment counter in loop but not modify loop bounds
        for(int i = nomod; i<e.edgesto1+nomod; i++){
          vertexlist[vertexes.indexOf(e.connected1)][edges.indexOf(e) + i] = -1;
          vertexlist[vertexes.indexOf(e.connected2)][edges.indexOf(e) + i] = 1;
          if(e.size > 1 && i != nomod) {
            extraedges++;
          } else if(e.edgesto2 > 0){
            extraedges++;
          }

        }
        nomod = extraedges;
        for(int i = nomod; i<e.edgesto2+nomod; i++){
          vertexlist[vertexes.indexOf(e.connected1)][edges.indexOf(e) + i] = 1;
          vertexlist[vertexes.indexOf(e.connected2)][edges.indexOf(e) + i] = -1;
          if(e.size > 1 && i != nomod) {
            extraedges++;
          }
        }

      } else {
        vertexlist[vertexes.indexOf(e.connected1)][edges.indexOf(e) + extraedges] = 1;
        vertexlist[vertexes.indexOf(e.connected2)][edges.indexOf(e) + extraedges] = 1;
      }


      if(e.size > 1 && !digraph ) {
        for(int i = 1; i < e.size; i++) {
          extraedges++;
          vertexlist[vertexes.indexOf(e.connected1)][edges.indexOf(e)+extraedges] = 1;
          vertexlist[vertexes.indexOf(e.connected2)][edges.indexOf(e)+extraedges] = 1;
        }
      }
    }

    return vertexlist;
  }

  int[][] createGraphMatrix(){

    int[][] B = createTransitionTable();
    if((B == null) || (B.length == 0) || (B[0].length == 0)){
      System.out.println("matrix is null");
      return null;
    }
    int[][] Bt = transposeMatrix(B);


    return multiplyMatrixes(B,Bt);
  }

  int[][] createAdjacencyMatrix(int[][] M){
    int[][] A = new int[M.length][M[0].length];
    for(int i = 0; i < M.length; i++){
      for(int j = 0; j<M[0].length; j++){
        if( i!= j ){
          A[i][j] = M[i][j];
        }
      }
    }
    return A;
  }

  int[][] createDiagnalMatrix(int[][] M){
    int[][] C = new int[M.length][M[0].length];
    for(int i =0; i < M.length; i++){
      C[i][i] = M[i][i];
    }
    return C;
  }

  int[][] createLaplaceMatrix(int[][] B){
    if(B == null){
      System.out.println("matrix is null");
      return null;
    }
    int[][] A = createAdjacencyMatrix(B);
    int[][] D = createDiagnalMatrix(B);

    for(int i = 0; i < B.length; i++){
      for(int j = 0; j<B[0].length; j++){
        B[i][j] = D[i][j] - A[i][j];
      }
    }

    return B;

  }

  int[][] multiplyMatrixes(int[][] A, int[][] B){
    if(A[0].length != B.length){
      System.out.println("matrixes are different sizes");
      return null;
    }
    int m = B.length;

    int[][] C = new int[A.length][B[0].length];
    for(int i = 0; i < A.length; i++){
      for(int j = 0; j<B[0].length; j++){
        int sum = 0;
        for (int k = 0; k< m; k++){
          sum = sum + A[i][k] * B[k][j];
        }
        C[i][j] = sum;
      }
    }
    return C;
  }

  int[][] transposeMatrix(int[][] M){
    int[][] T = new int[M[0].length][M.length];
      for(int i = 0; i < M.length; i++){
        for(int j = 0; j<M[0].length; j++){
          T[j][i] = M[i][j];
        }
      }
    return T;
  }

  ArrayList<ArrayList<Vertex>> createComponents(){
    ArrayList<ArrayList<Vertex>> graph = new ArrayList<>();
    for(Vertex v : vertexes){

      // if the vertex is already in a component skip it
      if(vertexInComp(v,graph)){continue;}

      // BFS all connected vertexes and add them to a component
      ArrayList<Vertex> component = new ArrayList<>();
      Queue<Vertex> tocheck = new LinkedList<>();
      tocheck.add(v);
      while(!tocheck.isEmpty()){
        Vertex current = tocheck.remove();
        if(!component.contains(current)){
          component.add(current);
          for(Edge e : current.connected_edges){
            tocheck.add(e.connected1);
            tocheck.add(e.connected2);
          }
        }
      }

      graph.add(component);
    }
    return graph;
  }

  // helper function
  boolean vertexInComp(Vertex v, ArrayList<ArrayList<Vertex>> graph){
    for(ArrayList<Vertex> g : graph){
      if(g.contains(v)){
        return true;
      }
    }
    return false;
  }

  void findBridges(){
    // reset variables
    for(Edge e : edges){
      e.isBridge = false;
    }


    for(Vertex v : vertexes){
      for(Edge eB : v.connected_edges){
        boolean isbridge = true;
        ArrayList<Vertex> component = new ArrayList<>();
        Queue<Vertex> tocheck = new LinkedList<>();
        if(eB.connected1.equals(v)){
          tocheck.add(eB.connected2);
        }else {
          tocheck.add(eB.connected1);
        }

        // BFS for the vertex
        while(!tocheck.isEmpty() && isbridge){
          Vertex current = tocheck.remove();
          if(current.equals(v)){
            isbridge = false;
          }
          if(!component.contains(current)){
            component.add(current);
            for(Edge e : current.connected_edges){
              if(edges.indexOf(e) == edges.indexOf(eB)){
                continue;
              }
              tocheck.add(e.connected1);
              tocheck.add(e.connected2);
            }
          }
        }
        if(isbridge){eB.isBridge = true;}

      }
    }
  }

  boolean bipartite(){
    // setup variables and reset colors
    boolean isbipartite = true;
    for(Vertex v : vertexes){
      v.vcolor = 0;
    }

    //  goes by components so if 2 components are bipartite the graph is too
    for(ArrayList<Vertex> c : components) {
      int curcolor = 1;
      Vertex start = c.get(0);
      start.vcolor = 2;
      Queue<Vertex> tocheck = new LinkedList<>();
      tocheck.add(start);

      // BFS color vertexs one of 2 colors
      while (!tocheck.isEmpty()) {
        Vertex current = tocheck.remove();
        if (current.vcolor == 1) {
          curcolor = 2;
        } else {
          curcolor = 1;
        }

        for (Edge e : current.connected_edges) {
          // find which end of the vertex is not the current vertex
          if (current.equals(e.connected2)) {
            // if we havnt colored this vertex yet
            if (e.connected1.vcolor == 0) {
              // add it to tocheck and color it
              tocheck.add(e.connected1);
              e.connected1.vcolor = curcolor;
            }
          } else {
            if (e.connected2.vcolor == 0) {
              tocheck.add(e.connected2);
              e.connected2.vcolor = curcolor;
            }
          }
        }
      }
    }

    // check if 2 edges have the same color
    for(Edge e : edges){
      if (e.connected1.vcolor == e.connected2.vcolor) {
        isbipartite = false;
        break;
      }
    }

    return isbipartite;
  }

  void dijkstra(){
    // create warnings for selected vertexs that dont have a path between them
    if(vertex2 == null || vertex1 == null || vertex1.equals(vertex2)){
      System.out.println("NO PATH: Only a single vertex is selected");
      return;
    }
    for(ArrayList<Vertex> g :components){
      if(g.contains(vertex1) && !g.contains(vertex2)){
        System.out.println("NO PATH: Selected vertexes are in different components");
        return;
      }
    }


    // reset dijkstra specific stuff
    for(Vertex v : vertexes){
      v.distance = -1;
      v.path = null;
      v.vcolor = 0;
    }
    for(Edge e: edges){
      e.dijkstrapath = false;
    }

    // create lists
    ArrayList<Vertex> checked = new ArrayList<>();
    Queue<Vertex> tocheck = new LinkedList();

    // setup endpoints
    vertex1.distance = 0;
    tocheck.add(vertex1);
    checked.add(vertex2);
    while (!tocheck.isEmpty()){
      Vertex current = tocheck.remove();
      if(checked.contains(current)){
        continue;
      }
      checked.add(current);

      for(Edge e : current.connected_edges){
        Vertex mod;
        mod = e.getOtherVertex(current);
        tocheck.add(mod);
        if(mod.distance == -1 || mod.distance > current.distance + 1){
          mod.distance = current.distance + 1;
          mod.path = e;
        }
      }
    }

    // color the path using the created references
    Vertex current = vertex2;
    while(current.path != null){
      current.vcolor = 5;
      current.path.dijkstrapath = true;
      current = current.path.getOtherVertex(current);
    }
    current.vcolor = 5;
  }

  void spanningTree(){
    if(components.size() > 1 ){
      System.out.println("More than one component");
      return;
    }

    // reset variables
    for(Vertex v : vertexes){
      v.path = null;
    }
    for(Edge e : edges){
      e.dijkstrapath = false;
    }

    // setup stack
    Stack<Vertex> tocheck = new Stack();
    ArrayList<Vertex> checked = new ArrayList();
    if(vertex1 != null){
      tocheck.push(vertex1);
    } else tocheck.push(vertexes.get(0));

    // DFS through vertexes
    while (!tocheck.empty()){
      Vertex current = tocheck.pop();
      if(checked.contains(current)){
        continue;
      }
      checked.add(current);
      for(Edge e : current.connected_edges){
        Vertex neighbor = e.getOtherVertex(current);
        tocheck.push(neighbor);
        // if there is no path yet make one
        if(neighbor.path == null){
          neighbor.path = e;
          e.dijkstrapath = true;
        }
      }
    }
  }
  // a function to rototate a point around a point used for making edges look correct
  float[] rotatePoint(float Ox, float Oy, float Px, float Py, float theta){
    float Tx = Px-Ox;
    float Ty = Py-Oy;

    Px = Tx*cos(theta)-Ty*sin(theta);
    Py = Tx*sin(theta)+Ty*cos(theta);

    Px += Ox;
    Py += Oy;

    return new float[] {Px, Py};
  }

  // load a graph from the file "graph(graphnum)" if it exists
  public void load(int graphnum){
    noLoop();

    String filename = "graph"+graphnum+".ser";
    try {
      FileInputStream fis = new FileInputStream(filename);
      ObjectInputStream ois = new ObjectInputStream(fis);


      // only copy primitives because otherwise object references get messed up and it wont work
      vertex_amount = (int) ois.readObject();
      edge_amount = (int) ois.readObject();

      vertexes.clear();
      ArrayList<Vertex> newverts = (ArrayList<Vertex>) ois.readObject();
      for (Vertex v : newverts) {
        Vertex vnew = new Vertex(v.xpos, v.ypos);
        vnew.vcolor = v.vcolor;
        vnew.num = v.num;
        vertexes.add(vnew);
      }

      edges.clear();
      ArrayList<Edge> newedges = (ArrayList<MAIN.Edge>) ois.readObject();
      for (Edge e : newedges) {

        Vertex v1 = null;
        Vertex v2 = null;
        for (Vertex v : vertexes) {
          if (v.equals(e.connected1)) {
            v1 = v;
          }
          if (v.equals(e.connected2)) {
            v2 = v;
          }
        }

        assert v1 != null;
        assert v2 != null;

        Edge enew = new Edge(v1, v2);
        v1.connected_edges.add(enew);
        v2.connected_edges.add(enew);
        enew.size = e.size;
        enew.edgesto1 = e.edgesto1;
        enew.edgesto2 = e.edgesto2;
        enew.isBridge = e.isBridge;
        enew.isloop = e.isloop;
        edges.add(enew);

      }

      transitiontable = (int[][]) ois.readObject();
      digraph = (boolean) ois.readObject();
      bipartitegraph = (boolean) ois.readObject();
      showvnums = (boolean) ois.readObject();
      showtable = (boolean) ois.readObject();
      curtype = (matrixtype) ois.readObject();
      Vertex v1 = (Vertex) ois.readObject();
      Vertex v2 = (Vertex) ois.readObject();

      // setup v1 and v2 to the right vertices
      for(Vertex v: vertexes){
        if(v1 != null && v1.equals(v)){
          vertex1 = v;
        }
        if(v2 != null && v2.equals(v)){
          vertex2 = v;
        }
      }


      ois.close();
      fis.close();
    }catch (InvalidClassException se){
      System.out.println("modified class");
    } catch (Exception ex){
      ex.printStackTrace();
    }

    // recreate components instead of copy
    loop();
    System.out.println("loaded "+filename);
  }

  // save a graph to a file
  public void save() throws IOException {
    noLoop();
    String filename = "graph"+graphnum+".ser";
    File file = new File(filename);

    // dont overwrite other saved graphs
    while (file.exists()){
      graphnum++;
      filename = "graph"+graphnum+".ser";
      file = new File(filename);
    }
    graphs.add(graphnum);
    graphnum++;

    // create the output stream and write the graph attributes to it.
    FileOutputStream fout = new FileOutputStream(filename);
    ObjectOutputStream oos = new ObjectOutputStream(fout);
    oos.writeObject(vertex_amount);
    oos.writeObject(edge_amount);
    oos.writeObject(vertexes);
    oos.writeObject(edges);

    oos.writeObject(transitiontable);
    oos.writeObject(digraph);
    oos.writeObject(bipartitegraph);
    oos.writeObject(showvnums);
    oos.writeObject(showtable);
    oos.writeObject(curtype);
    oos.writeObject(vertex1);
    oos.writeObject(vertex2);

    oos.close();
    fout.close();
    loop();
  }
}

