#include <stdio.h>
#include <windows.h>
#include <unistd.h>
#include <string.h>

/**
 * 字符串替换
 * @param str
 * @param src
 * @param dst
 * @return
 */
char *replace(const char *str, const char *src, const char *dst) {
    const char *pos = str;
    int count = 0;
    while ((pos = strstr(pos, src))) {
        count++;
        pos += strlen(src);
    }

    size_t result_len = strlen(str) + (strlen(dst) - strlen(src)) * count + 1;
    char *result = (char *) malloc(result_len);
    memset(result, 0, result_len);

    const char *left = str;
    const char *right = NULL;

    while ((right = strstr(left, src))) {
        strncat(result, left, right - left);
        strcat(result, dst);
        right += strlen(src);
        left = right;
    }
    strcat(result, left);
    return result;
}

int main(int argc, char *argv[]) {
    ShowWindow(FindWindow("ConsoleWindowClass", argv[0]), 0); //查找窗口隐藏自身 ..
    //内存使用率
    MEMORYSTATUSEX statex;
    statex.dwLength = sizeof(statex);
    GlobalMemoryStatusEx(&statex);
    printf("内存使用率：%ld%%\n", statex.dwMemoryLoad);
    //物理内存
    printf("总内存大小：%llumb\n", statex.ullTotalPhys / 1024 / 1024);

    char buf[80];
    getcwd(buf, sizeof(buf));
    printf("%s\n", buf);

    char target1[] = "cmake-build-debug";
    char target2[] = "cmake-build-release";
    char *_buf = replace(buf, "\\", "/");
    char *c;
    if (strstr(_buf, target1)) {
        c = replace(_buf, target1, "");
    } else if (strstr(_buf, target2)) {
        c = replace(_buf, target2, "");
    } else {
        c = (char *) malloc(strlen(_buf) + 1);
        strcpy(c, _buf);
    }
    free(_buf);

    // 可用内存大小
    double memory = (double) statex.ullTotalPhys / 1024 / 1024 / 1024;
    int m = (int) memory;

    char result[1000];
    int r = 0;
    if (!m) {
        sprintf(result, "%s/jre/bin/java.exe -Xmx%dg -Xms%dm -jar %s/M3U8.jar", c, m, (m) * 1024 / 4, c);
        printf("%s\n", result);
        //如果内存不足1g
        r = !system(result);
    } else {
        for (int i = 0; i < m && !r; i++) {
            sprintf(result, "%s/jre/bin/java.exe -Xmx%dg -Xms%dm -jar %s/M3U8.jar", c, m - i, (m - i) * 1024 / 4, c);
            printf("%s\n", result);
            r = !system(result);
        }
    }

    if (!r) {
        sprintf(result, "%s/jre/bin/java.exe -jar %s/M3U8.jar", c, c);
        r = !system(result);
        if (!r) {
            ShowWindow(FindWindow("ConsoleWindowClass", argv[0]), 1); //查找窗口显示自身 ..
            system("pause");
        }
    }
    free(c);
    return 0;
}
