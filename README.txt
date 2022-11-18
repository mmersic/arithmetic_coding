This is an implementation of Arithmetic Coding that I translated from the C version given in
Bell, T. C., Cleary, J. G., & Witten, I. H. (1990). 
    Text compression (pp. 132-139). Prentice Hall.

ArithmeticCoder is threadsafe.
AdaptiveModel is not threadsafe and any attempt to call encode and/or decode more than once will throw
an IllegalStateException.

To build tests, as of Java 19, requires  
--add-modules jdk.incubator.concurrent

To run tests, as of Java 19 requires preview features enabled.

Yes, this is where I get to play with new features.