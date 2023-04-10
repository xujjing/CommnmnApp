//
// Created by admin on 2020/9/17.
//
#ifndef COLLECTLOGUTIL_NATIVE_LOGFILE_H
#define COLLECTLOGUTIL_NATIVE_LOGFILE_H

#define NATIVE_LOGFILE_VERBOSE  2
#define NATIVE_LOGFILE_DEBUG    3
#define NATIVE_LOGFILE_INFO     4
#define NATIVE_LOGFILE_WARN     5
#define NATIVE_LOGFILE_ERROR    6
#define NATIVE_LOGFILE_ASSERT   7

#define NATIVE_LOG_BUF_SIZE	4096

extern "C" int native_log_file(int priority, const char* tag, const char* message);

#endif //COLLECTLOGUTIL_NATIVE_LOGFILE_H
