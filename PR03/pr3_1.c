#include <stdio.h>
#include <stdlib.h>
#include <signal.h>

typedef void (*sighandler)(int);

int main(void)
{
	kill(getpid(), SIGKILL);
	return 0;
}
