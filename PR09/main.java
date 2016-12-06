
public class main {

	public static void main(String[] args) {
		int x = 3;
		int y = 2;
		System.out.print(func(x,y));
		System.out.println();

	}
	
	public static int func(int x, int y){
		if((x-y) <= 0){
			return y - x;
		}else if (x-y == 0){
			return 0;
		}
		return (x-y);
	}
}