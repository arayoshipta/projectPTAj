#Makefile for jni2DGaussfit.jnilib

#Macro
CC = gcc
OBJS = fit2DGauss.c

#Description

libfit2DGauss.jnilib:$(OBJS)
	$(CC) -dynamiclib -lm -lminpack -m64 -O3 -o $@ $(OBJS) -I /System/Library/Frameworks/JavaVM.framework/Headers/ -Wall
