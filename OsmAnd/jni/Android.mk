LOCAL_PATH := $(call my-dir)
ROOT_PATH := $(LOCAL_PATH)/../../..
$(info OsmAnd root: $(ROOT_PATH))

OSMAND_MAKEFILES := \
    $(all-subdir-makefiles) \
    $(call all-makefiles-under,$(ROOT_PATH)/core-legacy/targets/android)
#$(info OsmAnd makefiles: $(OSMAND_MAKEFILES))

# By default, include makefiles only once
include $(OSMAND_MAKEFILES)
