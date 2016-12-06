#include <stdio.h>
#include <stdlib.h> 

void quadrat(float*);

int main() {
	float seitenlaenge = 3.7;
	quadrat(&seitenlaenge);			// & liefert die Adresse (=Adressoperator
	system("Pause;");
}
void quadrat(float *zahl) {
	float flaeche = *zahl * *zahl;
	printf("Die Flaeche ist %0.2f lang \n", flaeche);
}