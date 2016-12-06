#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void *zeige(void *arg);

int main (int argc, char *argv[])
{
	int status, i;
	int data[3];
	pthread_t thread[3];
	
	/*data[0] = atol(argv[1]);
	data[1] = atol(argv[2]);
	data[2] = atol(argv[3]);*/

	//Konsole eingabe
	for(i = 0; i<3; i++){
		printf("Bitte eine Zahl Eingeben:");
		scanf("%d",&data[i]);
	}
	
	// 3 Threads erzeugen
	for(i = 0; i<3; i++){
		status = pthread_create(&thread[i], NULL, &zeige, &data[i]);
		if(status!=0){
			fprintf(stderr,"Error: Konnte Thread nicht erzeugen\n");
			exit(-1);		
		}
	}

	/* Warte auf Terminierung */
	for(i = 0; i<3; i++){
		pthread_join(thread[i],NULL);
	}
	return 0;
}

void *zeige(void *arg){

	int *dat = (int*)arg;
	
	sleep(*dat);
	printf("Habe folgende Daten erhalten %d\n",*dat);

	return NULL;
}
