#include <sys/wait.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>

#define MAXLINE 4096

int main(){
	int p1[2], p2[2], n;
	pid_t pid;
	char line[MAXLINE], input[MAXLINE];
	
	if(pipe(p1)<0){	// Pipe 1 erzeugen
		perror("pipe1 error");
		exit(1);
	}
	
	if(pipe(p2)<0){	// Pipe 2 erzeugen
		perror("pipe1 error");
		exit(1);
	}
	
	switch(pid = fork()){
		case -1: //ERROR
			perror("Fork ERROR");
			exit(1);
			break;
		case 0: // KIND
			close(p1[1]);
			close(p2[0]);
			dup2(p1[0], STDIN_FILENO);
			close(p1[0]);
			dup2(p2[1], STDOUT_FILENO);
			close(p2[1]);
			execlp("./pr05_1", "./pr05_1", NULL);
			perror("execl ERROR");
			exit(1);
			break;
		default: // VATER
			close(p1[0]);
			close(p2[1]);
			n = read(STDIN_FILENO, input, MAXLINE);
			write(p1[1], input, strlen(input));
			
			n = read(p2[0], line, MAXLINE);
			line[n++] = '\n';
			write(STDOUT_FILENO, line, n);
			break;
	}
	exit(0);
}
