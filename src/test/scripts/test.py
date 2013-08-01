from ij import IJ
from ij.gui import Roi
#from pta import PTA
from pta.track import DetectParticle
from pta.data import PtaParam
from java.util import ArrayList
from java.lang import System
property = System.getProperty("java.library.path")
print property
#System.loadLibrary("fit2DGauss")
#System.load("/Users/miura/Dropbox/codes/ws1/projectPTAj/lib/forMacOSX/libfit2DGauss.jnilib")

#ptaobj = PTA()
#imp = IJ.getImage()
imp = IJ.openImage("/Users/miura/Dropbox/20130700_Osaka/data/transferrin-movement/tconc1_3.tif")
#PTA.setDetectionState(True)
scanAreaRoi = Roi(0,0,imp.getWidth(),imp.getHeight())
ptap = PtaParam.Builder(12,12, False).build()
ptap.setDo2dGaussfit(True)
#pdata = None
dp = DetectParticle(ptap,imp,scanAreaRoi)
dp.setPtap(ptap)
dp.setScanRoi(scanAreaRoi)
dp.setStackRange(1, imp.getStackSize())

#dplist = new ArrayList<FPoint>()
#dplist = ArrayList()
#dp = DetectParticle(ptap, imp, scanAreaRoi, dplist, True)
dp.start()
dp.join()

#table = dp.getShowPdata()
#print table.toString()
#table.show()