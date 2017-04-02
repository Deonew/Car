////
//// Created by deonew on 17-4-1.
////
//
////
//// Created by deonew on 17-3-31.
////
//#include <sys/types.h>
//#include <sys/socket.h>
//#include <string.h>
//#include <linux/in.h>
//#include <stdlib.h>
//#include <stdio.h>
//
//#include "com_example_deonew_car_VideoActivity2.h"
//
//JNIEXPORT void JNICALL Java_com_example_deonew_car_VideoActivity2_sendH264Func
//        (JNIEnv * env, jobject obj, jstring js) {
////    return ;
////    int cfd;
////    struct sockaddr_in s_add, c_add;
////    unsigned short portnum = 0x8888;
////    char buffer[1024] = {0};
////    //send
////    char* sendFilePath = "/storage/emulated/0/carTemp.h264";
////    FILE *sendf = fopen(sendFilePath, "rb");
////    if(!sendf){
////        return;
////    }
////
//////    return;
////
////    char *receiveFile = "/home/deonew/G/c_tcp_h264/recvfile.264";
////
////
////
////    if ((cfd = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
////        printf("%s\n", "socket failed");
////    }
////    bzero(&s_add, sizeof(struct sockaddr_in));
////    s_add.sin_family = AF_INET;
////    s_add.sin_addr.s_addr = inet_addr("10.105.36.224");
////    s_add.sin_port = htons(portnum);
////    if (-1 == connect(cfd, (struct sockaddr *) (&s_add), sizeof(struct sockaddr))) {
////        printf("connect failed");
////        return;
////    }
////    FILE *recvf = fopen(receiveFile, "wb");
////    if(!recvf){
////        return;
////    }
////    char bu[10] = {0};
////    int nCnt;
////    while ((nCnt = recv(cfd, bu, 10, 0)) > 0) {
////        printf("%d\n", nCnt);
////        fwrite(bu, sizeof(char), nCnt, recvf);
//////    }
////    char sendBuffer[10] = {0};
////    int sendCnt;
////    while((sendCnt = fread(buffer,sizeof(char),10,sendf))>0){
////        send(cfd,sendBuffer,sendCnt,0);
////    }
////    close(cfd);
//
//
//    //client send file v2
//    //socket handle
//    int cfd;
//    //struct to save c and s ' info
//    struct sockaddr_in s_add, c_add;
//    unsigned short portnum=0x8888;
////    char* cliSendFilePath = "/storage/emulated/0/carTemp.h264";
//    const char* cliSendFilePath;
//    //get path from jstring
//    //jstring to char*
////    const char *str =
//    cliSendFilePath = env-> GetStringUTFChars(js,false);
//
//    //end get path
//
//
//    FILE* cliSendfp = fopen(cliSendFilePath,"rb");
//
//    //socket
//    if ((cfd = socket(AF_INET,SOCK_STREAM,0)) == -1)
//    {
//        return;
//    }
//
//    //server info
//    bzero(&s_add,sizeof(struct sockaddr_in));
//    s_add.sin_family=AF_INET;
//    s_add.sin_addr.s_addr= inet_addr("10.105.36.224");
//    s_add.sin_port=htons(portnum);
//
//    //connect
//    if(-1 == connect(cfd,(struct sockaddr *)(&s_add), sizeof(struct sockaddr)))
//    {
//        return;
//    }
//
//    //send test buffer
////    char bufferTest[10] = {0};
////    send(cfd,bufferTest,sizeof(bufferTest),0);
//
//    //send a file
//    char cliSendBuffer[10] = {0};
//    int cliSendCnt;
//    while((cliSendCnt = fread(cliSendBuffer,sizeof(char),10,cliSendfp))>0){
//        send(cfd,cliSendBuffer,cliSendCnt,0);
//    }
//
//
//    fclose(cliSendfp);
//
//    close(cfd);
//    return;
//}
