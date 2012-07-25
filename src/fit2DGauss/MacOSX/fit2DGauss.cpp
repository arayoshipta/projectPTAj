// fit2DGauss.cpp : DLL アプリケーション用にエクスポートされる関数を定義します。
//

#include "cminpack.h"
#include <math.h>
#include "pta_PTA.h"


/*
* fit2DGauss.cpp
*
* Created on: 2012/05/22
* Author: araiyoshiyuki
*/


#define PI 3.14159

int iteration;
int iterationMax;

double *fdata;
int size;
double f2dGauss(int, const double*,int);
int fcn(void*, int, int, const double*, double*, int);

typedef struct  {
    int m;
    double *y;
} fcndata_t;

JNIEXPORT jdoubleArray JNICALL Java_pta_PTA_fit2DGauss(JNIEnv *env,
jclass jcla, jdoubleArray jdobFionaData,
jdoubleArray jdobParam, jint s, jintArray jinfoParam) {

jsize paramLength = env->GetArrayLength(jdobParam); // to obtain a length of jdobParam
jsize fionaDataLength = env->GetArrayLength(jdobFionaData); //

jdouble *jfdata = env->GetDoubleArrayElements(jdobFionaData,NULL); // to access jdobData by jdata
jdouble *jparam = env->GetDoubleArrayElements(jdobParam,NULL);
jint *jinfo = env->GetIntArrayElements(jinfoParam,NULL);

jdoubleArray retParam = env->NewDoubleArray(paramLength); //to return param

fdata = new double[static_cast<int>(fionaDataLength)];
double *param = new double[static_cast<int>(paramLength)];

iteration = 0; //Iteration value
iterationMax = jinfo[1];

for(int i=0;i<fionaDataLength;i++)
fdata[i] = static_cast<double>(jfdata[i]);
for(int i=0;i<paramLength;i++)
param[i] = static_cast<double>(jparam[i]);

size = static_cast<int>(s);
//
int m, n, info=0, lwa, iwa[6];
//
n=paramLength;

m=size*size;
double *fvec = new double[m];

lwa = n*m+5*n+m;
double *wa = new double[lwa];

fcndata_t data;
data.m = m;
data.y = fdata;

double tol = sqrt(__cminpack_func__(dpmpar)(1));

info = __cminpack_func__(lmdif1)(fcn, &data, m, n, param, fvec, tol, iwa, wa, lwa);

if(iteration<jinfo[1])
	jinfo[0] = info;
else
	jinfo[0] = 10;

jinfo[1] = iteration;

env->SetDoubleArrayRegion(retParam,0,paramLength,static_cast<jdouble*>(param));
env->ReleaseDoubleArrayElements(jdobFionaData,jfdata,0); // release
env->ReleaseDoubleArrayElements(jdobParam,jparam,0); // release
env->ReleaseIntArrayElements(jinfoParam,jinfo,0);

delete fdata;
delete param;
delete wa;
delete fvec;

iteration = 0;
return retParam;
}

double f2dGauss(int xin, const double *x,int size)
/**
* 2-dimensional gaussian function
* In general, 2D gaussian distribution is described as,
* f(xin,yin) =x0+x1*exp(0.5*((xin-x2)/x3)^2+((yin-x4)/x5))^2)
* However, only x variable is acceptable for this lm methods.
* The equation f(x,y) should be traslated to the f(x) type.
* f(x) = sum(i)[x0..x5](x0*exp(-(i-x4)^2/(2*x2^2))*exp(-(xin-(x3+i*size))^2/(2*x1^2))+x5
* x0:ampltude
* x1:sigma x
* x2:sigma y
* x3:mu x
* x4:mu y
* x5:offset
* size:length of square of side
*/
{
double expx, expy, argx, argy, sqr;
double y;
double xindx, yindx;


xindx = xin%size;
yindx = xin/size;

argx = xindx-x[3];
argy = yindx-x[4];
expx = exp(-argx*argx/(2*x[1]*x[1]));
expy = exp(-argy*argy/(2*x[2]*x[2]));
sqr = sqrt(x[1]*x[2]);
y = x[0]*expx*expy/(2*PI*sqr);
y += x[5];
return y;
}

int fcn(void *p, int m, int n, const double *x, double *fvec, int iflag)
{
int i;
const double *y = static_cast<fcndata_t*>(p)->y;

for(i=0;i<size*size;i++) {
fvec[i] = y[i] - f2dGauss(i,x,size);
}
iteration++;
if (iteration>iterationMax) iflag = -1; // if iteration is over 1000, lmdif is forced to stop
return 0;
}