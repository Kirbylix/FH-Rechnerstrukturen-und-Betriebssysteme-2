#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/ipc.h>
#include <sys/sem.h>
#include <sys/stat.h>
#include "sm.h"
#include "pv.c"

static int sem_anfordid;

int main(int argc,char* argv[])
{
	int anzahl_ressourcen,freie_ressourcen;
	int i;

	/* Uebergebene Argumente pruefen */
	if(argc!=2){
		fprintf(stderr,"usage: %s <Anzahl Ressourcen>\n",argv[0]);
		exit(-1);
	}
	
	/* ‹bergebene Anzahl von Resourcen in integer wandeln */
	anzahl_ressourcen = atol(argv[1]);

	/* Anlegen des Semaphors */
	sem_anfordid = sem_connect(SEM_READKEY);

	/* Freigeben der Ressourcen */
	pv(sem_anfordid, anzahl_ressourcen);

	/* Anzahl nun freier Ressourcen abfragen */
	freie_ressourcen = sem_get(sem_anfordid);

   /* Anzahl der freigegebene und der insgesamt freien Ressourcen ausgeben */
	printf("Anzahl der Freigegeben: %d \ninsgesamt freien Ressourcen: %d \n",anzahl_ressourcen, freie_ressourcen);

	exit(0);
}
