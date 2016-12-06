#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>

#define MAXLINE 4096
#define TRUE 1

int main(void)
{
	int n, fifo1, fifo2, i;
	char puffer[laenge];
	
	// FIFO 1 erzeugen
	fifo1 = mkfifo("/tmp/fifo1", 0666);
	if(fifo1 < 0)
	{
		perror("FIFO 1 wurde bereits erzeugt");
	}
	
	// FIFO 2 erzeugen
	fifo2 = mkfifo("/tmp/fifo2", 0666);
	if(fifo2 < 0)
	{
		perror("FIFO 2 wurde bereits erzeugt");
	}
	
	while(TRUE)
	{
			//FIFO 1 öffnen
			fifo1 = open("/tmp/fifo1", O_RDONLY);
			if(fifo1 < 0)
			{
				perror("Fehler beim öffnen der FIFO 1");
				exit(1);
			}
			
			//FIFO 2 öffnen
			fifo2 = open("/tmp/fifo2", O_WRONLY);
			if(fifo2 < 0)
			{
				perror("Fehler beim öffnen der FIFO 2");
				exit(1);
			}
			
			//Aus FIFO 1 lesen und in FIFO 2 schreiben
			while(TRUE)
			{
				n = read(fifo1, puffer, MAXLINE);
				
				if(n <= 0) break;
				
				for(i = 0; i<strlen(puffer); i++)
				{
					puffer[i] = toupper(puffer[i]);
				}
				
				write(fifo2, puffer, n);
			}
			
			//FIFOs schließen
			close(fifo1);
			close(fifo2);
	}
			
	//FIFOs löschen
	unlink("/tmp/fifo1");
	unlink("/tmp/fifo2");
	
	return 0;
}
