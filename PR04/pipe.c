#include <sys/wait.h>
#include <unistd.h>
#include <stdio.h> 
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#define MAXLINE 4096

int main(int argc, char** argv) {
  int   n, fdA[2], fdB[2];
  pid_t pid;
  char  line[MAXLINE];
  char *data = argv[1];
  
  if (pipe(fdA) < 0) {		// Pipe A
    perror("pipe error");
    exit(1);
  }

  if (pipe(fdB) < 0) {		// Pipe B
    perror("pipe error");
    exit(1);
  }

switch(pid = fork()){		//FORK
	case -1:	perror("fork error");
			exit(1);
			break;
	case 0: //Kind - 
		// Empfangen - Pipe A
		close(fdA[1]);
		n = read(fdA[0], line, MAXLINE);
		//write(STDOUT_FILENO, line, n);
		sleep(1);
		// Senden - Pipe B
		close(fdB[0]);
		int i = 0;
		while(line[i])
		{
			line[i] = toupper(line[i]);
			i++;
		}
		write(fdB[1], line, strlen(line));
		break;
	default: // Vater
		// Senden - Pipe A
		close(fdA[0]);	
		write(fdA[1], data, strlen(data));
		// Empfangen - Pipe B
		close(fdB[1]);
		n = read(fdB[0], line, MAXLINE);
		write(STDOUT_FILENO, line, n);
		printf("\n");
		break;
}

  wait(NULL);
  return 0;
}
