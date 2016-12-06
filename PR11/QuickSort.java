/** rekursives Sortieren mit Quicksort Singlethreaded
*  Prof. Dr. Rolf Swik
*/
public class QuickSort extends Object {
	
  public QuickSort() {
	super();

	QuickSortTestFrame.textarea.append("\n -> "+ this ); 
			}

  public  void sort (int[] a, int unten, int oben) {
    int tmp ;
    int i = unten;
    int j = oben;
    int x = a[(unten+oben) / 2];                  // Pivotelement, willkuerlich
    StringBuffer strB = new StringBuffer();
	
    do {
        while (a[i] < x) i++;                     // x fungiert als Bremse
        while (a[j] > x) j--;                     // x fungiert als Bremse
        if ( i<=j )  {
            tmp  = a[i];                          // Hilfsspeicher
            a[i] = a[j];                          // a[i] und
            a[j] = tmp;                           // a[j] werden getauscht
            i++;
            j--;
        }
    } while (i <= j);
                              // alle Elemente der linken Haelfte sind kleiner
                              // als alle Elemente der rechten Haelfte
         
	 strB.append("\n Teilsortierung ->: "+ this + ":  Pivot= "+x+" \n");
	 QuickSortTestFrame.textarea.append("");
     for (int tag=unten;tag<=oben;tag++)  strB.append(" "+a[tag]);
     QuickSortTestFrame.textarea.append("\nThread-ID: " + Thread.currentThread().getId() + strB.toString() );

     tQuickSort t1 = new tQuickSort(a, unten, j);
     tQuickSort t2 = new tQuickSort(a, i, oben);
     
    if (unten < j)  t1.start();            // sortiere linke Haelfte
    if (i < oben )  t2.start();            // sortiere rechte Haelfte
    
    try {
		t1.join();
		t2.join();
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
    
  }

  public class tQuickSort extends Thread{
	  int[] a;
	  int unten;
	  int oben;
	  
	  public tQuickSort(int[] a, int unten, int oben){
		  this.a=a;
		  this.unten= unten;
		  this.oben = oben;
	  }
	  
	  public void run(){
		  sort(a, unten,oben);
	  }
  }
}
