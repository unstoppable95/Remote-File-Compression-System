#include <iostream>
#include <fstream>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <signal.h>
#include <dirent.h>
#include <errno.h>
#include <vector>


using namespace std;
//funkcja pobierająca zawartość katalogu
int getdir (string dir, vector<string> &files)
{
    DIR *dp;
    struct dirent *dirp;
    if((dp  = opendir(dir.c_str())) == NULL)
    {
        cout << "Error(" << errno << ") opening " << dir << endl;
        return errno;
    }
    while ((dirp = readdir(dp)) != NULL )
    {

        files.push_back(string(dirp->d_name));
    }
    closedir(dp);
    return 0;
}

int main()
{
    int ON = 1;
    int HEADERSIZE =110;
    int PORT_NUMBER=12345;
    int socketDescriptor = socket(PF_INET, SOCK_STREAM, 0);
    setsockopt(socketDescriptor,SOL_SOCKET, SO_REUSEADDR, (char*) &ON, sizeof(ON));
    //tworzenie struktury polaczenia
    struct sockaddr_in str;
    str.sin_family = PF_INET;
    str.sin_port = htons(PORT_NUMBER);
    str.sin_addr.s_addr = INADDR_ANY;
    bind(socketDescriptor,(struct sockaddr*) &str, sizeof(str));
    listen(socketDescriptor, 0);

    while(1)
    {
        struct sockaddr_in newClient;
        socklen_t size;
        int client = accept(socketDescriptor, (struct sockaddr*)&newClient, &size);

        //tworzenie nowego procesu dla podłączonego klienta
        if (fork() == 0)
        {
            close(socketDescriptor);
            if(client != -1)
            {

                //odczytanie znaku polecenia jakie chce wykonać klient
                char messageBufforHeader[1];
                read(client, &messageBufforHeader, 1);
                char typeMessage = messageBufforHeader[0];

                switch(typeMessage)
                {
                case 'c': //c => KOMPRESJA
                {

                    //WCZYTANIE DANYCH OD KLIENTA
                    cout<<client<< " Kompresja : ";
                    char header[HEADERSIZE];
                    //wczytanie nagłówka
                    for (int i = 0; i < HEADERSIZE; i++)
                    {
                        char buf[1];
                        read(client, &buf, 1);
                        header[i] = buf[0];
                    }
                    int fileSize = 0;
                    int start = 0;

                    //zdekodowanie rozmiaru z nagłówka
                    for (int i = 86; i <100; i++)
                    {
                        int number = header[i] - 48;
                        if (number != 0)
                        {
                            start = 1;
                        }
                        if (start == 1)
                        {
                            fileSize = fileSize*10 + number;
                        }
                    }

                    cout<<"Rozmiar przeslanych danych od klienta  : "<<fileSize<<endl;

                    char * bufferToFile = new char[fileSize];

                    //przepisane nagłówka do bufora do pliku
                    for (int i=0 ; i<HEADERSIZE ; i++)
                    {
                        bufferToFile[i]=header[i];
                    }

                    int actualByte = HEADERSIZE;
                    char tmpBufforForData[1];

                    //wczytanie oraz przepisanie danych od klienta do buforu do pliku
                    do
                    {
                        int readed = read(client, &tmpBufforForData, 1);
                        for (int i = 0; i < readed; i++)
                        {
                            bufferToFile[actualByte] = tmpBufforForData[i];
                            actualByte++;
                        }
                    }
                    while(actualByte != fileSize);

                    cout<<endl<<"Wczytałem cały strumień wejściowy."<<endl;

                    //KOMPRESJA

                    //tworzymy folder dla archiwow .zip
                    system("mkdir -p archives");
                    //tworzymy folder do zapisu tymczasowych plikow do kompresji
                    system("mkdir -p tmpFilesForZip");

                    cout<<"Rozpoczynam kompresję"<<endl;

                    //tworzymy nazwe pliku archiwum
                    string archiveName= string(header);

                    // tworzymy nazwe pliku tymczasowego
                    string fileName = "./tmpFilesForZip/"+archiveName;
                    cout<<"Miejsce zapisania pliku tymczasowego : "<<fileName<<endl;

                    //zapis strumienia do pliku tymczasowego
                    fstream fs;
                    fs.open(fileName.c_str(), ios::binary|ios::out);
                    for(int i = 0; i<fileSize; i++)
                    {
                        fs.write((char*)&bufferToFile[i],1);
                    }
                    fs.close();

                    //tworzenie polecenia kompresujacego plik tymczasowy do archiwum .zip

                    string zipCommand ="zip ./archives/" + archiveName + ".zip" + "  ./tmpFilesForZip/"+archiveName+" -j  -FS -r ";

                    system (zipCommand.c_str());

                    cout<<"Uworzylem archiwum dla klienta o IP " << inet_ntoa((struct in_addr) newClient.sin_addr) <<endl;

                    string rmCommand = "rm " + fileName;
                    system (rmCommand.c_str());

                    cout<< "Usuniecie pliku tymczasowego " <<endl ;

                    break;
                }
                case 'd': //d => DEKOMPRESJA
                {

                    cout<<" Deskryptor klienta :  " << client<<endl<< " Dekompresja... "<<endl;
                    //wczytanie nagłówka archiwum, które klient chce pobrać; HEADERSIZE+4 bo musimy znaleźć archiwum .zip
                    char nameArchive[HEADERSIZE+4];
                    for (int i = 0; i < HEADERSIZE+4; i++)
                    {
                        char buf[1];
                        read(client, &buf, 1);
                        nameArchive[i] = buf[0];
                    }

                    //stworzenia nazwy archiwum po stronie serwera
                    string testArchiveName= string(nameArchive);
                    string archiveName = "./archives/"+testArchiveName.substr(0,HEADERSIZE+4);
                    cout<<endl<<"Nazwa archiwum : "<<endl <<archiveName<<endl;

                    //sprawdzenie czy istnieje żadane archiwum w katalogu archives
                    string dir = string("./archives");
                    vector<string> files = vector<string>();

                    getdir(dir,files);

                    for (unsigned int x = 0; x < files.size(); x++)
                    {
                        if (archiveName.compare(files[x])==0)
                        {
                            cout<<"Znaleziono archiwum w katalogu ./archives"<<endl;
                        }
                    }

                    //stworzenie polecenia dekompresujacego żądane archiwum
                    string unzipCommand= "unzip -u " + archiveName + " -d tmpFilesUnzipped";
                    system(unzipCommand.c_str());

                    fstream archive;


                    string archiveNameOpen="./tmpFilesUnzipped/"+testArchiveName.substr(0,HEADERSIZE);

                    cout<<"Nazwa pliku tymczasowego zdekompresowanego " <<archiveNameOpen <<endl;

                    //otworzenie zdekompresowanego pliku
                    archive.open(archiveNameOpen.c_str(),ios::binary|ios::in);
                    if(!archive.is_open()) return 0;

                    //sprawdzenie dlugosci pliku
                    archive.seekg(0,ios::end);
                    unsigned int archiveLength = archive.tellg();
                    archive.seekg(0,ios::beg);

                    cout<<"Dlugosc pliku po dekompresji : " <<archiveLength <<endl;

                    //tworzenie bufora do klienta
                    char buffer[archiveLength];

                    for(unsigned int i = 0; i<archiveLength; i++)
                    {
                        archive.read((char*)&buffer[i],1);
                    }

                    cout<<"Odczytalem bufor " <<endl;


                    //wyslanie rozpakowanego archiwum do clienta
            
                    unsigned int numberOfSendingChars =0;
                    char tmpBufforForData;

                    do
                    {
                        tmpBufforForData=buffer[numberOfSendingChars];
                        int readed = write(client, &tmpBufforForData, 1);
                        numberOfSendingChars+=readed;

                    }
                    while(numberOfSendingChars!= archiveLength);

                    //usniecie pliku tymczasowego z zdekompresowanym archiwum
                    string rmCommand = "rm -f "+ archiveNameOpen;
                    system(rmCommand.c_str());
                    cout<<"Usunalem plik tymczasowy" <<endl;
                    break;
                }
                case 'w': //w => POBIERZ LISTĘ DOSTĘPNYCH ARCHIWÓW
                {
                    //powiazanie z katalogiem archiwów
                    string dir = string("./archives");
                    vector<string> files = vector<string>();

                    getdir(dir,files);
                    int sizeNameFiles=0;

                    //tworzenie dlugosci bufora z nazwami dostepnych archiwow dla klienta
                    for (unsigned int i = 0; i < files.size(); i++)
                    {
                        string fileName=files[i];
                        if (fileName!="." && fileName!="..")
                        {
                            sizeNameFiles+=fileName.size()+1;
                        }
                    }
                    //zapisanie zawartosci katalogu z archiwami do bufora dla klienta
                    char tableToSend [sizeNameFiles];
                    int iterator =0;
                    for ( unsigned int i=0; i<files.size() ; i++ )
                    {
                        string fileName=files[i];
                        if (fileName!="." && fileName!="..")
                        {
                            for (unsigned int j=0; j<fileName.size(); j++)
                            {
                                tableToSend[iterator]=fileName[j];
                                iterator++;
                            }
                            tableToSend[iterator]='\n';
                            iterator++;
                        }
                    }

                    cout<<"Lista archiwow w katalogu ./archives :" <<endl<<tableToSend<<endl;

                    int numberOfSendingChars =0;

                    char tmpBufforForData;
                    do // wysyłanie strumienia
                    {
                        tmpBufforForData=tableToSend[numberOfSendingChars];
                        int readed = write(client, &tmpBufforForData, 1);
                        numberOfSendingChars+=readed;
                    }
                    while(numberOfSendingChars!= sizeNameFiles);

                    break;
                }
                }
            }

            cout<<"Obsłużono klienta o adresie IP : " << inet_ntoa((struct in_addr) newClient.sin_addr) <<endl;
            cout<<endl;
            exit(0);
        }
        close(client);
    }
    return 0;
}
