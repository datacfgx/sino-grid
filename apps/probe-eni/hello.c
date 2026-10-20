#include <stdio.h>
#include <unistd.h>
#include <sys/socket.h>
#include <errno.h>
#include <string.h>

int main() {
    char dom[128] = "?";
    FILE* f = fopen("/proc/self/attr/current", "r");
    if (f) { if (fgets(dom, sizeof(dom), f)) { char* nl = strchr(dom, '\n'); if (nl) *nl = 0; } fclose(f); }
    printf("EXEC_OK uid=%d\n", getuid());
    printf("domain=%s\n", dom);
    int s = socket(38, SOCK_SEQPACKET, 3);
    if (s >= 0) { printf("AF_ALG AEAD: ABERTA\n"); close(s); }
    else printf("AF_ALG AEAD: errno=%d (%s)\n", errno, strerror(errno));
    return 0;
}
