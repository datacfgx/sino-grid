// hello freestanding arm64-android: so syscalls, sem libc, sem TLS, sem seccomp
typedef long i64;

static i64 sc3(i64 n, i64 a, i64 b, i64 c) {
    register i64 x8 __asm__("x8") = n;
    register i64 x0 __asm__("x0") = a;
    register i64 x1 __asm__("x1") = b;
    register i64 x2 __asm__("x2") = c;
    __asm__ volatile("svc #0" : "+r"(x0) : "r"(x1), "r"(x2), "r"(x8) : "memory");
    return x0;
}

static char OUT[4096];
static char TMP[512];
static int pos = 0;

static void put(const char* s) { while (*s && pos < 4000) OUT[pos++] = *s++; }
static void putdec(i64 v) {
    char t[24]; int i = 0;
    if (v < 0) { put("-"); v = -v; }
    if (!v) { put("0"); return; }
    while (v) { t[i++] = (char)('0' + (v % 10)); v /= 10; }
    while (i) OUT[pos++] = t[--i];
}

void _start(void) {
    put("EXEC_OK uid="); putdec(sc3(174, 0, 0, 0)); put("\n");
    long fd = sc3(56, -100, (i64)"/proc/self/attr/current", 0);
    if (fd >= 0) {
        long n = sc3(63, fd, (i64)TMP, 400);
        if (n > 0) for (long i = 0; i < n && TMP[i] != 10; i++) OUT[pos++] = TMP[i];
        OUT[pos++] = 10;
        sc3(57, fd, 0, 0);
    }
    long s = sc3(198, 38, 5, 3);
    if (s >= 0) { put("AEAD: ABERTA\n"); sc3(57, s, 0, 0); }
    else { put("AEAD: errno="); putdec(-s); put("\n"); }
    sc3(64, 1, (i64)OUT, pos);
    sc3(94, 0, 0, 0);
}
