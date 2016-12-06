/* Das Beispielprogramm dateiinfo.c ermittelt Informationen ueber die
   als Argument angegebenen Dateinamen */

#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>


/* Die Elemente in der Struktur stat haben nachstehende Bedeutung:

Datentyp:     Element:      Bedeutung:                              %:

dev_t         st_dev        device                                  0x%x
ino_t         st_ino        inode                                   %lu
mode_t        st_mode       protection                              0%o
nlink_t       st_nlink      number of hard links                    %d
uid_t         st_uid        user ID of owner                        %d
gid_t         st_gid        group ID of owner                       %d
dev_t         st_rdev       device type (if inode device)           0x%x
off_t         st_size       total size, in bytes                    %ld
unsigned long st_blksize    blocksize for filesystem I/O            %ld
unsigned long st_blocks     number of blocks allocated              %ld
time_t        st_atime      time of last access                     %ld
time_t        st_mtime      time of last modification               %ld
time_t        st_ctime      time of last change                     %ld

*/

int main(int argc, char** argv)
{

  int i, err;
  mode_t typ, perm;
  struct stat buf;
  char* ptr;

  for (i = 1; i < argc; i++) {   /* Schleife ueber alle Argumente */
    printf("%s: ", argv[i]);     /* Ausgabe des Dateinamens       */

    err = lstat(argv[i], &buf);  /* Ermittlung der Informationen ueber die
                                    Datei argv[i], der Rueckgabewert ist 0
                                    bei Erfolg, sonst < 0,
                                    in buf stehen die Informationen */

    if (err < 0) {                           /* Abfrage auf Fehler */
      fprintf(stderr, "Fehler in lstat\n");   /* Fehlermeldung ausgeben */
      continue;                         /* weiter mit dem naechsten Argument */
    }

    /* Ermittlung des Dateityps */

    typ = buf.st_mode >> 12;             /* die unteren Bits rausschieben, nur
                                          die oberen 4 Bits werden benoetigt  */
    switch (typ) {
    case 010: printf("Regulaere Datei,"); break;
    case 004: printf("Verzeichnis, "); break;
    case 002: printf("Zeichen-Geraetedatei, "); break;
    case 006: printf("Block-Geraetedatei, "); break;
    case 001: printf("FIFO, "); break;
    case 012: printf("Symbolischer Link, "); break;
    case 014: printf("Socket, "); break;
    default:  printf("unbekannter Typ,");
    }

    printf("I-Node: %ld \n, ", (long) buf.st_ino); /* I-Node als Dezimalzahl ausgeben */
//Aufgabe 2.3
    printf("IUD: %d \n", buf.st_uid);
    printf("GID: %d \n", buf.st_gid);
    printf("Groesse: %ld \n", buf.st_size);

    perm = buf.st_mode & 0777;   /* Nur die unteren 12 Bits ausgeben */
    printf("Zugriffsrechte: 0%o\n", perm); /* Rechte als Oktalzahl ausgeben */
//Aufgabe 2.4
    printf("Zugriffsrechte: ");
	
//USER
	if(buf.st_mode & S_IRUSR) printf("r");
	else printf("-");

	if(buf.st_mode & S_IWUSR) printf("w");
	else printf("-");
	
	if(buf.st_mode & S_IXUSR) printf("x");
	else printf("-");
//Goup
	if(buf.st_mode & S_IRGRP) printf("r");
	else printf("-");

	if(buf.st_mode & S_IWGRP) printf("w");
	else printf("-");
	
	if(buf.st_mode & S_IXGRP) printf("x");
	else printf("-");
//Other
	if(buf.st_mode & S_IROTH) printf("r");
	else printf("-");

	if(buf.st_mode & S_IWOTH) printf("w");
	else printf("-");
	
	if(buf.st_mode & S_IXOTH) printf("x \n");
	else printf("- \n");
  }
  return 0;
}
