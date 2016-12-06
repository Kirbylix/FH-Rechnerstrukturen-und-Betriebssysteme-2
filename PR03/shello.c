#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>	// execlp()
#include <string.h>	// strlen()
#include <stdlib.h>
#include <stdio.h>
#include <signal.h>

#define MAXLINE 4096	/* max line length */
#define CHILD 0
#define ERROR -1

//Signal Handler
void sig_handler(int sig){
	printf("\n abgefangen :P \n");
	if(sig == SIGINT){
		printf("SIGINT wurde abgefangen :P \n");
	}else if(sig == SIGTERM){
		printf("SIGTERM wurde abgefangen :P \n");
	}
}
// MAIN
int main(void) {
  char	buf[MAXLINE];
  pid_t   pid;
  int	status;
  //Signale
	if(SIG_ERR == signal(SIGINT, sig_handler) || // für Strg+C
	   SIG_ERR == signal(SIGTERM, sig_handler)){ 
		fprintf(stderr, "Fehler beim handler \n");
		exit(1);
	}
	
  printf("%% ");  /* print prompt (printf requires %% to print %) */
  while (fgets(buf, MAXLINE, stdin) != NULL) 
  {
	buf[strlen(buf) - 1] = 0;	/* replace newline with null */
	// Eingabe abfrage
	if(buf[0] == 'q' && strlen(buf) == 1){
		printf("Shell wurde beendet\n");
		exit(0);
	}else if(buf[0] == 'k' && strlen(buf) == 1){
		printf("Sende Kill Kommando ]:->\n");
		kill(getpid(), SIGKILL);
	}

	switch(pid=fork()){
		case ERROR:perror("fork error");
			break;
		// Kind
		case CHILD:if(buf[strlen(buf)-1] == '&'){
			printf("Kind: Mein PID lautet: %d\n", getpid());
			buf[strlen(buf)-2] = '\0';
			execlp(buf, buf, '&', (char *) 0);
			printf("couldn't execute: %s\n", buf); 
			exit(127);}
		// Vater
		default:printf("Vater: Mein PID lautet: %d\n", getpid());
	}
		

	if ( (pid = waitpid(pid, &status, 0)) < 0)
		perror("waitpid error"); 
	printf("%% ");
  }
  exit(0);
}

