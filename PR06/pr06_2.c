#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdio.h> 
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#define MAXLINE 4096

int main(int argc, char* argv[]) 
{
	int n, fifo1, fifo2;
	pid_t pid;
	char line[MAXLINE];
  
	//FIFO 1 öffnen
	fifo1 = open("/tmp/fifo1", O_WRONLY);
	if(fifo1 < 0)
	{
		perror("Fehler beim öffnen der FIFO 1");
		exit(1);
	}
			
	//FIFO 1 öffnen
	fifo2 = open("/tmp/fifo2", O_RDONLY);
	if(fifo2 < 0)
	{
		perror("Fehler beim öffnen der FIFO 2");
		exit(1);
	}
	
	//In FIFO 1 schreiben
	write(fifo1, argv[1], strlen(argv[1]));
	//Aus FIFO 2 lesen
	n = read(fifo2, line, MAXLINE - 1);
	line[n++] = '\n';
	write(STDOUT_FILENO, line, n);

	return 0;
}
