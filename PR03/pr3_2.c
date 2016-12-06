#include <stdio.h>
#include <stdlib.h>
#include <signal.h>

void handler(int sig)
{
	printf("\nSignal %d gefangen\n", sig);
	printf("Programm wird beendet\n");
	exit(1);
}

int main(void)
{
	if(SIG_ERR == signal(SIGINT, handler)){
		fprintf(stderr, "Fehler beim Handler\n");
		exit(1);
	}
	printf("Handler lauft\n");
	for(;;){
		puts("Arbeiten");
		sleep(1);
	}
//	return 0;
}
