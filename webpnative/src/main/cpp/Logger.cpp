//
// Created by Wanghong Lin on 2019/2/20.
//

#include "Logger.h"

Logger &Logger::operator<<(char c) {
    os_ << c;
    flush();
    return *this;
}

Logger &Logger::operator<<(int i) {
    os_ << i;
    flush();
    return *this;
}

Logger &Logger::operator<<(unsigned ui) {
    return operator<<(static_cast<int>(ui));
}

Logger &Logger::operator<<(double f) {
    os_ << f;
    flush();
    return *this;
}

Logger &Logger::operator<<(const char *s) {
    os_ << s;
    flush();
    return *this;
}

Logger Logger::_d = Logger(ANDROID_LOG_DEBUG);
Logger Logger::_i = Logger(ANDROID_LOG_INFO);
Logger Logger::_v = Logger(ANDROID_LOG_VERBOSE);
Logger Logger::_w = Logger(ANDROID_LOG_WARN);
Logger Logger::_e = Logger(ANDROID_LOG_ERROR);

Logger& Logger::debug() {
    return _d;
}

Logger& Logger::info() {
    return _i;
}

Logger& Logger::error() {
    return _v;
}

Logger& Logger::verbose() {
    return _w;
}

Logger& Logger::warn() {
    return _e;
}

void Logger::flush() {
    __android_log_write(priority_, tag_, os_.str().c_str());
    os_.str("");
}
