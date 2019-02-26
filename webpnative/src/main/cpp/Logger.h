//
// Created by Wanghong Lin on 2019/2/20.
//

#ifndef ANDROIDWEBP_LOGGER_H
#define ANDROIDWEBP_LOGGER_H

#include <sstream>
#include <android/log.h>

class Logger {

public:
    Logger(android_LogPriority priority = ANDROID_LOG_DEBUG, const char *tag = nullptr) : tag_(tag), priority_(priority) {
        if (tag == nullptr) {
            tag_ = __FUNCTION__;
        }
    }

    Logger& tag(const char* TAG) {
        tag_ = TAG;
        return *this;
    }

    void flush();

    static Logger& debug();
    static Logger& info();
    static Logger& error();
    static Logger& verbose();
    static Logger& warn();

    Logger& operator<<(char c);
    Logger& operator<<(int i);
    Logger& operator<<(unsigned ui);
    Logger& operator<<(double f);
    Logger& operator<<(const char* s);
private:
    const char* tag_;
    std::stringstream os_;
    android_LogPriority priority_;

    static Logger _d;
    static Logger _i;
    static Logger _e;
    static Logger _v;
    static Logger _w;
};


#endif //ANDROIDWEBP_LOGGER_H
