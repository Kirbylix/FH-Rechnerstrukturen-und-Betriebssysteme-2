#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
# include <string.h>
#define MAXLINE 4096

int main(void){

int n, fd[2];
char buf[MAXLINE];
char *data = "hello World";

pipe(fd);				// Pipe erzeugen
write(fd[1], data, strlen(data));	// in die Pipe schreiben
if((n = read(fd[0], buf, 4097)) >= 0){	// aus der Pipe lesen
	buf[n] = 0;
	printf("lesen %d bytes von der pipe: %s \n", n, buf);
}else{
	perror("pipe error");
	exit(1);
}

printf("stdin %d \n",fd[0]);
printf("stout %d \n",fd[1]);
printf("sterr %d \n",fd[2]);

	return 0;
}
