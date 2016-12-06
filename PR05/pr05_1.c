#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <stdio.h>

#define MAXLINE 4096

int main(){
	int i, n;
	char input[MAXLINE];
	
	/* INPUT solange read() > 0 oder Shell geschlossen wird */
	while((n = read(STDIN_FILENO, input, MAXLINE)) >= 0){
		input[n] = 0;
		
		/* Input in Großbuchstaben umwandeln */
		i = 0;
		while(input[i]){
			input[i] = toupper(input[i]);
			i++;
		}
		/* Ausgabe */
		write(STDOUT_FILENO, input, i);
	}
	exit(0);
}
