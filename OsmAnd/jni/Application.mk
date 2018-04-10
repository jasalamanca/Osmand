APP_STL := gnustl_shared
APP_CPPFLAGS := -std=c++11 -fexceptions -frtti
APP_SHORT_COMMANDS := true

# Specify least supported Android platform version
APP_PLATFORM := android-16

APP_ABI := x86 armeabi-v7a
    
ifndef OSMAND_DEBUG_NATIVE
    # Force release compilation in release optimizations, even if application is debuggable by manifest
    APP_OPTIM := release
endif
