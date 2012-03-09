/*
 * fit2DGauss.c
 *
 *  Created on: 2009/05/29
 *      Author: araiyoshiyuki
 */

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "PTA_.h"
#include <cminpack.h>
#define PI 3.14159

int iteration;
int iterationMax;

double *fdata;
int size;
double f2dGauss(double xin, const double *x,int size);
int fcn(void *p, int m, int n, const double *x, double *fvec, int iflag);


JNIEXPORT jdoubleArray JNICALL Java_PTA_1_fit2DGauss(JNIEnv *env,
		jobject jObje, jdoubleArray jdobFionaData,
		jdoubleArray jdobParam, jint s, jintArray jinfoParam) {

	jsize paramLength = (*env)->GetArrayLength(env,jdobParam);	// to obtain a length of jdobParam
	jsize fionaDataLength = (*env)->GetArrayLength(env,jdobFionaData); //

	jdouble *jfdata = (*env)->GetDoubleArrayElements(env,jdobFionaData,NULL); // to access jdobData by jdata
	jdouble *jparam = (*env)->GetDoubleArrayElements(env,jdobParam,NULL);
	jint *jinfo = (*env)->GetIntArrayElements(env,jinfoParam,NULL);

	jdoubleArray retParam = (*env)->NewDoubleArray(env,paramLength); //to return param

	fdata = (double*)malloc((size_t)sizeof(double)*(int)fionaDataLength);
	double *param = (double*)malloc((size_t)sizeof(double)*(int)paramLength);

	iteration = 0; //Iteration value
	iterationMax = jinfo[1];

	int i=0;
	for(i=0;i<fionaDataLength;i++)
		fdata[i] = jfdata[i];
	for(i=0;i<paramLength;i++)
		param[i] = jparam[i];

	size = (int)s;

	int m, n, info, lwa, iwa[6];
	double tol;
	double *fvec;
	double *wa;

	wa = malloc(sizeof(double)*(6*size*size+5*6+size*size));
	fvec = malloc(sizeof(double)*size*size);
	m=size*size;
	n=6;
	lwa = 6*size*size+5*6+size*size;
	tol = sqrt(dpmpar(1));

	info = lmdif1(fcn, 0, m, n, param, fvec, tol, iwa, wa, lwa);
	if(iteration<jinfo[1])
		jinfo[0] = info;
	else
		jinfo[0] = 10;
	jinfo[1] = iteration;

	(*env)->SetDoubleArrayRegion(env,retParam,0,paramLength,param);
	(*env)->ReleaseDoubleArrayElements(env,jdobFionaData,jfdata,0); // release
	(*env)->ReleaseDoubleArrayElements(env,jdobParam,jparam,0); // release
	(*env)->ReleaseIntArrayElements(env,jinfoParam,jinfo,0);

	free(fdata);
	free(param);
	free(wa);
	free(fvec);

	iteration = 0;
	return retParam;
}

double f2dGauss(double xin, const double *x,int size)
/**
 * 2-dimensional gaussian function
 * In general, 2D gaussian distribution is described as,
 * f(xin,yin) =x0+x1*exp(0.5*((xin-x2)/x3)^2+((yin-x4)/x5))^2)
 * However, only x variable is acceptable for this lm methods.
 * The equation f(x,y) should be traslated to the f(x) type.
 * f(x) = sum(i)[x0..x5](x0*exp(-(i-x4)^2/(2*x2^2))*exp(-(xin-(x3+i*size))^2/(2*x1^2))+x5
 *	x0:ampltude
 *	x1:sigma x
 *	x2:sigma y
 *	x3:mu x
 *	x4:mu y
 *	x5:offset
 *	size:length of square of side
 */
{
	double expx, expy, argx, argy, sqr;
	int i;
	double y=0;
	for(i=0;i<size;i++) {
		argx = xin-(x[3]+i*size);
		argy = i-x[4];
		expx = exp(-argx*argx/(2*x[1]*x[1]));
		expy = exp(-argy*argy/(2*x[2]*x[2]));
		sqr = sqrt(x[1]*x[2]);
		y += x[0]*expx*expy/(2*PI*sqr);
	}
	y += x[5];
	return y;
}

int fcn(void *p, int m, int n, const double *x, double *fvec, int iflag)
{
	int i;
	for(i=0;i<size*size;i++) {
		fvec[i] = fdata[i] - f2dGauss((double)i,x,size);
	}
	iteration++;
	if (iteration>iterationMax) iflag = -1; // if iteration is over 1000, lmdif is forced to stop
	return 0;
}
