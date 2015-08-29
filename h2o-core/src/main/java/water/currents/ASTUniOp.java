package water.currents;

import hex.DMatrix;
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.util.FastMath;
import water.*;
import water.fvec.*;
import water.util.MathUtils;

import java.util.Arrays;
import java.util.Random;

/**
 * Subclasses auto-widen between scalars and Frames, and have exactly one argument
 */
abstract class ASTUniOp extends ASTPrim {
  @Override int nargs() { return 1+1; }
  @Override Val apply( Env env, Env.StackHelp stk, AST asts[] ) {
    Val val = stk.track(asts[1].exec(env));
    switch( val.type() ) {
    case Val.NUM: return new ValNum(op(val.getNum()));
    case Val.FRM: 
      Frame fr = val.getFrame();
      return new ValFrame(new MRTask() {
          @Override public void map( Chunk cs[], NewChunk ncs[] ) {
            for( int col=0; col<cs.length; col++ ) {
              Chunk c = cs[col];
              NewChunk nc = ncs[col];
              for( int i=0; i<c._len; i++ )
                nc.addNum(op(c.atd(i)));
            }
          }
        }.doAll(fr.numCols(),fr).outputFrame());
    case Val.STR: throw H2O.unimpl();
    default: throw H2O.fail();
    }
  }
  abstract double op( double d );
}

class ASTCeiling extends ASTUniOp{String str() { return "ceiling";}double op(double d) { return Math.ceil (d); } }
class ASTFloor extends ASTUniOp { String str() { return "floor"; } double op(double d) { return Math.floor(d); } }
class ASTNot   extends ASTUniOp { String str() { return "!!"   ; } double op(double d) { return d==0?1:0; } }
class ASTTrunc extends ASTUniOp { String str() { return "trunc"; } double op(double d) { return d>=0?Math.floor(d):Math.ceil(d);}}
class ASTCos  extends ASTUniOp { String str(){ return "cos";  } double op(double d) { return Math.cos(d);}}
class ASTSin  extends ASTUniOp { String str(){ return "sin";  } double op(double d) { return Math.sin(d);}}
class ASTTan  extends ASTUniOp { String str(){ return "tan";  } double op(double d) { return Math.tan(d);}}
class ASTACos extends ASTUniOp { String str(){ return "acos"; } double op(double d) { return Math.acos(d);}}
class ASTASin extends ASTUniOp { String str(){ return "asin"; } double op(double d) { return Math.asin(d);}}
class ASTATan extends ASTUniOp { String str(){ return "atan"; } double op(double d) { return Math.atan(d);}}
class ASTCosh extends ASTUniOp { String str(){ return "cosh"; } double op(double d) { return Math.cosh(d);}}
class ASTSinh extends ASTUniOp { String str(){ return "sinh"; } double op(double d) { return Math.sinh(d);}}
class ASTTanh extends ASTUniOp { String str(){ return "tanh"; } double op(double d) { return Math.tanh(d);}}
class ASTACosh extends ASTUniOp { String str(){ return "acosh"; } double op(double d) { return FastMath.acosh(d);}}
class ASTASinh extends ASTUniOp { String str(){ return "asinh"; } double op(double d) { return FastMath.asinh(d);}}
class ASTATanh extends ASTUniOp { String str(){ return "atanh"; } double op(double d) { return FastMath.atanh(d);}}
class ASTCosPi extends ASTUniOp { String str(){ return "cospi"; } double op(double d) { return Math.cos(Math.PI*d);}}
class ASTSinPi extends ASTUniOp { String str(){ return "sinpi"; } double op(double d) { return Math.sin(Math.PI*d);}}
class ASTTanPi extends ASTUniOp { String str(){ return "tanpi"; } double op(double d) { return Math.tan(Math.PI*d);}}
class ASTAbs  extends ASTUniOp { String str(){ return "abs";  } double op(double d) { return Math.abs(d);}}
class ASTSgn  extends ASTUniOp { String str(){ return "sign" ; } double op(double d) { return Math.signum(d);}}
class ASTSqrt extends ASTUniOp { String str(){ return "sqrt"; } double op(double d) { return Math.sqrt(d);}}
class ASTTrun extends ASTUniOp { String str(){ return "trunc"; } double op(double d) { return d>=0?Math.floor(d):Math.ceil(d);}}
class ASTCeil extends ASTUniOp { String str(){ return "ceiling"; } double op(double d) { return Math.ceil(d);}}
class ASTFlr  extends ASTUniOp { String str(){ return "floor";} double op(double d) { return Math.floor(d);}}
class ASTLog  extends ASTUniOp { String str(){ return "log";  } double op(double d) { return Math.log(d);}}
class ASTLog10  extends ASTUniOp { String str(){ return "log10";  } double op(double d) { return Math.log10(d);}}
class ASTLog2  extends ASTUniOp { String str(){ return "log2";  } double op(double d) { return Math.log(d)/Math.log(2);}}
class ASTLog1p  extends ASTUniOp { String str(){ return "log1p";  } double op(double d) { return Math.log1p(d);}}
class ASTExp  extends ASTUniOp { String str(){ return "exp";  } double op(double d) { return Math.exp(d);}}
class ASTExpm1  extends ASTUniOp { String str(){ return "expm1";  } double op(double d) { return Math.expm1(d);}}
class ASTGamma  extends ASTUniOp { String str(){ return "gamma";  } double op(double d) {  return Gamma.gamma(d);}}
class ASTLGamma extends ASTUniOp { String str(){ return "lgamma"; } double op(double d) { return Gamma.logGamma(d);}}
class ASTDiGamma  extends ASTUniOp { String str(){ return "digamma";  } double op(double d) {  return Gamma.digamma(d);}}
class ASTTriGamma  extends ASTUniOp { String str(){ return "trigamma";  } double op(double d) {  return Gamma.trigamma(d);}}

// Split out in it's own function, instead of Yet Another UniOp, because it
// needs a "is.NA" check instead of just using the Double.isNaN hack... because
// it works on UUID and String columns.
class ASTIsNA  extends ASTPrim {
  @Override String str() { return "is.na"; } 
  @Override int nargs() { return 1+1; }
  @Override Val apply( Env env, Env.StackHelp stk, AST asts[] ) {
    Val val = stk.track(asts[1].exec(env));
    switch( val.type() ) {
    case Val.NUM: return new ValNum(op(val.getNum()));
    case Val.FRM: 
      Frame fr = val.getFrame();
      return new ValFrame(new MRTask() {
          @Override public void map( Chunk cs[], NewChunk ncs[] ) {
            for( int col=0; col<cs.length; col++ ) {
              Chunk c = cs[col];
              NewChunk nc = ncs[col];
              for( int i=0; i<c._len; i++ )
                nc.addNum(c.isNA(i) ? 1 : 0);
            }
          }
        }.doAll(fr.numCols(),fr).outputFrame());
    case Val.STR: return new ValNum(val.getStr()==null ? 1 : 0);
    default: throw H2O.fail();
    }
  }
  double op(double d) { return Double.isNaN(d)?1:0; } 
}

class ASTRunif extends ASTPrim {
  @Override int nargs() { return 1+2; } // (h2o.runif frame seed)
  @Override String str() { return "h2o.runif"; }
  @Override Val apply( Env env, Env.StackHelp stk, AST asts[] ) {
    Frame fr  = stk.track(asts[1].exec(env)).getFrame();
    long seed = (long)asts[2].exec(env).getNum();
    if( seed == -1 ) seed = new Random().nextLong();
    return new ValFrame(new Frame(new String[]{"rnd"}, new Vec[]{fr.anyVec().makeRand(seed)}));
  }
}

class ASTNrow extends ASTPrim {
  @Override int nargs() { return 1+1; }
  @Override String str() { return "nrow"; }
  @Override Val apply(Env env, Env.StackHelp stk, AST asts[] ) {
    Frame fr = stk.track(asts[1].exec(env)).getFrame();
    return new ValNum(fr.numRows());
  }
}


class ASTNLevels extends ASTPrim {
  @Override int nargs() { return 1+1; } // (nlevels x)
  @Override String str() { return "nlevels"; }
  @Override ValNum apply(Env env, Env.StackHelp stk, AST asts[] ) {
    int nlevels;
    Frame fr = stk.track(asts[1].exec(env)).getFrame();
    if (fr.numCols() == 1) {
      Vec v = fr.anyVec();
      nlevels = v.isEnum()?v.domain().length:0;
      return new ValNum(nlevels);
    } else throw new IllegalArgumentException("nlevels applies to a single column. Got: " + fr.numCols());
  }
}

class ASTLevels extends ASTPrim {
  @Override int nargs() { return 1+1; } // (levels x)
  @Override String str() { return "levels"; }
  @Override ValFrame apply(Env env, Env.StackHelp stk, AST asts[] ) {
    Frame f = stk.track(asts[1].exec(env)).getFrame();
    Futures fs = new Futures();
    Key[] keys = Vec.VectorGroup.VG_LEN1.addVecs(f.numCols());
    Vec[] vecs = new Vec[keys.length];

    // compute the longest vec... that's the one with the most domain levels
    int max=0;
    for(int i=0;i<f.numCols();++i )
      if( f.vec(i).isEnum() )
        if( max < f.vec(i).domain().length ) max = f.vec(i).domain().length;

    for( int i=0;i<f.numCols();++i ) {
      AppendableVec v = new AppendableVec(keys[i]);
      NewChunk nc = new NewChunk(v,0);
      String[] dom = f.vec(i).domain();
      int numToPad = dom==null?max:max-dom.length;
      if( dom != null )
        for(int j=0;j<dom.length;++j) nc.addNum(j);
      for(int j=0;j<numToPad;++j)     nc.addNA();
      nc.close(0,fs);
      vecs[i] = v.close(fs);
      vecs[i].setDomain(dom);
    }
    fs.blockForPending();
    Frame fr2 = new Frame(vecs);
    return new ValFrame(fr2);
  }
}

class ASTSetLevel extends ASTPrim {
  @Override int nargs() { return 1+2; } // (setLevel x level)
  @Override String str() { return "setLevel"; }
  @Override ValFrame apply(Env env, Env.StackHelp stk, AST asts[] ) {
    Frame fr = stk.track(asts[1].exec(env)).getFrame();
    if (fr.numCols() != 1) throw new IllegalArgumentException("`setLevel` works on a single column at a time.");
    String[] doms = fr.anyVec().domain().clone();
    if( doms == null )
      throw new IllegalArgumentException("Cannot set the level on a non-factor column!");

    String lvl = asts[2].exec(env).getStr();

    final int idx = Arrays.asList(doms).indexOf(lvl);
    if (idx == -1) throw new IllegalArgumentException("Did not find level `" + lvl + "` in the column.");


    // COW semantics
    Frame fr2 = new MRTask() {
      @Override public void map(Chunk c, NewChunk nc) {
        for (int i=0;i<c._len;++i)
          nc.addNum(idx);
      }
    }.doAll(1, fr.anyVec()).outputFrame(null, fr.names(), fr.domains());
    return new ValFrame(fr2);
  }
}


class ASTSetDomain extends ASTPrim {
  @Override int nargs() { return 1+2;} // (setDomain x [list of strings])
  @Override String str() { return "setDomain"; }
  @Override ValFrame apply(Env env, Env.StackHelp stk, AST asts[] ) {
    Frame f = stk.track(asts[1].exec(env)).getFrame();
    String[] _domains = ((ASTStrList)asts[2])._strs;
    if( f.numCols()!=1 ) throw new IllegalArgumentException("Must be a single column. Got: " + f.numCols() + " columns.");
    Vec v = f.anyVec();
    if( !v.isEnum() ) throw new IllegalArgumentException("Vector must be a factor column. Got: "+v.get_type_str());
    if( _domains!=null && _domains.length != v.domain().length) {
      // in this case we want to recollect the domain and check that number of levels matches _domains
      Vec.CollectDomainFast t = new Vec.CollectDomainFast((int)v.max());
      t.doAll(v);
      final long[] dom = t.domain();
      if( dom.length != _domains.length)
        throw new IllegalArgumentException("Number of replacement factors must equal current number of levels. Current number of levels: " + dom.length + " != " + _domains.length);
      new MRTask() {
        @Override public void map(Chunk c) {
          for(int i=0;i<c._len;++i) {
            if( !c.isNA(i) ) {
              long num = Arrays.binarySearch(dom, c.at8(i));
              if( num < 0 ) throw new IllegalArgumentException("Could not find the enum value!");
              c.set(i,num);
            }
          }
        }
      }.doAll(v);
    }
    v.setDomain(_domains);
    DKV.put(v);
    return new ValFrame(f);
  }
}

class ASTTranspose extends ASTPrim {
  @Override int nargs() { return 1+1; } // (t X)
  @Override String str() { return "t"; }
  @Override ValFrame apply(Env env, Env.StackHelp stk, AST asts[]) {
    Frame f = stk.track(asts[1].exec(env)).getFrame();
    return new ValFrame(DMatrix.transpose(f));
  }
}

class ASTMMult extends ASTPrim {
  @Override int nargs() { return 1+2; } // (t X1 X2)
  @Override String str() { return "x"; }

  @Override ValFrame apply(Env env, Env.StackHelp stk, AST asts[]) {
    Frame X1 = stk.track(asts[1].exec(env)).getFrame();
    Frame X2 = stk.track(asts[2].exec(env)).getFrame();
    return new ValFrame(DMatrix.mmul(X1,X2));
  }
}


class ASTMatch extends ASTPrim {
  @Override int nargs() { return 1+4; } // (match fr table nomatch incomps)
  @Override String str() { return "match"; }
  @Override ValFrame apply(Env env, Env.StackHelp stk, AST asts[]) {
    Frame fr = stk.track(asts[1].exec(env)).getFrame();
    if (fr.numCols() != 1 && !fr.anyVec().isEnum()) throw new IllegalArgumentException("can only match on a single categorical column.");
    Key tmp = Key.make();
    String[] _strsTable=null;
    double[] _dblsTable=null;
    if( asts[2] instanceof ASTNumList ) _dblsTable = ((ASTNumList)asts[2]).expand();
    else if( asts[2] instanceof ASTNum ) _dblsTable = new double[]{asts[2].exec(env).getNum()};
    else if( asts[2] instanceof ASTStrList) _strsTable = ((ASTStrList)asts[2])._strs;
    else if( asts[2] instanceof ASTStr) _strsTable = new String[]{asts[2].exec(env).getStr()};
    else throw new IllegalArgumentException("Expected numbers/strings. Got: "+asts[2].getClass());

    final String[] strsTable = _strsTable;
    final double[] dblsTable = _dblsTable;

    Frame rez = new MRTask() {
      @Override public void map(Chunk c, NewChunk n) {
        int rows = c._len;
        if(strsTable==null)
          for (int r = 0; r < rows; ++r) n.addNum(c.isNA(r)?0:in(dblsTable, c.atd(r)),0);
        else
          for (int r = 0; r < rows; ++r) n.addNum(c.isNA(r)?0:in(strsTable, c.vec().domain()[(int)c.at8(r)]),0);
      }
    }.doAll(1, fr.anyVec()).outputFrame(tmp, null, null);
    return new ValFrame(rez);
  }
  private static int in(String[] matches, String s) { return Arrays.binarySearch(matches, s) >=0 ? 1: 0;}
  private static int in(double[] matches, double d) { return binarySearchDoublesUlp(matches, 0,matches.length,d) >=0 ? 1: 0;}

  private static int binarySearchDoublesUlp(double[] a, int from, int to, double key) {
    int lo = from;
    int hi = to-1;
    while( lo <= hi ) {
      int mid = (lo + hi) >>> 1;
      double midVal = a[mid];
      if( MathUtils.equalsWithinOneSmallUlp(midVal, key) ) return mid;
      if (midVal < key)      lo = mid + 1;
      else if (midVal > key) hi = mid - 1;
      else {
        long midBits = Double.doubleToLongBits(midVal);
        long keyBits = Double.doubleToLongBits(key);
        if (midBits == keyBits) return mid;
        else if (midBits < keyBits) lo = mid + 1;
        else                        hi = mid - 1;
      }
    }
    return -(lo + 1);  // key not found.
  }
}

class ASTWhich extends ASTPrim {
  @Override int nargs() { return 1+1; } // (h2o.which col)
  @Override String str() { return "h2o.which"; }
  @Override ValFrame apply(Env env, Env.StackHelp stk, AST asts[]) {
    Frame f = stk.track(asts[1].exec(env)).getFrame();
    if( f.numRows()==1 && f.numCols() > 1) {
      double[] in = new double[f.numCols()];
      for(int i=0;i<in.length;++i) in[i] = f.vecs()[i].at(0)==1?i:-1;
      Futures fs = new Futures();
      Key key = Vec.VectorGroup.VG_LEN1.addVecs(1)[0];
      AppendableVec v = new AppendableVec(key);
      NewChunk chunk = new NewChunk(v, 0);
      for (double d : in) {
        if( d!=-1)
          chunk.addNum(d);
      }
      chunk.close(0, fs);
      Vec vec = v.close(fs);
      fs.blockForPending();
      return new ValFrame(new Frame(vec));
    }
    Frame f2 = new MRTask() {
      @Override public void map(Chunk c, NewChunk nc) {
        long start = c.start();
        for(int i=0;i<c._len;++i)
          if( c.at8(i)==1 ) nc.addNum(start+i);
      }
    }.doAll(1,f.anyVec()).outputFrame();
    return new ValFrame(f2);
  }
}