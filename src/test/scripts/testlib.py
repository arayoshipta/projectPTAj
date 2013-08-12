from java.lang import System
from java.util import StringTokenizer 
#String
property = System.getProperty("java.library.path")
#StringTokenizer
parser = StringTokenizer(property, ";")
print property
#while (parser.hasMoreTokens()) {
#    System.err.println(parser.nextToken());
#    }